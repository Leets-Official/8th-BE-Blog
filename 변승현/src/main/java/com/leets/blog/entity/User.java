package com.leets.blog.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "member", uniqueConstraints = {
        @jakarta.persistence.UniqueConstraint(name = "uk_member_email", columnNames = "email"),
        @jakarta.persistence.UniqueConstraint(name = "uk_member_nickname", columnNames = "nickname")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 254)
    private String email;

    @Column(length = 100)
    private String password;

    @Column(length = 50)
    private String nickname;

    public User(String email, String password, String nickname) {
        this.name = nickname;
        this.email = email;
        this.password = password;
        this.nickname = nickname;
    }

    public User(String name) {
        this.name = Objects.requireNonNull(name, "사용자 이름은 필수입니다.");
    }
}
