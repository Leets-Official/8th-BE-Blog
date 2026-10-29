package com.leets_8th_be.kanghyeonwoo.search.config;

import com.leets_8th_be.kanghyeonwoo.search.application.usecase.SearchSuggestionService;
import com.leets_8th_be.kanghyeonwoo.search.application.port.out.SearchDemoDataStore;
import com.leets_8th_be.kanghyeonwoo.user.application.port.in.UserProfileEmailLookup;
import com.leets_8th_be.kanghyeonwoo.user.application.port.in.CreateUserProfileUseCase;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.CreatePostUseCase;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.CreatePostCommand;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
@Profile("demo")
@Order(20)
@RequiredArgsConstructor
public class SearchDemoSeeder implements ApplicationRunner {
    private static final String DEMO_EMAIL = "search-demo@blog.example";
    private final SearchDemoDataStore store;
    private final SearchSuggestionService suggestions;
    private final UserProfileEmailLookup users;
    private final CreateUserProfileUseCase createUsers;
    private final CreatePostUseCase createPosts;
    private final ObjectMapper mapper;
    private final Clock clock;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        JsonNode example;
        try (InputStream input = new ClassPathResource("demo/search-example.json").getInputStream()) {
            example = mapper.readTree(input);
        }
        Instant anchor = clock.instant().truncatedTo(ChronoUnit.MINUTES);
        Duration shift = Duration.between(Instant.parse(example.get("reference_time").asText()), anchor);
        var keywords = new ArrayList<SearchDemoDataStore.DemoKeyword>();
        for (JsonNode row : example.get("search_keywords")) {
            keywords.add(new SearchDemoDataStore.DemoKeyword(row.get("id").asLong(),
                    row.get("normalized_keyword").asText(), row.get("suggestible").asBoolean(),
                    shifted(row.get("created_at"), shift)));
        }
        var events = new ArrayList<SearchDemoDataStore.DemoEvent>();
        for (JsonNode row : example.get("search_events")) {
            events.add(new SearchDemoDataStore.DemoEvent(row.get("keyword_id").asLong(), shifted(row.get("searched_at"), shift)));
        }
        var states = new ArrayList<SearchDemoDataStore.DemoDedupState>();
        for (JsonNode row : example.get("search_dedup_states")) {
            states.add(new SearchDemoDataStore.DemoDedupState(row.get("keyword_id").asLong(),
                    row.get("session_key").asText(), shifted(row.get("last_counted_at"), shift)));
        }
        store.importDemo(keywords, events, states, () -> {
            if (users.findByEmail(DEMO_EMAIL).isPresent()) return false;
            var author = createUsers.create("검색 데모 작성자", DEMO_EMAIL);
            createPosts.create(new CreatePostCommand("Spring Boot와 Java 시작하기",
                    "검색 추천과 집계를 확인하는 데모 게시글입니다.", author.id()));
            createPosts.create(new CreatePostCommand("JPA와 스프링부트 실습",
                    "스프링과 JPA 검색 결과를 확인합니다.", author.id()));
            return true;
        });
        suggestions.rebuild();
    }

    private Instant shifted(JsonNode value, Duration shift) {
        return Instant.parse(value.asText()).plus(shift);
    }
}
