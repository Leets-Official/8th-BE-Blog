package com.example.demo.repository;

import com.example.demo.entity.Report;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ReportRepository extends JpaRepository<Report, Long> {

    boolean existsByPostIdAndReporterId(Long postId, Long reporterId);

    // 신고 처리: 두 명이 동시에 resolve 해도 한 명만 성공하도록 행 잠금
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Report r WHERE r.id = :reportId")
    Optional<Report> findByIdForUpdate(@Param("reportId") Long reportId);
}
