package com.leets_8th_be.kanghyeonwoo.search.application.model;
public record SearchSuggestion(long keywordId, String keyword, int distance, long recentSearchCount) {}
