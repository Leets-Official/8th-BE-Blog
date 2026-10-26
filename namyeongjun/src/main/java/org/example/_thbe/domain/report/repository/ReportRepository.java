package org.example._thbe.domain.report.repository;

import org.example._thbe.domain.report.entity.Report;
import org.example._thbe.domain.report.enums.ReportContentType;
import org.example._thbe.domain.report.enums.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report,Long> {
    //중복 파악
    boolean existsByReporterIdAndContentTypeAndContentId(
            Long reporterId,
            ReportContentType contentType,
            Long contentId
    );

    //해당 도메인 신고 횟수 카운트
    long countByContentTypeAndContentId(ReportContentType contentType,Long contentId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Report r SET r.status = :status WHERE r.contentType = :contentType AND r.contentId = :contentId")
    void updateReportStatusByContent(@Param("status") ReportStatus status,
                                     @Param("contentType") ReportContentType contentType,
                                     @Param("contentId") Long contentId);

    //패치조인 목록 조회 쿼리
    @Query("SELECT r FROM Report r JOIN FETCH r.reporter WHERE r.status = :status")
    List<Report> findByStatusWithReporter(@Param("status") ReportStatus status);


}