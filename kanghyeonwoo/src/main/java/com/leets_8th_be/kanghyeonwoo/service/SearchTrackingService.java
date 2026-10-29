package com.leets_8th_be.kanghyeonwoo.service;

import com.leets_8th_be.kanghyeonwoo.repository.SearchRepository;
import com.leets_8th_be.kanghyeonwoo.util.SearchNormalization;

import java.time.Clock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SearchTrackingService {
    private final SearchRepository store;
    private final Clock clock;

    public void record(String input, String sessionKey, boolean hasResults) {
        String keyword = SearchNormalization.requireKeyword(input);
        store.record(keyword, sessionKey, hasResults, clock.instant());
    }
}
