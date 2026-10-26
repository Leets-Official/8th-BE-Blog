package com.example.demo.report.entity;

import com.example.demo.comment.entity.Comment;
import com.example.demo.global.entity.BaseEntity;
import com.example.demo.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ReportComment extends BaseEntity {

    @EmbeddedId
    private ReportPostId id;

    @MapsId("commentId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id")
    private Comment comment;

    @MapsId("reporterId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id")
    private Member reporter;

    @Column(length = 500)
    private String reason;
}
