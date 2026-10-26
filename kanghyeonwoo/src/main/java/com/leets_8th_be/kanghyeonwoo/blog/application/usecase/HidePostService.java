package com.leets_8th_be.kanghyeonwoo.blog.application.usecase;

import com.leets_8th_be.kanghyeonwoo.blog.application.exception.PostNotFound;
import com.leets_8th_be.kanghyeonwoo.blog.application.model.PostView;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.HidePostUseCase;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.out.PostStore;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.out.PostProjectionPort;
import com.leets_8th_be.kanghyeonwoo.blog.domain.model.PostId;

public class HidePostService implements HidePostUseCase {
    private final PostStore posts;
    private final PostProjectionPort projection;

    public HidePostService(PostStore posts, PostProjectionPort projection) {
        this.posts = posts;
        this.projection = projection;
    }

    @Override
    public PostView hide(PostId id) {
        var post = posts.findForUpdate(id).orElseThrow(PostNotFound::new);
        PostView saved = posts.save(post.hide());
        projection.synchronize(saved);
        return saved;
    }
}
