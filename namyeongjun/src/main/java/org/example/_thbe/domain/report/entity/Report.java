package org.example._thbe.domain.report.entity;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example._thbe.domain.member.entity.Member;
import org.example._thbe.domain.report.enums.ReportContentType;
import org.example._thbe.domain.report.enums.ReportStatus;
import org.example._thbe.global.entity.BaseTimeEntity;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity @Table(name = "reports")
public class Report extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long id;


    @Column(name = "reason",columnDefinition = "TEXT")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name ="status",nullable = false,length = 50)
    private ReportStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "content_type",nullable = false,length = 50)
    private ReportContentType contentType;

    @Column(name = "content_id",nullable = false)
    private Long contentId;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id",nullable = false)
    private Member reporter;





    private Report(String reason,ReportContentType contentType,Long contentId,Member reporter) {
        this.reason = reason;
        this.contentType =contentType;
        this.contentId = contentId;
        this.reporter = reporter;
        this.status = ReportStatus.PENDING;
    }

    public static Report create(String reason,ReportContentType contentType,Long contentId,Member reporter) {
        return new Report (reason, contentType ,contentId ,reporter);
    }

    public void changeStatus(ReportStatus status) {
        this.status = status;
    }


}