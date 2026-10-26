package com.leets_8th_be.kanghyeonwoo.blog.adapter.out.persistence;

import com.leets_8th_be.kanghyeonwoo.blog.application.model.PostView;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.out.PostStore;
import com.leets_8th_be.kanghyeonwoo.blog.domain.model.Post;
import com.leets_8th_be.kanghyeonwoo.blog.domain.model.PostId;
import com.leets_8th_be.kanghyeonwoo.user.domain.model.UserId;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JpaPostStore implements PostStore {
    private final PostJpaRepository posts;

    public JpaPostStore(PostJpaRepository posts) {
        this.posts = posts;
    }

    @Override
    public PostView save(Post post) {
        var saved = posts.save(PostJpaEntity.create(
                post.title(), post.content(), post.authorId().value()));
        return toView(saved);
    }

    @Override
    public Optional<PostView> findActiveById(PostId id) {
        return posts.findById(id.value())
                .filter(post -> post.getStatus() == PostJpaEntity.Visibility.ACTIVE)
                .map(JpaPostStore::toView);
    }

    private static PostView toView(PostJpaEntity post) {
        return new PostView(new PostId(post.getId()), post.getTitle(), post.getContent(),
                new UserId(post.getUserId()), post.getCreatedAt(), post.getUpdatedAt());
    }
}
