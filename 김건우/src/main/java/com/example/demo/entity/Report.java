package com.example.demo.entity;

import com.example.demo.global.CustomException;
import com.example.demo.global.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "report",
        // 중복 신고 방지의 최종 방어선: 동시 요청이 와도 DB 가 막아준다
        uniqueConstraints = @UniqueConstraint(
                name = "uk_report_post_reporter",
                columnNames = {"post_id", "reporter_id"})
)
public class Report extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    @Column(nullable = false, length = 500)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReportStatus status = ReportStatus.PENDING;

    private LocalDateTime resolvedAt;

    @Builder
    public Report(Post post, User reporter, String reason) {
        this.post = post;
        this.reporter = reporter;
        this.reason = reason;
    }

    // PENDING -> RESOLVED. 이미 처리된 신고는 409
    public void resolve() {
        if (this.status == ReportStatus.RESOLVED) {
            throw new CustomException(ErrorCode.REPORT_ALREADY_RESOLVED);
        }
        this.status = ReportStatus.RESOLVED;
        this.resolvedAt = LocalDateTime.now();
    }
}
