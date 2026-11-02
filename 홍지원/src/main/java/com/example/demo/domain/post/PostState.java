package com.example.demo.domain.post;

import com.example.demo.domain.comment.Comment;
import com.example.demo.domain.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "post_state", uniqueConstraints = @UniqueConstraint(name = "uk_post_state_accepted_comment", columnNames = "accepted_comment_id"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostState extends BaseTimeEntity {
    @Id
    private Long postId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private PostVisibility visibility = PostVisibility.ACTIVE;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accepted_comment_id", unique = true)
    private Comment acceptedComment;

    public PostState(Post post) {
        this.post = post;
        this.visibility = PostVisibility.ACTIVE;
    }

    public void changeVisibility(PostVisibility visibility) {
        this.visibility = visibility;
    }

    public void accept(Comment comment) {
        this.acceptedComment = comment;
    }

    public void clearAcceptedComment() {
        this.acceptedComment = null;
    }
}
