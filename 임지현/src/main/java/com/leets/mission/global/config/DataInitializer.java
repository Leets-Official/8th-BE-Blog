package com.leets.mission.global.config;

import com.leets.mission.domain.member.Member;
import com.leets.mission.domain.member.MemberRepository;
import com.leets.mission.domain.member.MemberRole;
import com.leets.mission.domain.member.MemberStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final MemberRepository memberRepository;

    @Override
    public void run(String... args) {
        if (memberRepository.count() == 0) {
            memberRepository.save(Member.builder()
                    .email("author@test.com")
                    .nickname("작성자닉네임")
                    .name("작성자")
                    .birthDate(LocalDate.of(2000, 1, 1))
                    .role(MemberRole.ROLE_USER)
                    .status(MemberStatus.ACTIVE)
                    .build());

            memberRepository.save(Member.builder()
                    .email("other@test.com")
                    .nickname("타인닉네임")
                    .name("타인")
                    .birthDate(LocalDate.of(2000, 1, 1))
                    .role(MemberRole.ROLE_USER)
                    .status(MemberStatus.ACTIVE)
                    .build());

            memberRepository.save(Member.builder()
                    .email("admin@test.com")
                    .nickname("관리자")
                    .name("관리자")
                    .birthDate(LocalDate.of(2000, 1, 1))
                    .role(MemberRole.ROLE_ADMIN)
                    .status(MemberStatus.ACTIVE)
                    .build());
        }
    }
}