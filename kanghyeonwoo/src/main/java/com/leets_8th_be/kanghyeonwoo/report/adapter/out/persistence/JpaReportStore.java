package com.leets_8th_be.kanghyeonwoo.report.adapter.out.persistence;

import com.leets_8th_be.kanghyeonwoo.report.application.model.ReportView;
import com.leets_8th_be.kanghyeonwoo.report.application.model.ReportListItem;
import com.leets_8th_be.kanghyeonwoo.report.application.model.ReportPage;
import com.leets_8th_be.kanghyeonwoo.report.application.port.in.ReportSearchQuery;
import com.leets_8th_be.kanghyeonwoo.report.application.port.out.ReportStore;
import com.leets_8th_be.kanghyeonwoo.report.domain.model.Report;
import com.leets_8th_be.kanghyeonwoo.report.domain.model.ReportId;
import com.leets_8th_be.kanghyeonwoo.report.domain.model.ReportTarget;
import com.leets_8th_be.kanghyeonwoo.report.domain.model.ReportAction;
import com.leets_8th_be.kanghyeonwoo.user.domain.model.UserId;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import com.leets_8th_be.kanghyeonwoo.shared.adapter.out.persistence.TextSearch;

@Repository
public class JpaReportStore implements ReportStore {
    private final ReportJpaRepository reports;

    public JpaReportStore(ReportJpaRepository reports) {
        this.reports = reports;
    }

    @Override
    public boolean exists(UserId reporterId, ReportTarget target, Long targetId) {
        return reports.existsByReporterIdAndTargetTypeAndTargetId(reporterId.value(), target, targetId);
    }

    @Override
    public ReportView save(Report report) {
        ReportJpaEntity entity;
        if (report.id() == null) {
            entity = ReportJpaEntity.from(report);
        } else {
            entity = reports.findById(report.id().value())
                    .orElseThrow(() -> new IllegalStateException("저장할 신고가 없습니다."));
            entity.applyResolution(report);
        }
        return toView(reports.saveAndFlush(entity));
    }

    @Override
    public java.util.Optional<Report> findForUpdate(ReportId id) {
        return reports.findForUpdate(id.value()).map(ReportJpaEntity::toDomain);
    }

    @Override
    public ReportPage search(ReportSearchQuery query) {
        var pageable = PageRequest.of(query.page() - 1, query.size(),
                Sort.by(Sort.Direction.DESC, "createdAt", "id"));
        Specification<ReportJpaEntity> criteria = (root, q, cb) -> cb.conjunction();
        if (query.status() != null) criteria = criteria.and((root, q, cb) ->
                cb.equal(root.get("status"), query.status()));
        if (query.target() != null) criteria = criteria.and((root, q, cb) ->
                cb.equal(root.get("targetType"), query.target()));
        if (query.targetId() != null) criteria = criteria.and((root, q, cb) ->
                cb.equal(root.get("targetId"), query.targetId()));
        if (query.keyword() != null && !query.keyword().isBlank()) {
            String keyword = query.keyword().strip();
            criteria = criteria.and((root, q, cb) -> cb.or(
                    TextSearch.contains(cb, root.get("reason"), keyword),
                    TextSearch.contains(cb, root.get("targetTitleSnapshot"), keyword),
                    TextSearch.contains(cb, root.get("targetContentSnapshot"), keyword)));
        }
        Page<ReportListItem> page = reports.findAll(criteria, pageable).map(JpaReportStore::toListItem);
        return new ReportPage(page.getContent(), page.getNumber() + 1, page.getSize(),
                page.getTotalElements(), page.getTotalPages());
    }

    private static ReportView toView(ReportJpaEntity entity) {
        return new ReportView(new ReportId(entity.getId()), entity.getTargetType(), entity.getTargetId(),
                entity.getStatus(), entity.getReason(), entity.getTargetTitleSnapshot(),
                entity.getTargetContentSnapshot());
    }

    private static ReportListItem toListItem(ReportJpaEntity entity) {
        return new ReportListItem(new ReportId(entity.getId()), new UserId(entity.getReporterId()), entity.getTargetType(),
                entity.getTargetId(), entity.getStatus(), entity.getReason(), entity.getTargetTitleSnapshot(),
                entity.getTargetContentSnapshot(), entity.getCreatedAt(), entity.getAction() == null ? null
                : ReportAction.valueOf(entity.getAction()), entity.getResolutionReason(), entity.getProcessorId() == null
                ? null : new UserId(entity.getProcessorId()),
                entity.getResolvedAt());
    }
}
