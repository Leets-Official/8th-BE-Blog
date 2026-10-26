package com.leets_8th_be.kanghyeonwoo.blog.application.port.out;

import com.leets_8th_be.kanghyeonwoo.blog.application.model.PostView;
import com.leets_8th_be.kanghyeonwoo.blog.domain.model.Post;
import com.leets_8th_be.kanghyeonwoo.blog.domain.model.PostId;

import java.util.Optional;

public interface PostStore {
    PostView save(Post post);

    Optional<PostView> findActiveById(PostId id);
}
