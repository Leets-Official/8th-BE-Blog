package com.leets_8th_be.kanghyeonwoo.search;

import jakarta.servlet.http.HttpSession;
import java.time.Clock;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SearchTrackingService {
    private static final String SESSION_KEY = SearchTrackingService.class.getName() + ".key";
    private final SearchStore store;
    private final Clock clock;

    public void record(String input, HttpSession session, boolean hasResults) {
        String keyword = SearchNormalization.requireKeyword(input);
        String key;
        synchronized (session) {
            key = (String) session.getAttribute(SESSION_KEY);
            if (key == null) {
                key = UUID.randomUUID().toString();
                session.setAttribute(SESSION_KEY, key);
            }
        }
        store.record(keyword, key, hasResults, clock.instant());
    }
}
