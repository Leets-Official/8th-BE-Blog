package com.leets_8th_be.kanghyeonwoo.blog.application.usecase;

import com.leets_8th_be.kanghyeonwoo.blog.application.exception.PostNotFound;
import com.leets_8th_be.kanghyeonwoo.blog.application.model.PostView;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.GetPostUseCase;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.out.PostStore;
import com.leets_8th_be.kanghyeonwoo.blog.domain.model.PostId;

public class GetPostService implements GetPostUseCase {
    private final PostStore posts;

    public GetPostService(PostStore posts) {
        this.posts = posts;
    }

    @Override
    public PostView get(PostId id) {
        return posts.findActiveById(id).orElseThrow(PostNotFound::new);
    }
}
