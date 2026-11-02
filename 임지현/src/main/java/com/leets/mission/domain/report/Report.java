package com.leets.mission.domain.report;

import com.leets.mission.domain.comment.Comment;
import com.leets.mission.domain.member.Member;
import com.leets.mission.domain.post.Post;
import com.leets.mission.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Check;

@Entity
@Table(
        name = "report",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_report_reporter_post",
                        columnNames = {"reporter_id", "post_id"}
                ),
                @UniqueConstraint(
                        name = "uk_report_reporter_comment",
                        columnNames = {"reporter_id", "comment_id"}
                )
        }
)
@Check(
        constraints = """
                (post_id IS NOT NULL AND comment_id IS NULL)
                OR
                (post_id IS NULL AND comment_id IS NOT NULL)
                """
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Report extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private Member reporter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id")
    private Comment comment;

    @Column(name = "reason", nullable = false, length = 500)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ReportStatus status;

    @Builder
    public Report(Member reporter, Post post, Comment comment, String reason) {
        validateTarget(post, comment);

        this.reporter = reporter;
        this.post = post;
        this.comment = comment;
        this.reason = reason;
        this.status = ReportStatus.PENDING;
    }

    public void resolve() {
        validatePending();
        this.status = ReportStatus.RESOLVED;
    }

    public void dismiss() {
        validatePending();
        this.status = ReportStatus.DISMISSED;
    }

    private void validatePending() {
        if (this.status != ReportStatus.PENDING) {
            throw new IllegalStateException("대기 중인 신고만 처리할 수 있습니다.");
        }
    }

    private static void validateTarget(Post post, Comment comment) {
        boolean hasExactlyOneTarget = (post == null) != (comment == null);
        if (!hasExactlyOneTarget) {
            throw new IllegalArgumentException(
                    "신고 대상은 게시글 또는 댓글 중 하나만 지정해야 합니다."
            );
        }
    }
}