package com.leets.blog.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, length = 30)
    private String name;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 15)
    private String tel;

    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column(length = 1)
    private Gender gender;

    @Column(name = "is_banned", nullable = false)
    private boolean banned = false;

    private LocalDateTime deletedAt;

    @Builder
    private Member(String email, String name, String password, String tel, LocalDate birthDate, Gender gender) {
        this.email = email;
        this.name = name;
        this.password = password;
        this.tel = tel;
        this.birthDate = birthDate;
        this.gender = gender;
    }
}
