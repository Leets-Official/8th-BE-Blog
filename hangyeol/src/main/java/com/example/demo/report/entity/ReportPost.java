package com.example.demo.report.entity;

import com.example.demo.global.apiPayload.exception.GeneralException;
import com.example.demo.global.entity.BaseEntity;
import com.example.demo.member.entity.Member;
import com.example.demo.post.entity.Post;
import com.example.demo.report.enums.ReportStatus;
import com.example.demo.report.exception.ReportErrorCode;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "post_reports")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ReportPost extends BaseEntity {
    @EmbeddedId
    private ReportPostId id;

    @MapsId("postId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @MapsId("reporterId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id")
    private Member reporter;

    @Column(length = 500)
    private String reason;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReportStatus status = ReportStatus.PENDING;

    public void updateStatus(ReportStatus newStatus) {
        if (this.status != ReportStatus.PENDING) {
            throw new GeneralException(ReportErrorCode.ALREADY_PROCESSED);
        }

        if (newStatus == ReportStatus.PENDING) {
            throw new GeneralException(ReportErrorCode.INVALID_REPORT_STATUS);
        }

        this.status = newStatus;
    }
}
