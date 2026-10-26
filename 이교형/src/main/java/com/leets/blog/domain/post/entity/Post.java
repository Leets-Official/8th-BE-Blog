package com.leets.blog.domain.post.entity;

import com.leets.blog.common.entity.BaseEntity;
import com.leets.blog.common.exception.CustomException;
import com.leets.blog.common.exception.ErrorCode;
import com.leets.blog.domain.comment.entity.Comment;
import com.leets.blog.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "posts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, columnDefinition = "MEDIUMTEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @ColumnDefault("'ACTIVE'")
    @Column(nullable = false)
    private PostStatus status;

    // 낙관적 락: 작성자 수정과 관리자 숨김이 겹칠 때 수정이 status를 ACTIVE로 덮어써 숨김이 풀리는 것을 방지
    @Version
    @ColumnDefault("0")
    @Column(nullable = false)
    private Long version;

    @OneToMany(mappedBy = "post")
    private List<Comment> comments = new ArrayList<>();

    @Builder
    private Post(Member member, String title, String content) {
        this.member = member;
        this.title = title;
        this.content = content;
        this.status = PostStatus.ACTIVE;
    }

    // null인 필드는 바꾸지 않음 (PATCH: 보낸 필드만 수정)
    public void update(String title, String content) {
        if (title != null) {
            this.title = title;
        }
        if (content != null) {
            this.content = content;
        }
    }

    public void delete() {
        softDelete();
    }

    // ACTIVE → HIDDEN (이미 숨김이면 중복 요청으로 보고 409)
    public void hide() {
        if (isHidden()) {
            throw new CustomException(ErrorCode.ALREADY_HIDDEN);
        }
        this.status = PostStatus.HIDDEN;
    }

    public boolean isHidden() {
        return this.status == PostStatus.HIDDEN;
    }

    // 작성자 본인인지 확인 (수정·삭제 권한 체크)
    public boolean isWrittenBy(Long memberId) {
        return this.member.getId().equals(memberId);
    }

}
