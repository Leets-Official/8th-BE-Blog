package com.example.demo.config;

import com.example.demo.entity.Comment;
import com.example.demo.entity.Member;
import com.example.demo.entity.Post;
import com.example.demo.repository.MemberRepository;
import com.example.demo.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final MemberRepository memberRepository;
    private final PostRepository postRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        Member author1 = memberRepository.save(
                Member.signUp("author1@test.com", "작성자1", passwordEncoder.encode("password1")));
        Member author2 = memberRepository.save(
                Member.signUp("author2@test.com", "작성자2", passwordEncoder.encode("password2")));

        Post post = new Post("테스트 게시글", "신고 기능 테스트용 게시글입니다.", author1);
        new Comment("테스트 댓글입니다.", post, author2);
        postRepository.save(post);
    }
}