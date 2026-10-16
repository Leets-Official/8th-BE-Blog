package com.example.demo.member.entity;

import com.example.demo.comment.entity.Comment;
import com.example.demo.global.entity.BaseEntity;
import com.example.demo.post.entity.Post;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false , unique = true)
    private String name;

    @OneToMany(mappedBy = "member" , fetch = FetchType.LAZY)
    private List<Post> posts;

    @OneToMany(mappedBy = "member" , fetch = FetchType.LAZY)
    private List<Comment> comments;
}
