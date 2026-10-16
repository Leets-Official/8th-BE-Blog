package com.leets.choyeji.repository;

import com.leets.choyeji.entity.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findFirstByEmailOrderByIdAsc(String email);
}
