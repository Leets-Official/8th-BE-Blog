package com.leets_8th_be.kanghyeonwoo.blog.application.port.out;

import com.leets_8th_be.kanghyeonwoo.blog.application.model.PostView;
import com.leets_8th_be.kanghyeonwoo.blog.domain.model.Post;
import com.leets_8th_be.kanghyeonwoo.blog.domain.model.PostId;

import java.util.Optional;

public interface PostStore {
    Optional<PostView> findActiveById(PostId id);

    Optional<PostView> findById(PostId id);

    Optional<Post> findForUpdate(PostId id);

    PostView save(Post post);

    void delete(PostId id);
}
