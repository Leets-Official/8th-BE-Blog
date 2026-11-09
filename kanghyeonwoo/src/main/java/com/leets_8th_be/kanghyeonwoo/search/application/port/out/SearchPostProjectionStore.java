package com.leets_8th_be.kanghyeonwoo.search.application.port.out;

import com.leets_8th_be.kanghyeonwoo.search.application.model.PostProjection;
import com.leets_8th_be.kanghyeonwoo.search.application.model.SearchPostPage;

public interface SearchPostProjectionStore {
    void upsert(PostProjection post);
    void delete(long postId);
    SearchPostPage search(int page, int size, String normalizedKeyword);
}
