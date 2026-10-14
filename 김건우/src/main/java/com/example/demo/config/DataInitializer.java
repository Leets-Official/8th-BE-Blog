package com.example.demo.config;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

// 개발/테스트용 초기 데이터. 회원 기능이 생기면 제거
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;

    @Override
    public void run(String... args) {
        userRepository.save(User.builder()
                .password("test-password").nickname("tester1").email("tester1@test.com").build());
        userRepository.save(User.builder()
                .password("test-password").nickname("tester2").email("tester2@test.com").build());
    }
}