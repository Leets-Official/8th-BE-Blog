package com.leets_8th_be.kanghyeonwoo.controller;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicReference;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {"spring.datasource.hikari.maximum-pool-size=2",
        "spring.datasource.hikari.connection-timeout=1000"})
@AutoConfigureMockMvc
@Import(SearchAnalyticsTest.TimeConfiguration.class)
class SearchAnalyticsTest {
    static final Instant NOW = Instant.parse("2026-10-26T06:00:00Z");
    @Autowired MockMvc mvc;
    @Autowired MutableClock clock;
    @Autowired JdbcTemplate jdbc;
    @Autowired com.leets_8th_be.kanghyeonwoo.search.application.port.out.SearchStore searchStore;

    @BeforeEach
    void resetSearches() {
        jdbc.update("DELETE FROM search_events");
        jdbc.update("DELETE FROM search_dedup_states");
        jdbc.update("DELETE FROM search_keyword_buckets");
        clock.set(NOW.minusSeconds(60));
    }

    @Test
    void ranksAKeywordAfterSixIndependentSearchesEvenWithoutResults() throws Exception {
        for (int i = 0; i < 6; i++) {
            mvc.perform(get("/api/v2/posts").param("keyword", "risingword").session(new MockHttpSession()))
                    .andExpect(status().isOk());
        }
        clock.set(NOW);
        mvc.perform(get("/api/v2/search/trends"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].keyword").value("risingword"))
                .andExpect(jsonPath("$.data[0].currentCount").value(6))
                .andExpect(jsonPath("$.data[0].riseScore").value(1.2));
    }

    @Test
    void countsAgainAtFiveMinutesAndFillsTwentyFourHourlyIntervals() throws Exception {
        MockHttpSession session = new MockHttpSession();
        clock.set(NOW.minusSeconds(600));
        mvc.perform(get("/api/v2/posts").param("keyword", "spring").session(session)).andExpect(status().isOk());
        clock.set(NOW.minusSeconds(301));
        mvc.perform(get("/api/v2/posts").param("keyword", " SPRING ").session(session)).andExpect(status().isOk());
        clock.set(NOW.minusSeconds(300));
        mvc.perform(get("/api/v2/posts").param("keyword", "spring").session(session)).andExpect(status().isOk());
        clock.set(NOW);
        String body = mvc.perform(get("/api/v2/search/suggestions").param("keyword", "sprng"))
                .andExpect(jsonPath("$.data[0].recentSearchCount").value(2))
                .andReturn().getResponse().getContentAsString();
        Number id = com.jayway.jsonpath.JsonPath.read(body, "$.data[0].keywordId");
        mvc.perform(get("/api/v2/search/keywords/" + id + "/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.currentCount").value(2))
                .andExpect(jsonPath("$.data.previousCount").value(0))
                .andExpect(jsonPath("$.data.hourlyCounts.length()").value(24))
                .andExpect(jsonPath("$.data.hourlyCounts[22].count").value(2))
                .andExpect(jsonPath("$.data.hourlyCounts[23].count").value(0))
                .andExpect(jsonPath("$.data.hourlyCounts[23].partial").value(true));
    }

    @Test
    void concurrentSearchesFromOneSessionCountOnlyOnce() throws Exception {
        MockHttpSession session = new MockHttpSession();
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var requests = new ArrayList<Callable<Integer>>();
            for (int i = 0; i < 12; i++) requests.add(() -> mvc.perform(
                    get("/api/v2/posts").param("keyword", "spring").session(session))
                    .andReturn().getResponse().getStatus());
            for (var result : executor.invokeAll(requests)) {
                org.assertj.core.api.Assertions.assertThat(result.get()).isEqualTo(200);
            }
        }
        clock.set(NOW);
        mvc.perform(get("/api/v2/search/suggestions").param("keyword", "sprng"))
                .andExpect(jsonPath("$.data[0].recentSearchCount").value(1));
    }

