package com.leets_8th_be.kanghyeonwoo.search.application.port.in;
import com.leets_8th_be.kanghyeonwoo.search.application.model.SearchKeywordStats;
public interface GetSearchKeywordStatsUseCase { SearchKeywordStats stats(long keywordId); }
