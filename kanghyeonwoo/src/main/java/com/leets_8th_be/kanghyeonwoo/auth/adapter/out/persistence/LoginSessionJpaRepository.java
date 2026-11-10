package com.leets_8th_be.kanghyeonwoo.auth.adapter.out.persistence;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface LoginSessionJpaRepository extends JpaRepository<LoginSessionJpaEntity, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from LoginSession s where s.id=:id")
    Optional<LoginSessionJpaEntity> lock(@Param("id") String id);
}
