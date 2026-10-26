package com.example.demo.config;

import com.example.demo.entity.Comment;
import com.example.demo.entity.Gender;
import com.example.demo.entity.Member;
import com.example.demo.entity.Post;
import com.example.demo.repository.MemberRepository;
import com.example.demo.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final MemberRepository memberRepository;
    private final PostRepository postRepository;

    @Override
    public void run(String... args) {
        Member author1 = memberRepository.save(new Member("author1@test.com", "작성자1", "password1", "010-1111-1111",
                LocalDate.of(2003, 1, 1), Gender.F));
        Member author2 = memberRepository.save(new Member("author2@test.com", "작성자2", "password2", "010-2222-2222",
                LocalDate.of(2003, 2, 2), Gender.M));

        Post post = new Post("테스트 게시글", "신고 기능 테스트용 게시글입니다.", author1);
        new Comment("테스트 댓글입니다.", post, author2);
        postRepository.save(post);
    }
}