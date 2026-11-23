package com.leets_8th_be.kanghyeonwoo.auth.adapter.out.persistence;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface OAuthGrantJpaRepository extends JpaRepository<OAuthGrantJpaEntity, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from OAuthTicket t where t.hash=:hash")
    Optional<OAuthGrantJpaEntity> lock(@Param("hash") String hash);
}
