package com.leets_8th_be.kanghyeonwoo.search.application.usecase;
import com.leets_8th_be.kanghyeonwoo.search.application.model.PostProjection;
import com.leets_8th_be.kanghyeonwoo.search.application.port.in.UpdatePostProjectionUseCase;
import com.leets_8th_be.kanghyeonwoo.search.application.port.out.SearchStore;
public class UpdatePostProjectionService implements UpdatePostProjectionUseCase {
    private final SearchStore store;
    public UpdatePostProjectionService(SearchStore store) { this.store = store; }
    @Override public void synchronize(PostProjection post) { store.upsertPostProjection(post); }
    @Override public void remove(long postId) { store.deletePostProjection(postId); }
}
