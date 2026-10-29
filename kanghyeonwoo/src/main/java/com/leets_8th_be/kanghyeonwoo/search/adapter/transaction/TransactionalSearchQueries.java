package com.leets_8th_be.kanghyeonwoo.search.adapter.transaction;

import com.leets_8th_be.kanghyeonwoo.search.application.model.SearchKeywordStats;
import com.leets_8th_be.kanghyeonwoo.search.application.model.SearchSuggestion;
import com.leets_8th_be.kanghyeonwoo.search.application.model.SearchTrend;
import com.leets_8th_be.kanghyeonwoo.search.application.port.in.GetSearchKeywordStatsUseCase;
import com.leets_8th_be.kanghyeonwoo.search.application.port.in.GetSearchSuggestionsUseCase;
import com.leets_8th_be.kanghyeonwoo.search.application.port.in.GetSearchTrendsUseCase;
import com.leets_8th_be.kanghyeonwoo.search.application.usecase.SearchKeywordStatsService;
import com.leets_8th_be.kanghyeonwoo.search.application.usecase.SearchSuggestionService;
import com.leets_8th_be.kanghyeonwoo.search.application.usecase.SearchTrendService;
import java.util.List;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

public class TransactionalSearchQueries implements GetSearchKeywordStatsUseCase,
        GetSearchSuggestionsUseCase, GetSearchTrendsUseCase {
    private final SearchKeywordStatsService stats;
    private final SearchSuggestionService suggestions;
    private final SearchTrendService trends;
    private final TransactionTemplate reads;

    public TransactionalSearchQueries(SearchKeywordStatsService stats, SearchSuggestionService suggestions,
                                      SearchTrendService trends, TransactionTemplate reads) {
        this.stats = stats;
        this.suggestions = suggestions;
        this.trends = trends;
        this.reads = reads;
        this.reads.setReadOnly(true);
        this.reads.setIsolationLevel(TransactionDefinition.ISOLATION_REPEATABLE_READ);
    }

    @Override public SearchKeywordStats stats(long id) { return reads.execute(status -> stats.stats(id)); }
    @Override public List<SearchSuggestion> suggest(String keyword) { return reads.execute(status -> suggestions.suggest(keyword)); }
    @Override public List<SearchTrend> trends() { return reads.execute(status -> trends.trends()); }
}
