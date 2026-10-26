package com.leets_8th_be.kanghyeonwoo.blog.application.usecase;

import com.leets_8th_be.kanghyeonwoo.blog.application.exception.PostNotFound;
import com.leets_8th_be.kanghyeonwoo.blog.application.model.PostView;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.HidePostUseCase;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.out.PostStore;
import com.leets_8th_be.kanghyeonwoo.blog.domain.model.PostId;

public class HidePostService implements HidePostUseCase {
    private final PostStore posts;

    public HidePostService(PostStore posts) {
        this.posts = posts;
    }

    @Override
    public PostView hide(PostId id) {
        var post = posts.findForUpdate(id).orElseThrow(PostNotFound::new);
        return posts.save(post.hide());
    }
}
