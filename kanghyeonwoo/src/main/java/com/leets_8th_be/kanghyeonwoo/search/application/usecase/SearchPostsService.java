package com.leets_8th_be.kanghyeonwoo.search.application.usecase;
import com.leets_8th_be.kanghyeonwoo.search.application.model.SearchPostPage;
import com.leets_8th_be.kanghyeonwoo.search.application.port.in.SearchPostsUseCase;
import com.leets_8th_be.kanghyeonwoo.search.application.port.out.SearchStore;
import com.leets_8th_be.kanghyeonwoo.search.application.exception.InvalidSearchRequest;
import com.leets_8th_be.kanghyeonwoo.search.domain.SearchNormalization;
public class SearchPostsService implements SearchPostsUseCase {
    private final SearchStore store;
    public SearchPostsService(SearchStore store) { this.store = store; }
    @Override public SearchPostPage search(int page, int size, String input) {
        if (page < 1 || size < 1 || size > 100) throw new InvalidSearchRequest();
        String keyword = SearchNormalization.normalize(input);
        return store.searchPosts(page, size, keyword.isEmpty() ? null : keyword);
    }
}
