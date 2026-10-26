package com.example.demo.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Table(name = "report", uniqueConstraints = {
        @UniqueConstraint(name = "uk_report_reporter_post", columnNames = {"reporter_id", "post_id"}),
        @UniqueConstraint(name = "uk_report_reporter_comment", columnNames = {"reporter_id", "comment_id"})
})
@Check(constraints = "(post_id IS NOT NULL AND comment_id IS NULL) OR (post_id IS NULL AND comment_id IS NOT NULL)")
@Getter
@NoArgsConstructor
public class Report extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private Member reporter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id")
    private Comment comment;

    @Column(nullable = false)
    private String reason;

    @ColumnDefault("'PENDING'")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReportStatus status = ReportStatus.PENDING;

    @PrePersist
    @PreUpdate
    private void validateReportTarget() {
        if (getReportTargetCount() != 1) {
            throw new IllegalArgumentException("신고 대상은 게시글 또는 댓글 중 하나만 존재해야 합니다.");
        }
    }

    public static Report forPost(Member reporter, Post post, String reason) {
        Report report = new Report();
        report.reporter = reporter;
        report.post = post;
        report.reason = reason;
        return report;
    }

    public static Report forComment(Member reporter, Comment comment, String reason) {
        Report report = new Report();
        report.reporter = reporter;
        report.comment = comment;
        report.reason = reason;
        return report;
    }

    private int getReportTargetCount() {
        int targetCount = 0;

        if (post != null) {
            targetCount++;
        }

        if (comment != null) {
            targetCount++;
        }

        return targetCount;
    }
}
