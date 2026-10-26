package com.example.demo.report.repository;

import com.example.demo.report.entity.ReportComment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportCommentRepository extends JpaRepository<ReportComment,Long> {
}
