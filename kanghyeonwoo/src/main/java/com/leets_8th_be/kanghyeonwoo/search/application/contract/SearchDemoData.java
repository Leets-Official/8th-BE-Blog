package com.leets_8th_be.kanghyeonwoo.search.application.contract;

import java.time.Instant;
import java.util.List;

public record SearchDemoData(List<DemoKeyword> keywords, List<DemoEvent> events, List<DemoDedupState> states) {
    public SearchDemoData {
        keywords = List.copyOf(keywords);
        events = List.copyOf(events);
        states = List.copyOf(states);
    }

    public record DemoKeyword(long sourceId, String keyword, boolean suggestible, Instant createdAt) {
    }

    public record DemoEvent(long sourceKeywordId, Instant searchedAt) {
    }

    public record DemoDedupState(long sourceKeywordId, String sessionKey, Instant lastCountedAt) {
    }
}
