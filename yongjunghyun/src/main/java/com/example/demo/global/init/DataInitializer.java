package com.example.demo.global.init;

import com.example.demo.entity.Member;
import com.example.demo.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

// 로컬 테스트용 회원 2명 (비밀번호: password1234)
// 6주차부터는 회원가입 API로도 회원을 만들 수 있고, 비밀번호는 해시로 저장한다
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (memberRepository.count() == 0) {
            String password = passwordEncoder.encode("password1234");
            memberRepository.save(Member.builder()
                    .email("test1@leets.com").name("테스트1").nickname("tester1").password(password).tel("01000000001")
                    .build());
            memberRepository.save(Member.builder()
                    .email("test2@leets.com").name("테스트2").nickname("tester2").password(password).tel("01000000002")
                    .build());
        }
    }
}