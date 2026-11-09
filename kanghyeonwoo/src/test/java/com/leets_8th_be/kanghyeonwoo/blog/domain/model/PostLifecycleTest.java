package com.leets_8th_be.kanghyeonwoo.blog.domain.model;

import com.leets_8th_be.kanghyeonwoo.user.domain.model.UserId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PostLifecycleTest {
    private final UserId author = new UserId(10L);

    @Test
    void authorCanSoftDeleteAnActivePost() {
        Post post = Post.create("title", "content", author);

        Post deleted = post.delete(author);

        assertThat(deleted.state()).isEqualTo(PostState.DELETED);
        assertThat(deleted.title()).isEqualTo(post.title());
        assertThat(deleted.content()).isEqualTo(post.content());
    }

    @Test
    void authorCanSoftDeleteAHiddenPost() {
        Post hidden = new Post(new PostId(1L), "title", "content", author, PostState.HIDDEN);

        assertThat(hidden.delete(author).state()).isEqualTo(PostState.DELETED);
    }

    @Test
    void deletedPostCannotBeUpdatedOrDeletedAgain() {
        Post deleted = new Post(new PostId(1L), "title", "content", author, PostState.DELETED);

        assertThatThrownBy(() -> deleted.update(author, "new title", "new content"))
                .isInstanceOf(PostDeleted.class);
        assertThatThrownBy(() -> deleted.delete(author)).isInstanceOf(PostDeleted.class);
    }

    @Test
    void hidingDeletedPostDoesNotRestoreOrChangeItsState() {
        Post deleted = new Post(new PostId(1L), "title", "content", author, PostState.DELETED);

        assertThat(deleted.hide()).isSameAs(deleted);
    }
}
