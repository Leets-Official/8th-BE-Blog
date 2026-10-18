package com.example.demo.config;

import com.example.demo.entity.Gender;
import com.example.demo.entity.Member;
import com.example.demo.repository.MemberRepository;
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
        memberRepository.save(new Member("author1@test.com", "작성자1", "password1", "010-1111-1111",
                LocalDate.of(2003, 1, 1), Gender.F));
        memberRepository.save(new Member("author2@test.com", "작성자2", "password2", "010-2222-2222",
                LocalDate.of(2003, 2, 2), Gender.M));
    }
}