    @Test
    void concurrentSuggestionsObserveANewDictionaryEntryWithASmallConnectionPool() throws Exception {
        searchStore.seedDictionary(java.util.List.of("concurrentspring"), clock.instant());
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var requests = new ArrayList<Callable<Void>>();
            for (int i = 0; i < 12; i++) requests.add(() -> {
                mvc.perform(get("/api/v2/search/suggestions").param("keyword", "concurrentsprng"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.data[0].keyword").value("concurrentspring"));
                return null;
            });
            for (var result : executor.invokeAll(requests)) result.get();
        }
    }

    @Test
    void excludesListPaginationAndSuggestionRequestsFromSearchCounts() throws Exception {
        MockHttpSession session = new MockHttpSession();
        mvc.perform(get("/api/v2/posts").session(session)).andExpect(status().isOk());
        mvc.perform(get("/api/v2/posts").param("keyword", "spring").param("page", "2").session(session))
                .andExpect(status().isOk());
        mvc.perform(get("/api/v2/search/suggestions").param("keyword", "spring").session(session))
                .andExpect(status().isOk());
        clock.set(NOW);
        mvc.perform(get("/api/v2/search/suggestions").param("keyword", "sprng"))
                .andExpect(jsonPath("$.data[0].recentSearchCount").value(0));
    }

    @Test
    void observesExactRollingWindowBoundariesInsidePartialMinutes() throws Exception {
        Instant anchor = NOW.plusSeconds(30);
        searchAt(anchor.minusSeconds(7201), 1);
        searchAt(anchor.minusSeconds(7200), 1);
        searchAt(anchor.minusSeconds(3601), 4);
        searchAt(anchor.minusSeconds(3600), 3);
        searchAt(anchor.minusSeconds(1), 3);
        searchAt(anchor, 1);
        clock.set(anchor);
        mvc.perform(get("/api/v2/search/trends"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].keyword").value("spring"))
                .andExpect(jsonPath("$.data[0].currentCount").value(6))
                .andExpect(jsonPath("$.data[0].previousCount").value(5))
                .andExpect(jsonPath("$.data[0].riseScore").value(0.2));
    }

    @Test
    void rejectsInvalidInputsAndUnknownKeywordIds() throws Exception {
        mvc.perform(get("/api/v2/search/suggestions")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/v2/search/suggestions").param("keyword", "a")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/v2/posts").param("keyword", "x".repeat(51))).andExpect(status().isBadRequest());
        mvc.perform(get("/api/v2/posts").param("size", "0")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/v2/search/keywords/999999/stats"))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("SEARCH_KEYWORD_NOT_FOUND"));
    }

    @Test
    void retentionKeepsTheCutoffMinuteConsistentWithRemainingHistory() {
        Instant now = NOW.plusSeconds(30);
        Instant cutoff = now.minus(java.time.Duration.ofDays(30));
        searchStore.record("retention", "expired-session", false, cutoff.minusSeconds(1));
        searchStore.record("retention", "retained-session", false, cutoff);
        Instant minute = cutoff.truncatedTo(java.time.temporal.ChronoUnit.MINUTES);
        org.assertj.core.api.Assertions.assertThat(searchStore.counts(minute, minute.plusSeconds(60)).values())
                .containsExactly(2L);
        searchStore.cleanup(now);
        org.assertj.core.api.Assertions.assertThat(searchStore.counts(minute, minute.plusSeconds(60)).values())
                .containsExactly(1L);
    }

    @Test
    void recommendationTiesUseRecentSearchCountsThenKeywordOrder() throws Exception {
        searchStore.seedDictionary(java.util.List.of("sprang", "sprung"), clock.instant());
        searchAt("spring", NOW.minusSeconds(600), 2);
        searchAt("sprang", NOW.minusSeconds(600), 2);
        searchAt("sprung", NOW.minusSeconds(600), 3);
        clock.set(NOW);
        mvc.perform(get("/api/v2/search/suggestions").param("keyword", "sprng"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].keyword").value("sprung"))
                .andExpect(jsonPath("$.data[1].keyword").value("sprang"))
                .andExpect(jsonPath("$.data[2].keyword").value("spring"));
    }

    @Test
    void equalRiseScoresUseCurrentCountsThenKeywordOrder() throws Exception {
        searchAt("spring", NOW.minusSeconds(4800), 5);
        searchAt("spring", NOW.minusSeconds(1200), 10);
        searchAt("jpa", NOW.minusSeconds(1200), 5);
        searchAt("java", NOW.minusSeconds(1200), 5);
        clock.set(NOW);
        mvc.perform(get("/api/v2/search/trends"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].keyword").value("spring"))
                .andExpect(jsonPath("$.data[0].riseScore").value(1.0))
                .andExpect(jsonPath("$.data[1].keyword").value("java"))
                .andExpect(jsonPath("$.data[2].keyword").value("jpa"));
    }

    @Test
    void aFailedBucketWriteRollsBackTheEventAndAllowsRetry() throws Exception {
        searchAt(NOW.minusSeconds(120), 1);
        MockHttpSession retrySession = new MockHttpSession();
        jdbc.execute("ALTER TABLE search_keyword_buckets ADD CONSTRAINT test_bucket_failure CHECK (search_count <= 1)");
        try {
            mvc.perform(get("/api/v2/posts").param("keyword", "spring").session(retrySession))
                    .andExpect(status().isInternalServerError());
            clock.set(NOW);
            mvc.perform(get("/api/v2/search/suggestions").param("keyword", "sprng"))
                    .andExpect(jsonPath("$.data[?(@.keyword == 'spring')].recentSearchCount").value(org.hamcrest.Matchers.contains(1)));
        } finally {
            jdbc.execute("ALTER TABLE search_keyword_buckets DROP CONSTRAINT test_bucket_failure");
        }
        clock.set(NOW.minusSeconds(120));
        mvc.perform(get("/api/v2/posts").param("keyword", "spring").session(retrySession)).andExpect(status().isOk());
        clock.set(NOW);
        mvc.perform(get("/api/v2/search/suggestions").param("keyword", "sprng"))
                .andExpect(jsonPath("$.data[?(@.keyword == 'spring')].recentSearchCount").value(org.hamcrest.Matchers.contains(2)));
    }

    private void searchAt(Instant at, int count) throws Exception {
        searchAt("spring", at, count);
    }

    private void searchAt(String keyword, Instant at, int count) throws Exception {
        clock.set(at);
        for (int i = 0; i < count; i++) mvc.perform(get("/api/v2/posts").param("keyword", keyword)
                .session(new MockHttpSession())).andExpect(status().isOk());
    }

    @TestConfiguration
    static class TimeConfiguration {
        @Bean @Primary MutableClock mutableClock() { return new MutableClock(); }
    }

    static class MutableClock extends Clock {
        private final AtomicReference<Instant> now = new AtomicReference<>(NOW);
        void set(Instant instant) { now.set(instant); }
        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zone) { return Clock.fixed(instant(), zone); }
        @Override public Instant instant() { return now.get(); }
    }
}
