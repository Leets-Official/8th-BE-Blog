package com.leets_8th_be.kanghyeonwoo.blog.application.contract;

import com.leets_8th_be.kanghyeonwoo.shared.domain.UserId;

public record ContentTargetSnapshot(
        UserId authorId,
        String title,
        String content,
        ContentVisibility visibility,
        ContentVisibility parentPostVisibility) {}
