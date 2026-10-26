package com.leets_8th_be.kanghyeonwoo.report.application.port.in;

import com.leets_8th_be.kanghyeonwoo.report.application.model.ReportView;

public interface ReportPostUseCase {
    ReportView report(ReportPostCommand command);
}
