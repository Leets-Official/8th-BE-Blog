package com.leets_8th_be.kanghyeonwoo.search.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;

@Schema(description = "시간대별 검색량 항목")
public record SearchHourlyCountResponse(
        @Schema(description = "시간 구간 시작 시각", example = "2026-10-29T11:00:00+09:00") OffsetDateTime startAt,
        @Schema(description = "해당 시간 구간의 검색량", example = "12") long count,
        @Schema(description = "현재 진행 중인 부분 시간 구간인지 여부", example = "true") boolean partial) {}
