package com.example.demo.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "post")
@Getter
@NoArgsConstructor
public class Post extends SoftDeletableEntity {

    @Column(nullable = false, length = 50)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ColumnDefault("false")
    @Column(name = "is_private", nullable = false)
    private boolean isPrivate = false;

    @ColumnDefault("false")
    @Column(name = "is_hidden", nullable = false)
    private boolean isHidden = false;

    @OneToMany(mappedBy = "post")
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "post")
    private List<Report> reports = new ArrayList<>();

    public Post(String title, String content, Member member, boolean isPrivate) {
        this.title = title;
        this.content = content;
        this.member = member;
        this.isPrivate = isPrivate;
    }

    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public void hide() {
        this.isHidden = true;
    }

    public boolean isOwnedBy(Long memberId) {
        return memberId != null && member != null && memberId.equals(member.getId());
    }
}
