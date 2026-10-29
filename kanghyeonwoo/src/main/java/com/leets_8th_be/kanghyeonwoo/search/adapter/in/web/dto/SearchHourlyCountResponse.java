package com.leets_8th_be.kanghyeonwoo.search.adapter.in.web.dto;

import java.time.OffsetDateTime;

public record SearchHourlyCountResponse(OffsetDateTime startAt, long count, boolean partial) {}
