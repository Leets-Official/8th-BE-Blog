package com.leets_8th_be.kanghyeonwoo.service;

import com.leets_8th_be.kanghyeonwoo.dto.SearchKeywordStatsResponse;

import com.leets_8th_be.kanghyeonwoo.dto.SearchHourlyCountResponse;

import com.leets_8th_be.kanghyeonwoo.repository.SearchRepository;

import com.leets_8th_be.kanghyeonwoo.exception.CustomException;
import com.leets_8th_be.kanghyeonwoo.exception.ErrorCode;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SearchKeywordStatsService {
    private final SearchRepository store;
    private final SearchTrendService trends;
    private final Clock clock;

    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public SearchKeywordStatsResponse stats(long id) {
        SearchRepository.Keyword keyword = store.find(id);
        if (keyword == null) throw new CustomException(ErrorCode.SEARCH_KEYWORD_NOT_FOUND);
        Instant now = clock.instant();
        SearchTrendService.Comparison comparison = trends.compare(id, now);
        ZonedDateTime hour = now.atZone(ZoneId.of("Asia/Seoul")).truncatedTo(ChronoUnit.HOURS);
        List<SearchHourlyCountResponse> hourly = new ArrayList<>();
        for (int i = 23; i >= 0; i--) {
            ZonedDateTime start = hour.minusHours(i);
            Instant end = i == 0 ? now : start.plusHours(1).toInstant();
            hourly.add(new SearchHourlyCountResponse(start.toOffsetDateTime(),
                    store.count(id, start.toInstant(), end), i == 0));
        }
        return new SearchKeywordStatsResponse(id, keyword.keyword(), comparison.current(id), comparison.previous(id),
                comparison.score(id), hourly);
    }
}
