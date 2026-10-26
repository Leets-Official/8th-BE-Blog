package com.leets_8th_be.kanghyeonwoo.search.config;

import com.leets_8th_be.kanghyeonwoo.search.application.port.out.SearchStore;

import java.time.Clock;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SearchMaintenance {
    private final SearchStore store;
    private final Clock clock;

    @Scheduled(initialDelay = 300_000, fixedDelay = 3_600_000)
    public void removeExpiredHistory() {
        store.cleanup(clock.instant());
    }
}
