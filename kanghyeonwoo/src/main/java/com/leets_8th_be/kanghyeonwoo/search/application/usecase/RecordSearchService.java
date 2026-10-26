package com.leets_8th_be.kanghyeonwoo.search.application.usecase;

import com.leets_8th_be.kanghyeonwoo.search.application.port.out.SearchStore;
import com.leets_8th_be.kanghyeonwoo.search.domain.SearchNormalization;
import com.leets_8th_be.kanghyeonwoo.search.application.port.in.RecordSearchUseCase;

import java.time.Clock;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RecordSearchService implements RecordSearchUseCase {
    private final SearchStore store;
    private final Clock clock;

    public void record(String input, String sessionKey, boolean hasResults) {
        String keyword = SearchNormalization.requireKeyword(input);
        store.record(keyword, sessionKey, hasResults, clock.instant());
    }
}
