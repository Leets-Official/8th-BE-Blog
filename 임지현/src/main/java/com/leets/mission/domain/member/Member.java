package com.leets.mission.domain.member;

import com.leets.mission.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;
import java.time.LocalDate;

@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "email", unique = true, length = 255)
    private String email;


    @Column(nullable = false, name = "password", length = 255)
    private String password;

    @Column(nullable = false, name = "nickname", length = 50)
    private String nickname;

    @Column(name = "name", length = 50)
    private String name;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private MemberRole role;

    /**
     * [확장성 설계] 회원 상태 관리 (ACTIVE, SUSPENDED, DORMANT, WITHDRAWN).
     * - 현 단계: Enum 필드 선언 및 기본값(ACTIVE) 매핑.
     * - 추후 구현: 로그인 제어, 차단, 휴면 처리 비즈니스 로직 적용 예정.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private MemberStatus status;

    @Builder
    public Member(String email, String password, String nickname, String name, LocalDate birthDate, MemberRole role, MemberStatus status) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.name = name;
        this.birthDate = birthDate;
        this.role = role != null ? role : MemberRole.ROLE_USER;
        this.status = status != null ? status : MemberStatus.ACTIVE;
    }
}