package com.leets_8th_be.kanghyeonwoo.dto;

public record SearchSuggestionResponse(long keywordId, String keyword, int distance, long recentSearchCount) {}
