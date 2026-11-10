package com.leets_8th_be.kanghyeonwoo.report.application.usecase;

import com.leets_8th_be.kanghyeonwoo.report.application.exception.DeletedReportTarget;
import com.leets_8th_be.kanghyeonwoo.report.application.exception.ReportNotFound;
import com.leets_8th_be.kanghyeonwoo.report.application.exception.ReporterNotFound;
import com.leets_8th_be.kanghyeonwoo.report.application.model.ReportResolutionView;
import com.leets_8th_be.kanghyeonwoo.report.application.model.ReportTargetSnapshot;
import com.leets_8th_be.kanghyeonwoo.report.application.port.in.ResolveReportCommand;
import com.leets_8th_be.kanghyeonwoo.report.application.port.in.ResolveReportUseCase;
import com.leets_8th_be.kanghyeonwoo.report.application.port.out.ReportStore;
import com.leets_8th_be.kanghyeonwoo.report.application.port.out.ReportTargetKind;
import com.leets_8th_be.kanghyeonwoo.report.application.port.out.ReportTargetPort;
import com.leets_8th_be.kanghyeonwoo.report.application.port.out.ReporterDirectory;
import com.leets_8th_be.kanghyeonwoo.report.domain.model.Report;
import com.leets_8th_be.kanghyeonwoo.report.domain.model.ReportAction;
import com.leets_8th_be.kanghyeonwoo.report.domain.model.ReportTarget;
import java.time.Clock;
import java.time.LocalDateTime;

public class ResolveReportService implements ResolveReportUseCase {
    private final ReporterDirectory reporters;
    private final ReportStore reports;
    private final ReportTargetPort targets;
    private final Clock clock;

    public ResolveReportService(
            ReporterDirectory reporters, ReportStore reports, ReportTargetPort targets, Clock clock) {
        this.reporters = reporters;
        this.reports = reports;
        this.targets = targets;
        this.clock = clock;
    }

    @Override
    public ReportResolutionView resolve(ResolveReportCommand command) {
        if (!reporters.exists(command.processorId())) {
            throw new ReporterNotFound();
        }
        Report pending = reports.findForUpdate(command.reportId()).orElseThrow(ReportNotFound::new);
        Report resolved = pending.resolve(
                command.action(), command.resolutionReason(), command.processorId(), LocalDateTime.now(clock));
        TargetVisibility visibility =
                resolveTarget(toTargetKind(pending.target()), pending.targetId(), command.action());
        reports.save(resolved);
        return new ReportResolutionView(
                resolved.id(),
                resolved.state(),
                resolved.action(),
                resolved.processorId(),
                resolved.resolvedAt(),
                visibility.available(),
                visibility.hidden());
    }

    private TargetVisibility resolveTarget(ReportTargetKind target, Long targetId, ReportAction action) {
        ReportTargetSnapshot current = targets.find(target, targetId).orElse(null);
        if (isDeleted(target, current)) {
            return deletedTarget(action);
        }
        if (action != ReportAction.HIDE) {
            return new TargetVisibility(true, current.hidden());
        }

        ReportTargetSnapshot hidden = targets.hide(target, targetId).orElse(null);
        if (isDeleted(target, hidden)) {
            return deletedTarget(action);
        }
        return new TargetVisibility(true, true);
    }

    private static boolean isDeleted(ReportTargetKind target, ReportTargetSnapshot snapshot) {
        return snapshot == null
                || snapshot.deleted()
                || target == ReportTargetKind.COMMENT && snapshot.parentPostDeleted();
    }

    private static ReportTargetKind toTargetKind(ReportTarget target) {
        return ReportTargetKind.valueOf(target.name());
    }

    private TargetVisibility deletedTarget(ReportAction action) {
        if (action == ReportAction.HIDE) {
            throw new DeletedReportTarget();
        }
        return new TargetVisibility(false, null);
    }

    private record TargetVisibility(boolean available, Boolean hidden) {}
}
