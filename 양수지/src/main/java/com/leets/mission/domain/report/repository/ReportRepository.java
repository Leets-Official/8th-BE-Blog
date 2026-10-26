package com.leets.mission.domain.report.repository;

import com.leets.mission.domain.report.entity.Report;
import com.leets.mission.domain.report.entity.ReportTargetType;
import com.leets.mission.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {

    // 같은 사용자가 같은 대상을 이미 신고했는지 확인
    boolean existsByReporterAndTargetTypeAndTargetId(User reporter, ReportTargetType targetType, Long targetId);
}
