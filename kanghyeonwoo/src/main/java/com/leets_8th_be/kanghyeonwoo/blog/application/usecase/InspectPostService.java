package com.leets_8th_be.kanghyeonwoo.blog.application.usecase;

import com.leets_8th_be.kanghyeonwoo.blog.application.model.PostView;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.InspectPostUseCase;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.out.PostStore;
import com.leets_8th_be.kanghyeonwoo.blog.domain.model.PostId;

import java.util.Optional;

public class InspectPostService implements InspectPostUseCase {
    private final PostStore posts;

    public InspectPostService(PostStore posts) {
        this.posts = posts;
    }

    @Override
    public Optional<PostView> inspect(PostId id) {
        return posts.findById(id);
    }
}
