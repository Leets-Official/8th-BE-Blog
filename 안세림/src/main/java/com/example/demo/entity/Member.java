package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
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

    public Member(String email, String name, String password, String tel, LocalDate birthDate, Gender gender) {
        this.email = email;
        this.name = name;
        this.password = password;
        this.tel = tel;
        this.birthDate = birthDate;
        this.gender = gender;
    }
}