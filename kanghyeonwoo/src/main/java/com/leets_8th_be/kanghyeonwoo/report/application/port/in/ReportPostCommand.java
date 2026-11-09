package com.leets_8th_be.kanghyeonwoo.report.application.port.in;

import com.leets_8th_be.kanghyeonwoo.shared.domain.UserId;

public record ReportPostCommand(Long postId, UserId reporterId, String reason) {}
