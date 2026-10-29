package com.leets_8th_be.kanghyeonwoo.search.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "최근 1시간 급상승 검색어")
public record SearchTrendResponse(
        @Schema(description = "순위", example = "1") int rank,
        @Schema(description = "검색어 ID", example = "1") long keywordId,
        @Schema(description = "검색어", example = "spring") String keyword,
        @Schema(description = "최근 1시간 검색량", example = "20") long currentCount,
        @Schema(description = "직전 1시간 검색량", example = "8") long previousCount,
        @Schema(description = "상승 점수", example = "1.5") double riseScore) {}
