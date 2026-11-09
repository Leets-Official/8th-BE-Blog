package com.leets_8th_be.kanghyeonwoo.blog.application.usecase;

import com.leets_8th_be.kanghyeonwoo.blog.application.exception.AuthorNotFound;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.CreatePostCommand;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.out.PostStore;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.out.PostProjectionPort;
import com.leets_8th_be.kanghyeonwoo.blog.application.model.PostView;
import com.leets_8th_be.kanghyeonwoo.blog.domain.model.Post;
import com.leets_8th_be.kanghyeonwoo.blog.domain.model.PostId;
import com.leets_8th_be.kanghyeonwoo.blog.domain.model.PostState;
import com.leets_8th_be.kanghyeonwoo.user.domain.model.UserId;
import com.leets_8th_be.kanghyeonwoo.user.application.model.UserProfile;
import com.leets_8th_be.kanghyeonwoo.user.application.port.in.UserProfileLookup;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicReference;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CreatePostServiceTest {

    @Test
    void rejectsPostWhenAuthorProfileDoesNotExist() {
        UserProfileLookup users = ignored -> Optional.empty();
        PostStore posts = new PostStore() {
            @Override
            public com.leets_8th_be.kanghyeonwoo.blog.application.model.PostView save(
                    com.leets_8th_be.kanghyeonwoo.blog.domain.model.Post post) {
                throw new AssertionError("post must not be stored when the author does not exist");
            }

            @Override
            public Optional<com.leets_8th_be.kanghyeonwoo.blog.application.model.PostView> findActiveById(
                    com.leets_8th_be.kanghyeonwoo.blog.domain.model.PostId postId) {
                return Optional.empty();
            }

            @Override
            public Optional<PostView> findById(PostId postId) { return Optional.empty(); }

            @Override
            public java.util.List<PostView> findAll() { return java.util.List.of(); }

            @Override
            public Optional<Post> findForUpdate(PostId postId) { return Optional.empty(); }

        };
        CreatePostService createPost = new CreatePostService(users, posts, noProjectionChanges());

        assertThatThrownBy(() -> createPost.create(
                new CreatePostCommand("제목", "본문", new UserId(404L))))
                .isInstanceOf(AuthorNotFound.class);
    }

    @Test
    void createsPostForAnExistingAuthor() {
        UserId authorId = new UserId(7L);
        AtomicReference<Post> storedPost = new AtomicReference<>();
        UserProfileLookup users = id -> Optional.of(new UserProfile(id, "작성자", "author@example.com"));
        PostStore posts = new PostStore() {
            @Override
            public PostView save(Post post) {
                storedPost.set(post);
                return new PostView(new PostId(42L), post.title(), post.content(), post.authorId(),
                        PostState.ACTIVE, LocalDateTime.parse("2026-01-02T03:04:05"),
                        LocalDateTime.parse("2026-01-02T03:04:05"));
            }

            @Override
            public Optional<PostView> findActiveById(PostId id) {
                return Optional.empty();
            }

            @Override
            public Optional<PostView> findById(PostId id) { return Optional.empty(); }

            @Override
            public java.util.List<PostView> findAll() { return java.util.List.of(); }

            @Override
            public Optional<Post> findForUpdate(PostId id) { return Optional.empty(); }

        };
        CreatePostService createPost = new CreatePostService(users, posts, noProjectionChanges());

        PostView created = createPost.create(new CreatePostCommand("첫 게시글", "게시글 본문", authorId));

        assertThat(storedPost.get()).isEqualTo(Post.create("첫 게시글", "게시글 본문", authorId));
        assertThat(created.id()).isEqualTo(new PostId(42L));
        assertThat(created.authorId()).isEqualTo(authorId);
        assertThat(created.title()).isEqualTo("첫 게시글");
    }

    private static PostProjectionPort noProjectionChanges() {
        return new PostProjectionPort() {
            @Override public void synchronize(PostView post) { }
            @Override public void remove(PostId postId) { }
        };
    }
}
