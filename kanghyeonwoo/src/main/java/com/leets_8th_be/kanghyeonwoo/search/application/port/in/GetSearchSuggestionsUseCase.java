package com.leets_8th_be.kanghyeonwoo.search.application.port.in;
import com.leets_8th_be.kanghyeonwoo.search.application.model.SearchSuggestion;
import java.util.List;
public interface GetSearchSuggestionsUseCase { List<SearchSuggestion> suggest(String keyword); }
