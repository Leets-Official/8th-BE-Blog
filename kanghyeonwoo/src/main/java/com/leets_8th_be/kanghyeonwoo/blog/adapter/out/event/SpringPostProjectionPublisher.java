package com.leets_8th_be.kanghyeonwoo.blog.adapter.out.event;

import com.leets_8th_be.kanghyeonwoo.blog.application.contract.PostProjectionRemoved;
import com.leets_8th_be.kanghyeonwoo.blog.application.contract.PostProjectionUpserted;
import com.leets_8th_be.kanghyeonwoo.blog.application.model.PostView;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.out.PostProjectionPort;
import com.leets_8th_be.kanghyeonwoo.blog.domain.model.PostId;
import com.leets_8th_be.kanghyeonwoo.blog.domain.model.PostState;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class SpringPostProjectionPublisher implements PostProjectionPort {
    private final ApplicationEventPublisher events;

    public SpringPostProjectionPublisher(ApplicationEventPublisher events) {
        this.events = events;
    }

    @Override
    public void synchronize(PostView post) {
        events.publishEvent(new PostProjectionUpserted(post.id().value(), post.title(), post.authorId().value(),
                post.createdAt(), post.state() == PostState.ACTIVE));
    }

    @Override
    public void remove(PostId postId) {
        events.publishEvent(new PostProjectionRemoved(postId.value()));
    }
}
