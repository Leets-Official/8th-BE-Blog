package com.leets_8th_be.kanghyeonwoo.search;

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
