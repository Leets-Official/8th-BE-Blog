package com.leets_8th_be.kanghyeonwoo.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Builder
    public User(String username, String email) {
        this.username = username;
        this.email = email;
    }

    public static User create(String username, String email) {
        return User.builder()
                .username(username)
                .email(email)
                .build();
    }
}
