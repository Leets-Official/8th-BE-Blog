package com.leets.mission.global.config;

import com.leets.mission.domain.member.Member;
import com.leets.mission.domain.member.MemberRepository;
import com.leets.mission.domain.member.MemberRole;
import com.leets.mission.domain.member.MemberStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Profile("local")
public class DataInitializer implements CommandLineRunner {

    // 로컬 API 테스트 전용 계정의 공통 비밀번호입니다.
    private static final String LOCAL_TEST_PASSWORD = "password123";

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (memberRepository.count() == 0) {
            memberRepository.save(Member.builder()
                    .email("author@test.com")
                    .password(passwordEncoder.encode(LOCAL_TEST_PASSWORD))
                    .nickname("작성자닉네임")
                    .name("작성자")
                    .birthDate(LocalDate.of(2000, 1, 1))
                    .role(MemberRole.ROLE_USER)
                    .status(MemberStatus.ACTIVE)
                    .build());

            memberRepository.save(Member.builder()
                    .email("other@test.com")
                    .password(passwordEncoder.encode(LOCAL_TEST_PASSWORD))
                    .nickname("타인닉네임")
                    .name("타인")
                    .birthDate(LocalDate.of(2000, 1, 1))
                    .role(MemberRole.ROLE_USER)
                    .status(MemberStatus.ACTIVE)
                    .build());

            memberRepository.save(Member.builder()
                    .email("admin@test.com")
                    .password(passwordEncoder.encode(LOCAL_TEST_PASSWORD))
                    .nickname("관리자")
                    .name("관리자")
                    .birthDate(LocalDate.of(2000, 1, 1))
                    .role(MemberRole.ROLE_ADMIN)
                    .status(MemberStatus.ACTIVE)
                    .build());
        }
    }
}
