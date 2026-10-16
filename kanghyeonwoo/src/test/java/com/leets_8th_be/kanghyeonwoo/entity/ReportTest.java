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
    @DisplayName("게시글 신고(Report.ofPost) 시 논리 참조(targetType=POST, targetId)와 제목/본문 스냅샷이 정상 보존된다.")
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
        assertThat(foundReport.getTargetType()).isEqualTo(ReportTargetType.POST);
        assertThat(foundReport.getTargetId()).isEqualTo(post.getId());
        assertThat(foundReport.getTargetTitleSnapshot()).isEqualTo("부적절한 글");
        assertThat(foundReport.getTargetContentSnapshot()).isEqualTo("광고 스팸");
        assertThat(foundReport.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("댓글 신고(Report.ofComment) 시 논리 참조(targetType=COMMENT, targetId)와 댓글 본문 스냅샷이 정상 보존된다.")
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
        assertThat(foundReport.getTargetType()).isEqualTo(ReportTargetType.COMMENT);
        assertThat(foundReport.getTargetId()).isEqualTo(comment.getId());
        assertThat(foundReport.getTargetTitleSnapshot()).isNull();
        assertThat(foundReport.getTargetContentSnapshot()).isEqualTo("욕설 댓글");
    }
}
