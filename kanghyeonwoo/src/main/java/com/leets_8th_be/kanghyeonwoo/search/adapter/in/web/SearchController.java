package com.leets_8th_be.kanghyeonwoo.search.adapter.in.web;

import com.leets_8th_be.kanghyeonwoo.search.adapter.in.web.dto.SearchSuggestionResponse;
import com.leets_8th_be.kanghyeonwoo.search.adapter.in.web.dto.SearchTrendResponse;
import com.leets_8th_be.kanghyeonwoo.search.adapter.in.web.dto.SearchKeywordStatsResponse;

import com.leets_8th_be.kanghyeonwoo.shared.web.ApiResponse;
import com.leets_8th_be.kanghyeonwoo.shared.web.ApiPaths;
import com.leets_8th_be.kanghyeonwoo.search.application.port.in.GetSearchSuggestionsUseCase;
import com.leets_8th_be.kanghyeonwoo.search.application.port.in.GetSearchTrendsUseCase;
import com.leets_8th_be.kanghyeonwoo.search.application.port.in.GetSearchKeywordStatsUseCase;
import com.leets_8th_be.kanghyeonwoo.search.application.model.SearchKeywordStats;
import com.leets_8th_be.kanghyeonwoo.search.adapter.in.web.dto.SearchHourlyCountResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Search", description = "유사 검색어 추천 및 검색량 통계")
@RestController
@RequestMapping(ApiPaths.API_V1 + "/search")
@RequiredArgsConstructor
public class SearchController {
    private final GetSearchSuggestionsUseCase suggestions;
    private final GetSearchTrendsUseCase trends;
    private final GetSearchKeywordStatsUseCase statistics;

    @Operation(summary = "검색어별 최근 24개 시간대의 검색량과 상승 점수")
    @GetMapping("/keywords/{keywordId}/stats")
    public ResponseEntity<ApiResponse<SearchKeywordStatsResponse>> stats(@PathVariable("keywordId") long id) {
        SearchKeywordStats result = statistics.stats(id);
        return ResponseEntity.ok(ApiResponse.success("S200", "검색어 통계 조회가 완료되었습니다.",
                new SearchKeywordStatsResponse(result.keywordId(), result.keyword(), result.currentCount(),
                        result.previousCount(), result.score(), result.hourlyCounts().stream()
                        .map(hour -> new SearchHourlyCountResponse(hour.hour(), hour.count(), hour.current())).toList())));
    }

    @Operation(summary = "최근 1시간의 급상승 검색어 상위 10개")
    @GetMapping("/trends")
    public ResponseEntity<ApiResponse<List<SearchTrendResponse>>> trends() {
        return ResponseEntity.ok(ApiResponse.success("S200", "급상승 검색어 조회가 완료되었습니다.", trends.trends().stream()
                .map(value -> new SearchTrendResponse(value.rank(), value.keywordId(), value.keyword(), value.currentCount(),
                        value.previousCount(), value.score())).toList()));
    }

    @Operation(summary = "철자가 비슷한 검색어 추천")
    @GetMapping("/suggestions")
    public ResponseEntity<ApiResponse<List<SearchSuggestionResponse>>> suggest(@RequestParam("keyword") String keyword) {
        return ResponseEntity.ok(ApiResponse.success("S200", "검색어 추천이 완료되었습니다.", suggestions.suggest(keyword).stream()
                .map(value -> new SearchSuggestionResponse(value.keywordId(), value.keyword(), value.distance(),
                        value.recentSearchCount())).toList()));
    }
}
