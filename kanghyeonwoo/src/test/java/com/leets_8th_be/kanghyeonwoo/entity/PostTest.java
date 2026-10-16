package com.leets_8th_be.kanghyeonwoo.entity;

import com.leets_8th_be.kanghyeonwoo.config.JpaConfig;
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
class PostTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager em;

    @Test
    @DisplayName("Post를 정상적으로 생성하고 저장하면 User 연관관계와 기본 정보가 저장된다.")
    void createPostSuccess() {
        // given
        User author = User.builder()
                .username("author")
                .email("author@example.com")
                .build();
        userRepository.save(author);

        Post post = Post.builder()
                .title("테스트 제목")
                .content("테스트 본문 내용입니다.")
                .user(author)
                .build();

        // when
        Post savedPost = postRepository.save(post);
        em.flush();
        em.clear();

        // then
        Post foundPost = postRepository.findById(savedPost.getId()).orElseThrow();
        assertThat(foundPost.getId()).isNotNull();
        assertThat(foundPost.getTitle()).isEqualTo("테스트 제목");
        assertThat(foundPost.getContent()).isEqualTo("테스트 본문 내용입니다.");
        assertThat(foundPost.getUser().getId()).isEqualTo(author.getId());
        assertThat(foundPost.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("update 메서드를 호출하면 제목과 본문이 정상적으로 수정된다.")
    void updatePostSuccess() {
        // given
        User author = userRepository.save(User.builder().username("author").email("author2@example.com").build());
        Post post = postRepository.save(Post.builder().title("초기 제목").content("초기 본문").user(author).build());

        // when
        post.update("수정된 제목", "수정된 본문");
        em.flush();
        em.clear();

        // then
        Post updatedPost = postRepository.findById(post.getId()).orElseThrow();
        assertThat(updatedPost.getTitle()).isEqualTo("수정된 제목");
        assertThat(updatedPost.getContent()).isEqualTo("수정된 본문");
    }
}
