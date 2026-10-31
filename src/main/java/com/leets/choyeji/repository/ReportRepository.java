package com.leets.choyeji.repository;

import com.leets.choyeji.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReportRepository extends JpaRepository<Report, Long> {
    boolean existsByPostIdAndMemberId(Long postId, Long memberId);

    @Modifying
    @Query("delete from Report r where r.post.id = :postId")
    void deleteAllByPostId(@Param("postId") Long postId);
}
