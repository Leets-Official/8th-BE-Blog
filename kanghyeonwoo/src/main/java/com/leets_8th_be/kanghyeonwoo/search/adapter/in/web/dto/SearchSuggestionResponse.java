package com.leets_8th_be.kanghyeonwoo.search.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "철자가 비슷한 검색어 추천 결과")
public record SearchSuggestionResponse(
        @Schema(description = "추천 검색어 ID", example = "1") long keywordId,
        @Schema(description = "추천 검색어", example = "spring") String keyword,
        @Schema(description = "입력 검색어와의 편집 거리", example = "1") int distance,
        @Schema(description = "최근 검색량", example = "24") long recentSearchCount) {}
