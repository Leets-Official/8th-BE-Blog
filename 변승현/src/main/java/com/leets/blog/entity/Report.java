package com.leets.blog.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "report", uniqueConstraints = {
        @UniqueConstraint(name = "uk_report_reporter_post", columnNames = {"reporter_id", "post_id"}),
        @UniqueConstraint(name = "uk_report_reporter_comment", columnNames = {"reporter_id", "comment_id"})
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Report extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id")
    private Comment comment;

    @Column(nullable = false, length = 500)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private ReportStatus status = ReportStatus.PENDING;

    private Report(User reporter, Post post, Comment comment, String reason) {
        this.reporter = reporter;
        this.post = post;
        this.comment = comment;
        this.reason = reason;
    }

    public static Report forPost(User reporter, Post post, String reason) {
        return new Report(reporter, post, null, reason);
    }

    public static Report forComment(User reporter, Comment comment, String reason) {
        return new Report(reporter, null, comment, reason);
    }

    public void resolve() {
        status = ReportStatus.RESOLVED;
    }
}
