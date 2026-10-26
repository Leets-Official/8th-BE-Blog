package com.leets_8th_be.kanghyeonwoo.dto;

import java.time.OffsetDateTime;

public record SearchHourlyCountResponse(OffsetDateTime startAt, long count, boolean partial) {}
