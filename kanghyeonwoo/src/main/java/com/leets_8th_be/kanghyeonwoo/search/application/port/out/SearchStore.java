package com.leets_8th_be.kanghyeonwoo.search.application.port.out;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public interface SearchStore {
    record Keyword(long id, String keyword, boolean suggestible) {}
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
}
