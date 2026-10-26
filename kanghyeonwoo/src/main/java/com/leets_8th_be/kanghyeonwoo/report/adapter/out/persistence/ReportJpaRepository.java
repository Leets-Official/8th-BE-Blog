package com.leets_8th_be.kanghyeonwoo.report.adapter.out.persistence;

import com.leets_8th_be.kanghyeonwoo.report.domain.model.ReportTarget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

interface ReportJpaRepository extends JpaRepository<ReportJpaEntity, Long>,
        JpaSpecificationExecutor<ReportJpaEntity> {
    boolean existsByReporterIdAndTargetTypeAndTargetId(Long reporterId, ReportTarget targetType, Long targetId);
}
