package com.leets_8th_be.kanghyeonwoo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import com.leets_8th_be.kanghyeonwoo.exception.CustomException;
import com.leets_8th_be.kanghyeonwoo.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "reports", uniqueConstraints = @UniqueConstraint(
        name = "uk_report_reporter_target", columnNames = {"reporter_id", "target_type", "target_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Report extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, columnDefinition = "varchar(20) default 'PENDING'")
    private ReportStatus status = ReportStatus.PENDING;

    @Column(nullable = false, length = 255)
    private String reason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 20)
    private ReportTargetType targetType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Column(name = "target_title_snapshot", length = 150)
    private String targetTitleSnapshot;

    @Lob
    @Column(name = "target_content_snapshot")
    private String targetContentSnapshot;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ReportResolutionAction action;

    @Column(name = "resolution_reason", length = 255)
    private String resolutionReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processor_id")
    private User processor;

    private LocalDateTime resolvedAt;

    public void resolve(ReportResolutionAction action, String reason, User processor, LocalDateTime resolvedAt) {
        if (status != ReportStatus.PENDING) throw new CustomException(ErrorCode.REPORT_ALREADY_RESOLVED);
        this.status = ReportStatus.RESOLVED;
        this.action = action;
        this.resolutionReason = reason;
        this.processor = processor;
        this.resolvedAt = resolvedAt;
    }

    @Builder
    public Report(String reason, User reporter, ReportTargetType targetType, Long targetId,
                  String targetTitleSnapshot, String targetContentSnapshot) {
        this.reason = reason;
        this.reporter = reporter;
        this.targetType = targetType;
        this.targetId = targetId;
        this.targetTitleSnapshot = targetTitleSnapshot;
        this.targetContentSnapshot = targetContentSnapshot;
    }

    public static Report ofPost(User reporter, Post post, String reason) {
        return Report.builder()
                .reporter(reporter)
                .targetType(ReportTargetType.POST)
                .targetId(post.getId())
                .targetTitleSnapshot(post.getTitle())
                .targetContentSnapshot(post.getContent())
                .reason(reason)
                .build();
    }

    public static Report ofComment(User reporter, Comment comment, String reason) {
        return Report.builder()
                .reporter(reporter)
                .targetType(ReportTargetType.COMMENT)
                .targetId(comment.getId())
                .targetTitleSnapshot(null)
                .targetContentSnapshot(comment.getContent())
                .reason(reason)
                .build();
    }
}
