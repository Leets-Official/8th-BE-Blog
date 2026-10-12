package com.example.demo.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Table(name = "report")
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
