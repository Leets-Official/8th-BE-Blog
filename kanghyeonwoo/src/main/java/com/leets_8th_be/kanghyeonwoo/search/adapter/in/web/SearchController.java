package com.leets_8th_be.kanghyeonwoo.search.adapter.in.web;

import static com.leets_8th_be.kanghyeonwoo.shared.web.ApiResponseCode.INVALID_INPUT_VALUE;
import static com.leets_8th_be.kanghyeonwoo.shared.web.ApiResponseCode.SEARCH_KEYWORD_NOT_FOUND;
import static com.leets_8th_be.kanghyeonwoo.shared.web.ApiResponseCode.TYPE_MISMATCH;

import com.leets_8th_be.kanghyeonwoo.search.adapter.in.web.dto.SearchHourlyCountResponse;
import com.leets_8th_be.kanghyeonwoo.search.adapter.in.web.dto.SearchKeywordStatsResponse;
import com.leets_8th_be.kanghyeonwoo.search.adapter.in.web.dto.SearchSuggestionResponse;
import com.leets_8th_be.kanghyeonwoo.search.adapter.in.web.dto.SearchTrendResponse;
import com.leets_8th_be.kanghyeonwoo.search.application.model.SearchKeywordStats;
import com.leets_8th_be.kanghyeonwoo.search.application.model.SearchSuggestion;
import com.leets_8th_be.kanghyeonwoo.search.application.model.SearchTrend;
import com.leets_8th_be.kanghyeonwoo.search.application.port.in.GetSearchKeywordStatsUseCase;
import com.leets_8th_be.kanghyeonwoo.search.application.port.in.GetSearchSuggestionsUseCase;
import com.leets_8th_be.kanghyeonwoo.search.application.port.in.GetSearchTrendsUseCase;
import com.leets_8th_be.kanghyeonwoo.shared.web.ApiErrorCodes;
import com.leets_8th_be.kanghyeonwoo.shared.web.ApiPaths;
import com.leets_8th_be.kanghyeonwoo.shared.web.ApiResponse;
import com.leets_8th_be.kanghyeonwoo.shared.web.ApiResponseCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@RequestMapping(ApiPaths.API_V2 + "/search")
@RequiredArgsConstructor
public class SearchController {
    private final GetSearchSuggestionsUseCase suggestions;
    private final GetSearchTrendsUseCase trends;
    private final GetSearchKeywordStatsUseCase statistics;

    @Operation(summary = "검색어별 최근 24개 시간대의 검색량과 상승 점수")
    @ApiErrorCodes({TYPE_MISMATCH, SEARCH_KEYWORD_NOT_FOUND})
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "검색어 통계 조회 성공",
            useReturnTypeSchema = true)
    @GetMapping("/keywords/{keywordId}/stats")
    public ResponseEntity<ApiResponse<SearchKeywordStatsResponse>> stats(
            @Parameter(description = "검색어 ID", example = "1") @PathVariable("keywordId") long id) {
        SearchKeywordStats result = statistics.stats(id);
        SearchKeywordStatsResponse response = toStatsResponse(result);
        return ResponseEntity.ok(ApiResponse.success(ApiResponseCode.SUCCESS, "검색어 통계 조회가 완료되었습니다.", response));
    }

    @Operation(summary = "최근 1시간의 급상승 검색어 상위 10개")
    @ApiErrorCodes
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "급상승 검색어 조회 성공",
            useReturnTypeSchema = true)
    @GetMapping("/trends")
    public ResponseEntity<ApiResponse<List<SearchTrendResponse>>> trends() {
        List<SearchTrendResponse> response =
                trends.trends().stream().map(SearchController::toTrendResponse).toList();
        return ResponseEntity.ok(ApiResponse.success(ApiResponseCode.SUCCESS, "급상승 검색어 조회가 완료되었습니다.", response));
    }

    @Operation(summary = "철자가 비슷한 검색어 추천")
    @ApiErrorCodes({INVALID_INPUT_VALUE})
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "검색어 추천 조회 성공",
            useReturnTypeSchema = true)
    @GetMapping("/suggestions")
    public ResponseEntity<ApiResponse<List<SearchSuggestionResponse>>> suggest(
            @Parameter(description = "추천 기준 검색어 (2~50자)", example = "sprng") @RequestParam("keyword") String keyword) {
        List<SearchSuggestionResponse> response = suggestions.suggest(keyword).stream()
                .map(SearchController::toSuggestionResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(ApiResponseCode.SUCCESS, "검색어 추천이 완료되었습니다.", response));
    }

    private static SearchKeywordStatsResponse toStatsResponse(SearchKeywordStats result) {
        List<SearchHourlyCountResponse> hourlyCounts = result.hourlyCounts().stream()
                .map(hour -> new SearchHourlyCountResponse(hour.hour(), hour.count(), hour.current()))
                .toList();
        return new SearchKeywordStatsResponse(
                result.keywordId(),
                result.keyword(),
                result.currentCount(),
                result.previousCount(),
                result.score(),
                hourlyCounts);
    }

    private static SearchTrendResponse toTrendResponse(SearchTrend trend) {
        return new SearchTrendResponse(
                trend.rank(),
                trend.keywordId(),
                trend.keyword(),
                trend.currentCount(),
                trend.previousCount(),
                trend.score());
    }

    private static SearchSuggestionResponse toSuggestionResponse(SearchSuggestion suggestion) {
        return new SearchSuggestionResponse(
                suggestion.keywordId(), suggestion.keyword(), suggestion.distance(), suggestion.recentSearchCount());
    }
}
