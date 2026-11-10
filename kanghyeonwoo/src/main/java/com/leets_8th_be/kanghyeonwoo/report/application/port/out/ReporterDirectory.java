package com.leets_8th_be.kanghyeonwoo.report.application.port.out;

import com.leets_8th_be.kanghyeonwoo.shared.domain.UserId;

public interface ReporterDirectory {
    boolean exists(UserId id);
}
