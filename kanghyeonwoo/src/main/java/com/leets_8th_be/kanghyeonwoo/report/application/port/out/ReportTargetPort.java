package com.leets_8th_be.kanghyeonwoo.report.application.port.out;

import com.leets_8th_be.kanghyeonwoo.report.application.model.ReportTargetSnapshot;
import java.util.Optional;

public interface ReportTargetPort {
    Optional<ReportTargetSnapshot> find(ReportTargetKind target, Long targetId);

    Optional<ReportTargetSnapshot> hide(ReportTargetKind target, Long targetId);
}
