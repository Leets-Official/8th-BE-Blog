package com.leets.choyeji.repository;

import com.leets.choyeji.entity.Report;
import com.leets.choyeji.entity.ReportStatus;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReportRepository extends JpaRepository<Report, Long> {
    @Override
    @EntityGraph(attributePaths = {"post", "member"})
    Page<Report> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"post", "member"})
    Page<Report> findAllByStatus(ReportStatus status, Pageable pageable);

    @Query("select r.post.id from Report r where r.id = :id")
    Optional<Long> findPostIdById(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Report r where r.id = :id")
    Optional<Report> findByIdForUpdate(@Param("id") Long id);

    boolean existsByPostIdAndMemberId(Long postId, Long memberId);

    @Modifying
    @Query("delete from Report r where r.post.id = :postId")
    void deleteAllByPostId(@Param("postId") Long postId);
}
