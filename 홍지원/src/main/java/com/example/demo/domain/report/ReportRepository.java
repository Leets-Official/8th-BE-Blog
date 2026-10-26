package com.example.demo.domain.report;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReportRepository extends JpaRepository<Report, Long> {
    boolean existsByReporterIdAndPostId(Long reporterId, Long postId);
    boolean existsByReporterIdAndCommentId(Long reporterId, Long commentId);

    List<Report> findAllByStatusOrderByCreatedAtAsc(ReportStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Report r where r.id = :id")
    Optional<Report> findByIdForUpdate(@Param("id") Long id);
}
