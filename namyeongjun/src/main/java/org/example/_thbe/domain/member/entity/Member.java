package org.example._thbe.domain.member.entity;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example._thbe.global.entity.BaseTimeEntity;

@Entity @Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Column(name = "nickname",length = 50,nullable = false, unique = true)
    private String nickname;

    @Column(name = "password",length = 100,nullable = false)
    private String password;

    @Column(name = "email",length = 255, nullable = false, unique = true)
    private String email;
}
