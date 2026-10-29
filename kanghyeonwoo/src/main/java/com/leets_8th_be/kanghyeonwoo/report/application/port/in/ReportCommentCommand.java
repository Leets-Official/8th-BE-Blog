package com.leets_8th_be.kanghyeonwoo.report.application.port.in;

import com.leets_8th_be.kanghyeonwoo.shared.domain.UserId;

public record ReportCommentCommand(Long commentId, UserId reporterId, String reason) {
}
