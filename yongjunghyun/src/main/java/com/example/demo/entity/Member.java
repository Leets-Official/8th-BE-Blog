package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, length = 30)
    private String name;

    @Column(nullable = false, length = 255)
    private String password;            // 해시된 값을 저장한다

    @Column(nullable = false, length = 15)
    private String tel;

    private LocalDate birthDate;

    @Column(length = 1)
    private String gender;              // "M" / "F"

    @Column(name = "is_banned", nullable = false)
    private boolean banned = false;

    private LocalDateTime deletedAt;    // null이면 활성 회원

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "member")
    private List<Post> posts = new ArrayList<>();

    @OneToMany(mappedBy = "member")
    private List<Comment> comments = new ArrayList<>();

    @Builder
    public Member(String email, String name, String password,
                  String tel, LocalDate birthDate, String gender) {
        this.email = email;
        this.name = name;
        this.password = password;
        this.tel = tel;
        this.birthDate = birthDate;
        this.gender = gender;
    }
}