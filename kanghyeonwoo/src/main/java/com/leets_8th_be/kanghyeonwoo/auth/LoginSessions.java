package com.leets_8th_be.kanghyeonwoo.auth;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

interface LoginSessions extends JpaRepository<LoginSession, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from LoginSession s where s.id=:id")
    Optional<LoginSession> lock(@Param("id") String id);
}
