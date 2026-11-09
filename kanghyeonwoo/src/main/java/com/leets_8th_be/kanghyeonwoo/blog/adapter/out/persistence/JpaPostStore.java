package com.leets_8th_be.kanghyeonwoo.blog.adapter.out.persistence;

import com.leets_8th_be.kanghyeonwoo.blog.application.model.PostView;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.out.PostStore;
import com.leets_8th_be.kanghyeonwoo.blog.domain.model.Post;
import com.leets_8th_be.kanghyeonwoo.blog.domain.model.PostId;
import com.leets_8th_be.kanghyeonwoo.blog.domain.model.PostState;
import com.leets_8th_be.kanghyeonwoo.user.domain.model.UserId;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public class JpaPostStore implements PostStore {
    private final PostJpaRepository posts;

    public JpaPostStore(PostJpaRepository posts) {
        this.posts = posts;
    }

    @Override
    public Optional<PostView> findActiveById(PostId id) {
        return posts.findById(id.value())
                .filter(post -> post.getStatus() == PostJpaEntity.Visibility.ACTIVE)
                .map(JpaPostStore::toView);
    }

    @Override
    public Optional<PostView> findById(PostId id) {
        return posts.findById(id.value()).map(JpaPostStore::toView);
    }

    @Override
    public List<PostView> findAll() {
        return posts.findAll().stream().map(JpaPostStore::toView).toList();
    }

    @Override
    public Optional<Post> findForUpdate(PostId id) {
        return posts.findForUpdate(id.value()).map(JpaPostStore::toDomain);
    }

    @Override
    public PostView save(Post post) {
        PostJpaEntity entity;
        if (post.id() == null) {
            entity = PostJpaEntity.create(post.title(), post.content(), post.authorId().value());
        } else {
            entity = posts.findById(post.id().value())
                    .orElseThrow(() -> new IllegalStateException("저장할 게시글이 없습니다."));
            entity.updateContent(post.title(), post.content());
            if (post.state() == PostState.HIDDEN) entity.hide();
            if (post.state() == PostState.DELETED) entity.markDeleted();
        }
        return toView(posts.save(entity));
    }

    private static PostView toView(PostJpaEntity post) {
        return new PostView(new PostId(post.getId()), post.getTitle(), post.getContent(),
                new UserId(post.getUserId()), PostState.valueOf(post.getStatus().name()),
                post.getCreatedAt(), post.getUpdatedAt());
    }

    private static Post toDomain(PostJpaEntity post) {
        return new Post(new PostId(post.getId()), post.getTitle(), post.getContent(), new UserId(post.getUserId()),
                PostState.valueOf(post.getStatus().name()));
    }
}
