package com.leets_8th_be.kanghyeonwoo.search.adapter.in.web.dto;

public record SearchSuggestionResponse(long keywordId, String keyword, int distance, long recentSearchCount) {}
