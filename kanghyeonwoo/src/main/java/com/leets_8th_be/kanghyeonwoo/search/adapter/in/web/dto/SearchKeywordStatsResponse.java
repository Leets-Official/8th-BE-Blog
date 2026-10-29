package com.leets_8th_be.kanghyeonwoo.search.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "검색어 통계 및 최근 24개 시간대 검색량")
public record SearchKeywordStatsResponse(
        @Schema(description = "검색어 ID", example = "1") long keywordId,
        @Schema(description = "정규화된 검색어", example = "spring") String keyword,
        @Schema(description = "최근 1시간 검색량", example = "20") long currentCount,
        @Schema(description = "직전 1시간 검색량", example = "8") long previousCount,
        @Schema(description = "최근 검색량 상승 점수", example = "1.5") double riseScore,
        @Schema(description = "최근 24개 시간대 검색량") List<SearchHourlyCountResponse> hourlyCounts) {}
