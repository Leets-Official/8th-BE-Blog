package org.example._thbe.domain.post.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example._thbe.domain.member.entity.Member;
import org.example._thbe.global.entity.BaseTimeEntity;

@Entity @Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long id;

    @Column(name = "title",length = 255,nullable = false)
    private String title;

    @Lob
    @Column(name = "content",nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id",nullable = false)
    private Member member;

    private Post(String title,String content,Member member) {
        this.title = title;
        this.content = content;
        this.member =member;
    }

    public static Post create(String title,String content,Member member) {
        return new Post(title,content,member);
    }


    public void update(String title,String content) {
        this.title = title;
        this.content = content;
    }
}
