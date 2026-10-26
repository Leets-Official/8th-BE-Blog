package com.leets.blog.domain.report.entity;

import com.leets.blog.common.entity.BaseEntity;
import com.leets.blog.common.exception.CustomException;
import com.leets.blog.common.exception.ErrorCode;
import com.leets.blog.domain.comment.entity.Comment;
import com.leets.blog.domain.member.entity.Member;
import com.leets.blog.domain.post.entity.Post;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Check;

import java.time.LocalDateTime;

// 같은 회원은 같은 대상을 한 번만 신고 가능
// 신고 대상은 게시글·댓글 중 정확히 하나만 (DB는 CHECK 제약으로 보장)
@Entity
@Table(name = "reports",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_reports_reporter_post", columnNames = {"reporter_id", "post_id"}),
                @UniqueConstraint(name = "uk_reports_reporter_comment", columnNames = {"reporter_id", "comment_id"})
        })
@Getter
@Check(name = "ck_reports_target",
        constraints = "(post_id IS NULL AND comment_id IS NOT NULL) OR (post_id IS NOT NULL AND comment_id IS NULL)")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Report extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reporter_id", nullable = false)
    private Member reporter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id")
    private Comment comment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportReason reason;

    @Column(length = 500)
    private String detail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus status;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Builder(access = AccessLevel.PRIVATE)
    private Report(Member reporter, Post post, Comment comment, ReportReason reason, String detail) {
        this.reporter = reporter;
        this.post = post;
        this.comment = comment;
        this.reason = reason;
        this.detail = detail;
        this.status = ReportStatus.PENDING;
    }

    public static Report ofPost(Member reporter, Post post, ReportReason reason, String detail) {
        return Report.builder()
                .reporter(reporter)
                .post(post)
                .reason(reason)
                .detail(detail)
                .build();
    }

    public static Report ofComment(Member reporter, Comment comment, ReportReason reason, String detail) {
        return Report.builder()
                .reporter(reporter)
                .comment(comment)
                .reason(reason)
                .detail(detail)
                .build();
    }

    // PENDING → RESOLVED (이미 처리된 신고면 중복 요청으로 보고 409)
    public void resolve() {
        if (this.status != ReportStatus.PENDING) {
            throw new CustomException(ErrorCode.REPORT_ALREADY_PROCESSED);
        }
        this.status = ReportStatus.RESOLVED;
        this.processedAt = LocalDateTime.now();
    }
}
