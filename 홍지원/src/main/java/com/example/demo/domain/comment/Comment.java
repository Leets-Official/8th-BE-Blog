package com.example.demo.domain.comment;

import com.example.demo.domain.common.BaseTimeEntity;
import com.example.demo.domain.member.Member;
import com.example.demo.domain.post.Post;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "comment")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseTimeEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    @OneToMany(mappedBy = "parent")
    private List<Comment> replies = new ArrayList<>();

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    public Comment(Member member, Post post, String content) {
        this(member, post, null, content);
    }

    public Comment(
        Member member,
        Post post,
        Comment parent,
        String content
    ) {
        this.member = member;
        this.post = post;
        this.parent = parent;
        this.content = content;

        member.addComment(this);
        post.addComment(this);
        
        if (parent != null) {
            parent.addReply(this);
        }
    }

    public void update(String content) {
        this.content = content;
    }

    public void addReply(Comment reply) {
        this.replies.add(reply);
    }
}