package com.example.demo.entity;

import com.example.demo.global.exception.CustomException;
import com.example.demo.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "report", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"reporter_id", "post_id"}),
        @UniqueConstraint(columnNames = {"reporter_id", "comment_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Report extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
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

    @Column(nullable = false)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReportStatus status;

    private Report(Member reporter, Post post, Comment comment, String reason) {
        this.reporter = reporter;
        this.post = post;
        this.comment = comment;
        this.reason = reason;
        this.status = ReportStatus.PENDING;
    }

    public static Report ofPost(Member reporter, Post post, String reason) {
        return new Report(reporter, post, null, reason);
    }

    public static Report ofComment(Member reporter, Comment comment, String reason) {
        return new Report(reporter, null, comment, reason);
    }

    public void resolve() {
        if (this.status == ReportStatus.RESOLVED) {
            throw new CustomException(ErrorCode.REPORT_ALREADY_RESOLVED);
        }
        this.status = ReportStatus.RESOLVED;
    }
}