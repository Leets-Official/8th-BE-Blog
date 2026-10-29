package com.leets_8th_be.kanghyeonwoo.service;

import com.leets_8th_be.kanghyeonwoo.dto.SearchTrendResponse;

import com.leets_8th_be.kanghyeonwoo.repository.SearchRepository;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SearchTrendService {
    public record Comparison(Map<Long, Long> current, Map<Long, Long> previous) {
        public long current(long id) { return current.getOrDefault(id, 0L); }
        public long previous(long id) { return previous.getOrDefault(id, 0L); }
        public double score(long id) { return (current(id) - previous(id)) / (double) Math.max(previous(id), 5); }
    }
    private final SearchRepository store;
    private final Clock clock;

    public Comparison compare(Instant now) {
        return new Comparison(store.counts(now.minus(1, ChronoUnit.HOURS), now),
                store.counts(now.minus(2, ChronoUnit.HOURS), now.minus(1, ChronoUnit.HOURS)));
    }

    public Comparison compare(long keywordId, Instant now) {
        return new Comparison(Map.of(keywordId, store.count(keywordId, now.minus(1, ChronoUnit.HOURS), now)),
                Map.of(keywordId, store.count(keywordId, now.minus(2, ChronoUnit.HOURS), now.minus(1, ChronoUnit.HOURS))));
    }

    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public List<SearchTrendResponse> trends() {
        Comparison comparison = compare(clock.instant());
        List<SearchRepository.Keyword> ranked = store.keywords().stream()
                .filter(k -> comparison.current(k.id()) >= 5 && comparison.current(k.id()) > comparison.previous(k.id()))
                .sorted(Comparator.<SearchRepository.Keyword>comparingDouble(k -> comparison.score(k.id())).reversed()
                        .thenComparing(Comparator.<SearchRepository.Keyword>comparingLong(k -> comparison.current(k.id())).reversed())
                        .thenComparing(SearchRepository.Keyword::keyword)).limit(10).toList();
        List<SearchTrendResponse> result = new ArrayList<>();
        for (SearchRepository.Keyword keyword : ranked) {
            result.add(new SearchTrendResponse(result.size() + 1, keyword.id(), keyword.keyword(), comparison.current(keyword.id()),
                    comparison.previous(keyword.id()), comparison.score(keyword.id())));
        }
        return result;
    }
}
