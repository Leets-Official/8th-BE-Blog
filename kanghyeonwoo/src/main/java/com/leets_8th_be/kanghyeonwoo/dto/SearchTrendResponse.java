package com.leets_8th_be.kanghyeonwoo.dto;

public record SearchTrendResponse(int rank, long keywordId, String keyword, long currentCount,
                                  long previousCount, double riseScore) {}
