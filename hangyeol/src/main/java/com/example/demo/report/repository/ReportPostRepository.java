package com.example.demo.report.repository;

import com.example.demo.report.entity.ReportPost;
import com.example.demo.report.entity.ReportPostId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportPostRepository extends JpaRepository<ReportPost, ReportPostId> {
}
