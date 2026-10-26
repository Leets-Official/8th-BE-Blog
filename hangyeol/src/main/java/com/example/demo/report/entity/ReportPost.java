package com.example.demo.report.entity;

import com.example.demo.global.entity.BaseEntity;
import com.example.demo.member.entity.Member;
import com.example.demo.post.entity.Post;
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

}
