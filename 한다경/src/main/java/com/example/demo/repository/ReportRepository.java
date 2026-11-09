package com.example.demo.repository;
import com.example.demo.domain.Report;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
public interface ReportRepository extends JpaRepository<Report, Long> {
    boolean existsByReporterIdAndPostId(Long reporterId, Long postId);
    boolean existsByReporterIdAndCommentId(Long reporterId, Long commentId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Report r where r.id = :id")
    Optional<Report> findForUpdate(@Param("id") Long id);
}
