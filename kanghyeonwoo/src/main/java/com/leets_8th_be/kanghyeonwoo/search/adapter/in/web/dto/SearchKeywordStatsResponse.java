package com.leets_8th_be.kanghyeonwoo.search.adapter.in.web.dto;

import java.util.List;

public record SearchKeywordStatsResponse(long keywordId, String keyword, long currentCount, long previousCount,
                                         double riseScore, List<SearchHourlyCountResponse> hourlyCounts) {}
