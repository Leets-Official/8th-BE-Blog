package com.leets_8th_be.kanghyeonwoo.search.application.model;
import java.util.List;
public record SearchKeywordStats(long keywordId, String keyword, long currentCount, long previousCount,
                                 double score, List<SearchHourlyCount> hourlyCounts) {}
