package com.example.demo.domain.report.entity;

import com.example.demo.domain.comment.entity.Comment;
import com.example.demo.domain.member.entity.Member;
import com.example.demo.domain.post.entity.Post;
import com.example.demo.global.common.entity.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Entity
@Table(
    name = "report",
    indexes = {
        @Index(name = "IDX_REPORT_POST", columnList = "post_id"),
        @Index(name = "IDX_REPORT_COMMENT", columnList = "comment_id")
    }
)
@Check(
    name = "CK_REPORT_ONE_TARGET",
    constraints = "(post_id IS NOT NULL AND comment_id IS NULL) OR (post_id IS NULL AND comment_id IS NOT NULL)"
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Report extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 신고한 사용자
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false, foreignKey = @ForeignKey(name = "FK_REPORT_MEMBER"))
    private Member member;

    // 게시글 신고 대상
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", foreignKey = @ForeignKey(name = "FK_REPORT_POST"))
    private Post post;

    // 댓글 신고 대상
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", foreignKey = @ForeignKey(name = "FK_REPORT_COMMENT"))
    private Comment comment;

    // 신고 사유
    @Column(nullable = false, length = 500)
    private String reason;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private ReportStatus status = ReportStatus.PENDING;

    private Report(Member member, Post post, Comment comment, String reason) {
        this.member = member;
        this.post = post;
        this.comment = comment;
        this.reason = reason;
    }

    public static Report ofPost(Member member, Post post, String reason) {
        return new Report(member, post, null, reason);
    }

    public static Report ofComment(Member member, Comment comment, String reason) {
        return new Report(member, null, comment, reason);
    }
}
