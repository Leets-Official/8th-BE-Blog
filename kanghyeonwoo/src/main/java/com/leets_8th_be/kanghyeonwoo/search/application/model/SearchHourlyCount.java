package com.leets_8th_be.kanghyeonwoo.search.application.model;
import java.time.OffsetDateTime;
public record SearchHourlyCount(OffsetDateTime hour, long count, boolean current) {}
