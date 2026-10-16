package com.example.demo.global.init;

import com.example.demo.entity.Member;
import com.example.demo.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

// 회원가입 API가 생기기 전까지 쓰는 테스트용 데이터
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final MemberRepository memberRepository;

    @Override
    public void run(String... args) {
        if (memberRepository.count() == 0) {
            memberRepository.save(Member.builder()
                    .email("test1@leets.com").name("테스트1").password("hashed-password").tel("01000000001")
                    .build());
            memberRepository.save(Member.builder()
                    .email("test2@leets.com").name("테스트2").password("hashed-password").tel("01000000002")
                    .build());
        }
    }
}