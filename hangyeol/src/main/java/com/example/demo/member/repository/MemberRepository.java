package com.example.demo.member.repository;

import com.example.demo.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member,Long> {
    boolean existsByEmail(String email);
    boolean existsByName(String name);
    Optional<Member> findByEmail(String email);
}
