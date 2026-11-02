package com.example.demo.entity;

import com.example.demo.global.CustomException;
import com.example.demo.global.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    // [4주차] 게시글 상태: ACTIVE(기본) / HIDDEN
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PostStatus status = PostStatus.ACTIVE;

    @OneToMany(mappedBy = "post", cascade = CascadeType.REMOVE)
    private List<Comment> comments = new ArrayList<>();

    @Builder
    public Post(User user, String title, String content) {
        this.user = user;
        this.title = title;
        this.content = content;
    }

    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public boolean isHidden() {
        return this.status == PostStatus.HIDDEN;
    }

    // ACTIVE -> HIDDEN. 이미 숨김이면 409
    public void hide() {
        if (isHidden()) {
            throw new CustomException(ErrorCode.POST_ALREADY_HIDDEN);
        }
        this.status = PostStatus.HIDDEN;
    }
}
