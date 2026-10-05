package com.leets_8th_be.kanghyeonwoo.entity;

import com.leets_8th_be.kanghyeonwoo.config.JpaConfig;
import com.leets_8th_be.kanghyeonwoo.repository.CommentRepository;
import com.leets_8th_be.kanghyeonwoo.repository.PostRepository;
import com.leets_8th_be.kanghyeonwoo.repository.ReportRepository;
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
class ReportTest {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private TestEntityManager em;

    @Test
    @DisplayName("게시글 신고(Report.ofPost)가 정상적으로 생성되고 대상 Post와 Reporter가 매핑된다.")
    void reportPostSuccess() {
        // given
        User reporter = userRepository.save(User.builder().username("reporter").email("reporter@example.com").build());
        User author = userRepository.save(User.builder().username("author").email("author6@example.com").build());
        Post post = postRepository.save(Post.builder().title("부적절한 글").content("광고 스팸").user(author).build());

        // when
        Report report = Report.ofPost(reporter, post, "스팸 및 광고 게시글입니다.");
        Report savedReport = reportRepository.save(report);
        em.flush();
        em.clear();

        // then
        Report foundReport = reportRepository.findById(savedReport.getId()).orElseThrow();
        assertThat(foundReport.getReason()).isEqualTo("스팸 및 광고 게시글입니다.");
        assertThat(foundReport.getReporter().getId()).isEqualTo(reporter.getId());
        assertThat(foundReport.getPost().getId()).isEqualTo(post.getId());
        assertThat(foundReport.getComment()).isNull();
        assertThat(foundReport.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("댓글 신고(Report.ofComment)가 정상적으로 생성되고 대상 Comment와 Reporter가 매핑된다.")
    void reportCommentSuccess() {
        // given
        User reporter = userRepository.save(User.builder().username("reporter2").email("reporter2@example.com").build());
        User author = userRepository.save(User.builder().username("author7").email("author7@example.com").build());
        Post post = postRepository.save(Post.builder().title("글").content("글").user(author).build());
        Comment comment = commentRepository.save(Comment.builder().content("욕설 댓글").user(author).post(post).build());

        // when
        Report report = Report.ofComment(reporter, comment, "부적절한 언어 사용");
        Report savedReport = reportRepository.save(report);
        em.flush();
        em.clear();

        // then
        Report foundReport = reportRepository.findById(savedReport.getId()).orElseThrow();
        assertThat(foundReport.getReason()).isEqualTo("부적절한 언어 사용");
        assertThat(foundReport.getReporter().getId()).isEqualTo(reporter.getId());
        assertThat(foundReport.getComment().getId()).isEqualTo(comment.getId());
        assertThat(foundReport.getPost()).isNull();
    }
}
