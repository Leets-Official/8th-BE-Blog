package com.leets_8th_be.kanghyeonwoo.search.application.usecase;

import com.leets_8th_be.kanghyeonwoo.search.application.model.SearchKeywordStats;
import com.leets_8th_be.kanghyeonwoo.search.application.model.SearchHourlyCount;
import com.leets_8th_be.kanghyeonwoo.search.application.port.out.SearchStore;
import com.leets_8th_be.kanghyeonwoo.search.application.exception.SearchKeywordNotFound;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SearchKeywordStatsService {
    private final SearchStore store;
    private final SearchTrendService trends;
    private final Clock clock;

    public SearchKeywordStats stats(long id) {
        SearchStore.Keyword keyword = store.find(id);
        if (keyword == null) throw new SearchKeywordNotFound();
        Instant now = clock.instant();
        SearchTrendService.Comparison comparison = trends.compare(id, now);
        ZonedDateTime hour = now.atZone(ZoneId.of("Asia/Seoul")).truncatedTo(ChronoUnit.HOURS);
        List<SearchHourlyCount> hourly = new ArrayList<>();
        for (int i = 23; i >= 0; i--) {
            ZonedDateTime start = hour.minusHours(i);
            Instant end = i == 0 ? now : start.plusHours(1).toInstant();
            hourly.add(new SearchHourlyCount(start.toOffsetDateTime(),
                    store.count(id, start.toInstant(), end), i == 0));
        }
        return new SearchKeywordStats(id, keyword.keyword(), comparison.current(id), comparison.previous(id),
                comparison.score(id), hourly);
    }
}
