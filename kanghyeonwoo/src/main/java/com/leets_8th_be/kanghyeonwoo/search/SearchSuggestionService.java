package com.leets_8th_be.kanghyeonwoo.search;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import org.springframework.stereotype.Service;

@Service
public class SearchSuggestionService {
    public record Suggestion(long keywordId, String keyword, int distance, long recentSearchCount) {}
    private final SearchStore store;
    private final Clock clock;
    private BkTree tree = new BkTree();
    private long version = -1;
    private final Set<Long> indexedIds = new HashSet<>();
    private final ReentrantLock indexLock = new ReentrantLock();

    public SearchSuggestionService(SearchStore store, Clock clock) { this.store = store; this.clock = clock; }

    public void rebuild() {
        indexLock.lock();
        try {
            long current = store.dictionaryVersion();
            BkTree replacement = new BkTree();
            indexedIds.clear();
            store.dictionary().forEach(keyword -> { replacement.add(keyword); indexedIds.add(keyword.id()); });
            tree = replacement;
            version = current;
        } finally {
            indexLock.unlock();
        }
    }

    private List<BkTree.Match> candidates(String keyword) {
        indexLock.lock();
        try {
            long current = store.dictionaryVersion();
            if (version != current) {
                try {
                    store.dictionary().forEach(word -> {
                        if (!indexedIds.contains(word.id())) {
                            tree.add(word);
                            indexedIds.add(word.id());
                        }
                    });
                    version = current;
                } catch (RuntimeException failure) {
                    rebuild();
                }
            }
            int length = keyword.codePointCount(0, keyword.length());
            return tree.find(keyword, length <= 4 ? 1 : 2);
        } finally {
            indexLock.unlock();
        }
    }

    public List<Suggestion> suggest(String input) {
        String keyword = SearchNormalization.requireKeyword(input);
        Instant now = clock.instant();
        List<BkTree.Match> matches = candidates(keyword);
        Map<Long, Long> counts = store.counts(now.minus(1, ChronoUnit.HOURS), now);
        return matches.stream().map(m -> new Suggestion(m.keywordId(), m.keyword(), m.distance(),
                        counts.getOrDefault(m.keywordId(), 0L)))
                .sorted(Comparator.comparingInt(Suggestion::distance)
                        .thenComparing(Comparator.comparingLong(Suggestion::recentSearchCount).reversed())
                        .thenComparing(Suggestion::keyword)).limit(5).toList();
    }
}
