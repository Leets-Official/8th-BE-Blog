package com.example.demo.post.entity;

import com.example.demo.comment.entity.Comment;
import com.example.demo.global.entity.BaseEntity;
import com.example.demo.global.enums.RestrictionStatus;
import com.example.demo.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Post extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false , length = 50)
    private String title;

    @Column(nullable = false , columnDefinition = "TEXT")
    private String content;

    @OneToMany(mappedBy = "post" , fetch = FetchType.LAZY)
    private List<Comment> comments= new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id",nullable=false)
    private Member member;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RestrictionStatus restrictionStatus = RestrictionStatus.NORMAL;

    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public void updateRestrictionStatus(RestrictionStatus status) {
        this.restrictionStatus = status;
    }

}
