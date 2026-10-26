package com.leets_8th_be.kanghyeonwoo.search.application.port.out;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import com.leets_8th_be.kanghyeonwoo.search.application.model.PostProjection;
import com.leets_8th_be.kanghyeonwoo.search.application.model.SearchPostPage;

public interface SearchStore {
    record Keyword(long id, String keyword, boolean suggestible) {}
    record DemoKeyword(long sourceId, String keyword, boolean suggestible, Instant createdAt) {}
    record DemoEvent(long sourceKeywordId, Instant searchedAt) {}
    record DemoDedupState(long sourceKeywordId, String sessionKey, Instant lastCountedAt) {}

    long dictionaryVersion();
    List<Keyword> dictionary();
    List<Keyword> keywords();
    Keyword find(long id);
    void seedDictionary(List<String> words, Instant at);
    void record(String word, String sessionKey, boolean hasResults, Instant at);
    void rebuildBuckets();
    void cleanup(Instant now);
    Map<Long, Long> counts(Instant start, Instant end);
    long count(long keywordId, Instant start, Instant end);
    void upsertPostProjection(PostProjection post);
    void deletePostProjection(long postId);
    SearchPostPage searchPosts(int page, int size, String normalizedKeyword);
    void importDemo(List<DemoKeyword> keywords, List<DemoEvent> events,
                    List<DemoDedupState> states, Supplier<Boolean> createPosts);
}
