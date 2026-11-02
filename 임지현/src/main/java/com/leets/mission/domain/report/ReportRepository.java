package com.leets.mission.domain.report;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ReportRepository extends JpaRepository<Report, Long> {

    boolean existsByReporterIdAndPostId(Long reporterId, Long postId);

    boolean existsByReporterIdAndCommentId(Long reporterId, Long commentId);

    Page<Report> findAllByStatus(ReportStatus status, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Report r where r.id = :reportId")
    Optional<Report> findByIdForUpdate(@Param("reportId") Long reportId);
}