package com.leets_8th_be.kanghyeonwoo.search.application.usecase;
import com.leets_8th_be.kanghyeonwoo.search.application.model.PostProjection;
import com.leets_8th_be.kanghyeonwoo.search.application.port.in.UpdatePostProjectionUseCase;
import com.leets_8th_be.kanghyeonwoo.search.application.port.out.SearchPostProjectionStore;
public class UpdatePostProjectionService implements UpdatePostProjectionUseCase {
    private final SearchPostProjectionStore store;
    public UpdatePostProjectionService(SearchPostProjectionStore store) { this.store = store; }
    @Override public void synchronize(PostProjection post) { store.upsert(post); }
    @Override public void remove(long postId) { store.delete(postId); }
}
