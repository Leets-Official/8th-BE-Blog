package com.leets_8th_be.kanghyeonwoo.entity;

import com.leets_8th_be.kanghyeonwoo.config.JpaConfig;
import com.leets_8th_be.kanghyeonwoo.repository.CommentRepository;
import com.leets_8th_be.kanghyeonwoo.repository.PostRepository;
import com.leets_8th_be.kanghyeonwoo.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaConfig.class)
class CommentTest {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager em;

    @Test
    @DisplayName("Comment를 생성하고 Post에 addComment로 추가하면 연관관계가 양방향으로 동기화된다.")
    void createCommentWithPostAssociation() {
        // given
        User author = userRepository.save(User.builder().username("author").email("author@example.com").build());
        User commenter = userRepository.save(User.builder().username("commenter").email("commenter@example.com").build());
        Post post = postRepository.save(Post.builder().title("게시글 제목").content("본문").user(author).build());

        Comment comment = Comment.builder()
                .content("댓글 내용입니다.")
                .user(commenter)
                .build();

        // when
        post.addComment(comment);
        em.flush();
        em.clear();

        // then
        Post foundPost = postRepository.findById(post.getId()).orElseThrow();
        assertThat(foundPost.getComments()).hasSize(1);
        Comment foundComment = foundPost.getComments().get(0);
        assertThat(foundComment.getContent()).isEqualTo("댓글 내용입니다.");
        assertThat(foundComment.getUser().getId()).isEqualTo(commenter.getId());
        assertThat(foundComment.getPost().getId()).isEqualTo(post.getId());
    }

    @Test
    @DisplayName("Post가 삭제되면 영속성 전이(CascadeType.ALL) 및 orphanRemoval에 의해 연관된 Comment도 함께 삭제된다.")
    void deletePostCascadesToComments() {
        // given
        User author = userRepository.save(User.builder().username("author").email("author3@example.com").build());
        Post post = Post.builder().title("삭제될 글").content("본문").user(author).build();
        Comment comment = Comment.builder().content("삭제될 댓글").user(author).build();
        post.addComment(comment);

        Post savedPost = postRepository.save(post);
        em.flush();
        em.clear();

        Long commentId = savedPost.getComments().get(0).getId();

        // when
        postRepository.deleteById(savedPost.getId());
        em.flush();
        em.clear();

        // then
        assertThat(postRepository.findById(savedPost.getId())).isEmpty();
        assertThat(commentRepository.findById(commentId)).isEmpty();
    }

    @Test
    @DisplayName("댓글에 대댓글을 추가하면 계층형 자기참조 관계(parent-children)가 양방향으로 동기화된다.")
    void addReplyCommentSuccess() {
        // given
        User author = userRepository.save(User.builder().username("author").email("author4@example.com").build());
        User replyAuthor = userRepository.save(User.builder().username("replyUser").email("reply@example.com").build());
        Post post = postRepository.save(Post.builder().title("게시글").content("내용").user(author).build());

        Comment parent = Comment.builder().content("부모 댓글").user(author).post(post).build();
        commentRepository.save(parent);

        Comment child = Comment.builder().content("자식 대댓글").user(replyAuthor).build();

        // when
        parent.addChildComment(child);
        commentRepository.save(child);
        em.flush();
        em.clear();

        // then
        Comment foundParent = commentRepository.findById(parent.getId()).orElseThrow();
        assertThat(foundParent.getChildren()).hasSize(1);
        Comment foundChild = foundParent.getChildren().get(0);
        assertThat(foundChild.getContent()).isEqualTo("자식 대댓글");
        assertThat(foundChild.getParent().getId()).isEqualTo(parent.getId());
        assertThat(foundChild.getPost().getId()).isEqualTo(post.getId());
    }

    @Test
    @DisplayName("부모 댓글 delete() 호출 시 isDeleted=true로 마스킹되며 하위 대댓글은 정상 보존된다.")
    void deleteParentCommentMasksAndPreservesChildren() {
        // given
        User author = userRepository.save(User.builder().username("author").email("author5@example.com").build());
        Post post = postRepository.save(Post.builder().title("글").content("글").user(author).build());

        Comment parent = Comment.builder().content("삭제될 원본 댓글").user(author).post(post).build();
        Comment child = Comment.builder().content("남겨질 대댓글").user(author).post(post).build();
        parent.addChildComment(child);

        commentRepository.save(parent);
        commentRepository.save(child);
        em.flush();
        em.clear();

        // when
        Comment foundParent = commentRepository.findById(parent.getId()).orElseThrow();
        foundParent.delete();
        em.flush();
        em.clear();

        // then
        Comment maskedParent = commentRepository.findById(parent.getId()).orElseThrow();
        assertThat(maskedParent.isDeleted()).isTrue();
        assertThat(maskedParent.getChildren()).hasSize(1);
        assertThat(maskedParent.getChildren().get(0).getContent()).isEqualTo("남겨질 대댓글");
    }
}
