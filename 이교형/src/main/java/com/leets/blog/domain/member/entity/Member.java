package com.leets.blog.domain.member.entity;

import com.leets.blog.common.entity.BaseEntity;
import com.leets.blog.domain.comment.entity.Comment;
import com.leets.blog.domain.post.entity.Post;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "members", uniqueConstraints = {
        @UniqueConstraint(name = Member.UK_EMAIL, columnNames = "email"),
        @UniqueConstraint(name = Member.UK_NICKNAME, columnNames = "nickname")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {

    // 동시 가입으로 DB unique 제약에 걸렸을 때 어느 쪽이 중복인지 구분하는 데 사용
    public static final String UK_EMAIL = "uk_members_email";
    public static final String UK_NICKNAME = "uk_members_nickname";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 게시글·댓글 작성자로 공개되는 이름
    @Column(nullable = false, length = 20)
    private String nickname;

    // 카카오 회원은 이메일 동의를 안 했으면 null
    @Column(length = 50)
    private String email;

    // BCrypt 해시, 카카오 회원은 null
    @Column
    private String password;

    @Enumerated(EnumType.STRING)
    @ColumnDefault("'MEMBER'")
    @Column(nullable = false)
    private MemberRole role;

    @OneToMany(mappedBy = "member")
    private List<Post> posts = new ArrayList<>();

    @OneToMany(mappedBy = "member")
    private List<Comment> comments = new ArrayList<>();

    private Member(String email, String password, String nickname) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.role = MemberRole.MEMBER;
    }

    // 이메일 회원가입 (password는 BCrypt로 해시한 값)
    public static Member createLocal(String email, String encodedPassword, String nickname) {
        return new Member(email, encodedPassword, nickname);
    }

    // 카카오 최초 로그인 시 자동 가입 (비밀번호 없음, 이메일은 없을 수 있음)
    public static Member createSocial(String email, String nickname) {
        return new Member(email, null, nickname);
    }

    // 카카오로만 가입한 회원은 이메일·비밀번호 로그인 불가
    public boolean hasPassword() {
        return this.password != null;
    }

    public void delete() {
        softDelete();
    }
}
