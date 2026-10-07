package com.leets.mission.domain.member;

import com.leets.mission.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    /**
     * [확장성 설계] 소셜 로그인(OAuth) 도입 가능성을 고려하여 Nullable 허용.
     * - 현 단계: 필드 선언 및 기본 빌더 매핑만 진행.
     * - 추후 구현: Spring Security 및 PasswordEncoder(BCrypt) 기반 암호화/검증 로직 적용 예정.
     */
    @Column(name = "password", length = 255)
    private String password;

    @Column(name = "nickname", nullable = false, length = 50)
    private String nickname;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    /**
     * [확장성 설계] 사용자 권한 분리 (ROLE_USER, ROLE_ADMIN).
     * - 현 단계: Enum 필드 선언 및 기본값(ROLE_USER) 매핑.
     * - 추후 구현: Security Interceptor / Method Security 기반 권한 인가 로직 적용 예정.
     */
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