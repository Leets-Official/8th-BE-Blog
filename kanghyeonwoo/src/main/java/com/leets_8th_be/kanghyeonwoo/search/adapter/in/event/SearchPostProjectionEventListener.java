package com.leets_8th_be.kanghyeonwoo.search.adapter.in.event;

import com.leets_8th_be.kanghyeonwoo.blog.application.contract.PostProjectionRemoved;
import com.leets_8th_be.kanghyeonwoo.blog.application.contract.PostProjectionUpserted;
import com.leets_8th_be.kanghyeonwoo.search.application.model.PostProjection;
import com.leets_8th_be.kanghyeonwoo.search.application.port.in.UpdatePostProjectionUseCase;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class SearchPostProjectionEventListener {
    private final UpdatePostProjectionUseCase projections;

    public SearchPostProjectionEventListener(UpdatePostProjectionUseCase projections) {
        this.projections = projections;
    }

    @EventListener
    public void on(PostProjectionUpserted event) {
        projections.synchronize(new PostProjection(event.postId(), event.title(), event.authorId(),
                event.createdAt(), event.visible()));
    }

    @EventListener
    public void on(PostProjectionRemoved event) {
        projections.remove(event.postId());
    }
}
