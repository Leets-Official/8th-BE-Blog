package com.example.demo.domain.member;

import com.example.demo.domain.comment.Comment;
import com.example.demo.domain.common.BaseTimeEntity;
import com.example.demo.domain.post.Post;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "member")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 기존 이름 필드를 닉네임으로 사용해 게시글의 작성자 표시를 유지합니다.
    @Column(nullable = false, length = 50, unique = true)
    private String name;

    private Byte age;

    @Column(nullable = false, length = 50, unique = true)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    // 가입자는 일반 회원이며, 재발급 토큰은 원문 대신 해시만 저장합니다.
    @Column(nullable = false, length = 10)
    private String role = "USER";

    @Column(length = 64)
    private String refreshTokenHash;

    private LocalDateTime refreshExpiresAt;

    @OneToMany(mappedBy = "member")
    private List<Post> posts = new ArrayList<>();

    @OneToMany(mappedBy = "member")
    private List<Comment> comments = new ArrayList<>();

    public Member(String name, Byte age, String email, String password) {
        this.name = name;
        this.age = age;
        this.email = email;
        this.password = password;
    }

    public void updateProfile(String name, Byte age) {
        this.name = name;
        this.age = age;
    }

    public void updateRefreshToken(String hash, LocalDateTime expiresAt) {
        this.refreshTokenHash = hash;
        this.refreshExpiresAt = expiresAt;
    }

    public void addPost(Post post) {
        this.posts.add(post);
    }

    public void addComment(Comment comment) {
        this.comments.add(comment);
    }
}
