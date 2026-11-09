package com.leets_8th_be.kanghyeonwoo.blog.adapter.out.search;
import com.leets_8th_be.kanghyeonwoo.blog.application.model.PostView;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.out.PostProjectionPort;
import com.leets_8th_be.kanghyeonwoo.blog.domain.model.PostId;
import com.leets_8th_be.kanghyeonwoo.search.application.model.PostProjection;
import com.leets_8th_be.kanghyeonwoo.search.application.port.in.UpdatePostProjectionUseCase;
import org.springframework.stereotype.Component;
@Component
public class SearchPostProjectionAdapter implements PostProjectionPort {
    private final UpdatePostProjectionUseCase projection;
    public SearchPostProjectionAdapter(UpdatePostProjectionUseCase projection) { this.projection = projection; }
    @Override public void synchronize(PostView post) {
        projection.synchronize(new PostProjection(post.id().value(), post.title(), post.authorId().value(),
                post.createdAt(), post.state() == com.leets_8th_be.kanghyeonwoo.blog.domain.model.PostState.ACTIVE));
    }
    @Override public void remove(PostId postId) { projection.remove(postId.value()); }
}
