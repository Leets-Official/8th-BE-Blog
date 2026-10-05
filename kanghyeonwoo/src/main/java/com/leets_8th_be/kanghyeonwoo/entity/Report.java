package com.leets_8th_be.kanghyeonwoo.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "reports")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Report extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String reason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id")
    private Comment comment;

    @Builder
    public Report(String reason, User reporter, Post post, Comment comment) {
        this.reason = reason;
        this.reporter = reporter;
        this.post = post;
        this.comment = comment;
    }

    public static Report ofPost(User reporter, Post post, String reason) {
        return Report.builder()
                .reporter(reporter)
                .post(post)
                .reason(reason)
                .build();
    }

    public static Report ofComment(User reporter, Comment comment, String reason) {
        return Report.builder()
                .reporter(reporter)
                .comment(comment)
                .reason(reason)
                .build();
    }
}
