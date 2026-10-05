package com.example.demo.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.SQLRestriction;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "post")
@SQLRestriction("deleted_at IS NULL")
public class Post extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50, nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "is_private", nullable = false)
    private boolean isPrivate = false;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "post")
    private List<Comment> comments = new ArrayList<>();

    protected Post() {
    }

    public Post(String title, String content, boolean isPrivate, User user) {
        this.title = title;
        this.content = content;
        this.isPrivate = isPrivate;
        this.user = user;
    }

    public void update(String title, String content, boolean isPrivate) {
        this.title = title;
        this.content = content;
        this.isPrivate = isPrivate;
    }

}
