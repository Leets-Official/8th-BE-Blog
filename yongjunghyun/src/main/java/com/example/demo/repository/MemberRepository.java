package com.example.demo.repository;

import com.example.demo.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    // 메서드 이름만으로 쿼리가 만들어진다 (Spring Data JPA 메서드 이름 쿼리)
    boolean existsByEmail(String email);        // select count(*) > 0 ... where email = ?

    boolean existsByNickname(String nickname);

    Optional<Member> findByEmail(String email); // 로그인할 때 이메일로 회원을 찾는다
}
