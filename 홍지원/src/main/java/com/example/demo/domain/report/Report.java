package com.example.demo.domain.report;

import com.example.demo.domain.comment.Comment;
import com.example.demo.domain.common.BaseTimeEntity;
import com.example.demo.domain.member.Member;
import com.example.demo.domain.post.Post;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
public class Report extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reporter_id", nullable = false)
    private Member reporter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", foreignKey = @ForeignKey(name = "fk_report_post"))
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", foreignKey = @ForeignKey(name = "fk_report_comment"))
    private Comment comment;

    @Column(nullable = false, length = 1000)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private ReportStatus status = ReportStatus.PENDING;

    @Column(length = 1000)
    private String result;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolved_by_id")
    private Member resolvedBy;

    public Report(Member reporter, Post post, Comment comment, String reason) {
        this.reporter = reporter;
        this.post = post;
        this.comment = comment;
        this.reason = reason;
        this.status = ReportStatus.PENDING;
    }

    public void resolve(Member resolver, String result) {
        this.status = ReportStatus.RESOLVED;
        this.resolvedBy = resolver;
        this.result = result;
    }
}
