package com.leets.blog.domain.member.repository;

import com.leets.blog.domain.member.entity.SocialAccount;
import com.leets.blog.domain.member.entity.SocialProvider;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SocialAccountRepository extends JpaRepository<SocialAccount, Long> {

    // 연결된 회원도 바로 쓰므로 함께 조회
    @EntityGraph(attributePaths = "member")
    Optional<SocialAccount> findByProviderAndProviderId(SocialProvider provider, String providerId);
}
