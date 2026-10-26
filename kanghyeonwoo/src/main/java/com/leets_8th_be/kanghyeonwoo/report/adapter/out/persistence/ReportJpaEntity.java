package com.leets_8th_be.kanghyeonwoo.report.adapter.out.persistence;

import com.leets_8th_be.kanghyeonwoo.report.domain.model.Report;
import com.leets_8th_be.kanghyeonwoo.report.domain.model.ReportState;
import com.leets_8th_be.kanghyeonwoo.report.domain.model.ReportTarget;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

@Entity
@Table(name = "reports", uniqueConstraints = @UniqueConstraint(
        name = "uk_report_reporter_target", columnNames = {"reporter_id", "target_type", "target_id"}))
class ReportJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, columnDefinition = "varchar(20) default 'PENDING'")
    private ReportState status = ReportState.PENDING;

    @Column(nullable = false, length = 255)
    private String reason;

    @Column(name = "reporter_id", nullable = false)
    private Long reporterId;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 20)
    private ReportTarget targetType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Column(name = "target_title_snapshot", length = 150)
    private String targetTitleSnapshot;

    @Lob
    @Column(name = "target_content_snapshot")
    private String targetContentSnapshot;

    @Column(length = 20)
    private String action;

    @Column(name = "resolution_reason", length = 255)
    private String resolutionReason;

    @Column(name = "processor_id")
    private Long processorId;

    private LocalDateTime resolvedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected ReportJpaEntity() {
    }

    static ReportJpaEntity from(Report report) {
        ReportJpaEntity entity = new ReportJpaEntity();
        entity.reporterId = report.reporterId().value();
        entity.targetType = report.target();
        entity.targetId = report.targetId();
        entity.reason = report.reason();
        entity.targetTitleSnapshot = report.titleSnapshot();
        entity.targetContentSnapshot = report.contentSnapshot();
        return entity;
    }

    @PrePersist
    void initializeTimestamps() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void updateTimestamp() {
        updatedAt = LocalDateTime.now();
    }

    Long getId() { return id; }
    ReportState getStatus() { return status; }
    String getReason() { return reason; }
    ReportTarget getTargetType() { return targetType; }
    Long getTargetId() { return targetId; }
    String getTargetTitleSnapshot() { return targetTitleSnapshot; }
    String getTargetContentSnapshot() { return targetContentSnapshot; }
}
