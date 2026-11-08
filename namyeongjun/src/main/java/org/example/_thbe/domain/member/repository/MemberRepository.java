package org.example._thbe.domain.member.repository;

import org.example._thbe.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member,Long> {

    boolean existsByEmail(String email);
    Optional<Member> findByEmail(String email);


    @Modifying(clearAutomatically = true)
    @Query(value = "DELETE FROM member WHERE deleted_at <= :expiredTime LIMIT :batchSize",nativeQuery = true)
    int hardDeleteExpiredUser(@Param("expiredTime") LocalDateTime expiredTime, @Param("batchSize") int batchSize);
}
