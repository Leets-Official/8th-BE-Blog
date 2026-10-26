package com.leets.mission.domain.report.entity;

import com.leets.mission.domain.user.entity.User;
import com.leets.mission.global.apiPayload.code.ErrorStatus;
import com.leets.mission.global.apiPayload.exception.GeneralException;
import com.leets.mission.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 신고 (게시글/댓글 신고를 target_type + target_id로 함께 관리한다)
@Entity
@Table(
        name = "report",
        // 같은 사용자가 같은 대상을 중복 신고하지 못하도록 DB 레벨에서 막음
        uniqueConstraints = @UniqueConstraint(
                name = "uk_report_reporter_target",
                columnNames = {"reporter_id", "target_type", "target_id"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Report extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 신고자 (신고 N : 사용자 1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    // 신고 대상 종류
    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 20)
    private ReportTargetType targetType;

    // 신고 대상 ID (게시글 ID 또는 댓글 ID)
    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Column(nullable = false, length = 200)
    private String reason;

    // 신고 상태 (생성 시 PENDING)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReportStatus status;

    @Builder
    private Report(User reporter, ReportTargetType targetType, Long targetId, String reason) {
        this.reporter = reporter;
        this.targetType = targetType;
        this.targetId = targetId;
        this.reason = reason;
        this.status = ReportStatus.PENDING;
    }

    // 신고 처리 (PENDING → RESOLVED/REJECTED, 이미 처리된 신고면 409)
    public void process(ReportStatus status) {
        if (this.status != ReportStatus.PENDING) {
            throw new GeneralException(ErrorStatus.REPORT_409_2);
        }
        if (status == ReportStatus.PENDING) {
            throw new GeneralException(ErrorStatus.REPORT_400_2);
        }
        this.status = status;
    }

    // 신고가 승인되었는지 확인
    public boolean isResolved() {
        return status == ReportStatus.RESOLVED;
    }
}
