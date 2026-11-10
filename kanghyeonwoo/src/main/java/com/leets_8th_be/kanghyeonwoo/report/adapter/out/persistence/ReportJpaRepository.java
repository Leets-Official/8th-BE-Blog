package com.leets_8th_be.kanghyeonwoo.report.adapter.out.persistence;

import com.leets_8th_be.kanghyeonwoo.report.domain.model.ReportTarget;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface ReportJpaRepository extends JpaRepository<ReportJpaEntity, Long>, JpaSpecificationExecutor<ReportJpaEntity> {
    boolean existsByReporterIdAndTargetTypeAndTargetId(Long reporterId, ReportTarget targetType, Long targetId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from ReportJpaEntity r where r.id = :id")
    Optional<ReportJpaEntity> findForUpdate(@Param("id") Long id);
}
