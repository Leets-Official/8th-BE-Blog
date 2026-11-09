package com.leets_8th_be.kanghyeonwoo.search.application.model;
public record SearchTrend(int rank, long keywordId, String keyword, long currentCount, long previousCount, double score) {}
