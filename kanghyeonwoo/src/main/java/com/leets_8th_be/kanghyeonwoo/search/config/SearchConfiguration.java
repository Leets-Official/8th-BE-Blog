package com.leets_8th_be.kanghyeonwoo.search.config;

import com.leets_8th_be.kanghyeonwoo.search.application.usecase.SearchSuggestionService;
import com.leets_8th_be.kanghyeonwoo.search.application.port.out.SearchStore;
import com.leets_8th_be.kanghyeonwoo.search.application.usecase.SearchSuggestionService;
import com.leets_8th_be.kanghyeonwoo.search.application.usecase.SearchTrendService;
import com.leets_8th_be.kanghyeonwoo.search.application.usecase.SearchKeywordStatsService;
import com.leets_8th_be.kanghyeonwoo.search.application.usecase.RecordSearchService;
import com.leets_8th_be.kanghyeonwoo.search.application.port.in.GetSearchSuggestionsUseCase;
import com.leets_8th_be.kanghyeonwoo.search.adapter.transaction.TransactionalSearchQueries;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.util.List;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
@EnableScheduling
public class SearchConfiguration {
    @Bean public Clock searchClock() { return Clock.systemUTC(); }

    @Bean public SearchSuggestionService searchSuggestionService(SearchStore store, Clock searchClock) {
        return new SearchSuggestionService(store, searchClock);
    }

    @Bean public SearchTrendService searchTrendService(SearchStore store, Clock searchClock) {
        return new SearchTrendService(store, searchClock);
    }

    @Bean public SearchKeywordStatsService searchKeywordStatsService(SearchStore store,
            SearchTrendService trends, Clock searchClock) {
        return new SearchKeywordStatsService(store, trends, searchClock);
    }

    @Bean public RecordSearchService recordSearchService(SearchStore store, Clock searchClock) {
        return new RecordSearchService(store, searchClock);
    }

    @Bean public TransactionalSearchQueries searchQueries(SearchKeywordStatsService stats,
            SearchSuggestionService suggestions, SearchTrendService trends, PlatformTransactionManager manager) {
        return new TransactionalSearchQueries(stats, suggestions, trends, new TransactionTemplate(manager));
    }

    @Bean
    @Order(10)
    public ApplicationRunner searchStartup(SearchStore store, SearchSuggestionService suggestions, Clock clock) {
        return args -> {
            store.backfillTitles();
            store.cleanup(clock.instant());
            store.rebuildBuckets();
            store.seedDictionary(List.of("spring", "spring boot", "java", "jpa", "스프링", "스프링부트", "자바"), clock.instant());
            suggestions.rebuild();
        };
    }
}
