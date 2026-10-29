package com.leets_8th_be.kanghyeonwoo.search.application.port.out;

import java.time.Instant;
import java.util.List;
import java.util.function.Supplier;

public interface SearchDemoDataStore {
    record DemoKeyword(long sourceId, String keyword, boolean suggestible, Instant createdAt) {}
    record DemoEvent(long sourceKeywordId, Instant searchedAt) {}
    record DemoDedupState(long sourceKeywordId, String sessionKey, Instant lastCountedAt) {}

    void importDemo(List<DemoKeyword> keywords, List<DemoEvent> events, List<DemoDedupState> states,
                    Supplier<Boolean> createDemoContent);
}
