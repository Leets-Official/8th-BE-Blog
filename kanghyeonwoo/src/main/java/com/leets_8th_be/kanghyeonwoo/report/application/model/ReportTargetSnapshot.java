package com.leets_8th_be.kanghyeonwoo.report.application.model;

import com.leets_8th_be.kanghyeonwoo.shared.domain.UserId;

public record ReportTargetSnapshot(
        UserId authorId,
        String title,
        String content,
        boolean hidden,
        boolean deleted,
        boolean parentPostActive,
        boolean parentPostDeleted) {}
