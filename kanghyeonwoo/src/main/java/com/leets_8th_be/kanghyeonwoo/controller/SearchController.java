package com.leets_8th_be.kanghyeonwoo.controller;

import com.leets_8th_be.kanghyeonwoo.dto.ApiResponse;
import com.leets_8th_be.kanghyeonwoo.search.SearchSuggestionService;
import com.leets_8th_be.kanghyeonwoo.search.SearchTrendService;
import com.leets_8th_be.kanghyeonwoo.search.SearchKeywordStatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Search", description = "유사 검색어 추천 및 검색량 통계")
@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchController {
    private final SearchSuggestionService suggestions;
    private final SearchTrendService trends;
    private final SearchKeywordStatsService statistics;

    @Operation(summary = "검색어별 최근 24개 시간대의 검색량과 상승 점수")
    @GetMapping("/keywords/{keywordId}/stats")
    public ApiResponse<SearchKeywordStatsService.Statistics> stats(@PathVariable("keywordId") long id) {
        return ApiResponse.success("S200", "검색어 통계 조회가 완료되었습니다.", statistics.stats(id));
    }

    @Operation(summary = "최근 1시간의 급상승 검색어 상위 10개")
    @GetMapping("/trends")
    public ApiResponse<List<SearchTrendService.Trend>> trends() {
        return ApiResponse.success("S200", "급상승 검색어 조회가 완료되었습니다.", trends.trends());
    }

    @Operation(summary = "철자가 비슷한 검색어 추천")
    @GetMapping("/suggestions")
    public ApiResponse<List<SearchSuggestionService.Suggestion>> suggest(@RequestParam("keyword") String keyword) {
        return ApiResponse.success("S200", "검색어 추천이 완료되었습니다.", suggestions.suggest(keyword));
    }
}
