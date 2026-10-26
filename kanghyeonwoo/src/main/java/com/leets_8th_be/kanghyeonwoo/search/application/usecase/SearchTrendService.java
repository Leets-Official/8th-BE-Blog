package com.leets_8th_be.kanghyeonwoo.search.application.usecase;

import com.leets_8th_be.kanghyeonwoo.search.application.model.SearchTrend;
import com.leets_8th_be.kanghyeonwoo.search.application.port.out.SearchStore;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SearchTrendService {
    public record Comparison(Map<Long, Long> current, Map<Long, Long> previous) {
        public long current(long id) { return current.getOrDefault(id, 0L); }
        public long previous(long id) { return previous.getOrDefault(id, 0L); }
        public double score(long id) { return (current(id) - previous(id)) / (double) Math.max(previous(id), 5); }
    }
    private final SearchStore store;
    private final Clock clock;

    public Comparison compare(Instant now) {
        return new Comparison(store.counts(now.minus(1, ChronoUnit.HOURS), now),
                store.counts(now.minus(2, ChronoUnit.HOURS), now.minus(1, ChronoUnit.HOURS)));
    }

    public Comparison compare(long keywordId, Instant now) {
        return new Comparison(Map.of(keywordId, store.count(keywordId, now.minus(1, ChronoUnit.HOURS), now)),
                Map.of(keywordId, store.count(keywordId, now.minus(2, ChronoUnit.HOURS), now.minus(1, ChronoUnit.HOURS))));
    }

    public List<SearchTrend> trends() {
        Comparison comparison = compare(clock.instant());
        List<SearchStore.Keyword> ranked = store.keywords().stream()
                .filter(k -> comparison.current(k.id()) >= 5 && comparison.current(k.id()) > comparison.previous(k.id()))
                .sorted(Comparator.<SearchStore.Keyword>comparingDouble(k -> comparison.score(k.id())).reversed()
                        .thenComparing(Comparator.<SearchStore.Keyword>comparingLong(k -> comparison.current(k.id())).reversed())
                        .thenComparing(SearchStore.Keyword::keyword)).limit(10).toList();
        List<SearchTrend> result = new ArrayList<>();
        for (SearchStore.Keyword keyword : ranked) {
            result.add(new SearchTrend(result.size() + 1, keyword.id(), keyword.keyword(), comparison.current(keyword.id()),
                    comparison.previous(keyword.id()), comparison.score(keyword.id())));
        }
        return result;
    }
}
