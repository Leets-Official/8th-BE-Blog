package com.leets_8th_be.kanghyeonwoo.repository;

import com.leets_8th_be.kanghyeonwoo.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByReporterId(Long reporterId);
    List<Report> findByPostId(Long postId);
    List<Report> findByCommentId(Long commentId);
}
