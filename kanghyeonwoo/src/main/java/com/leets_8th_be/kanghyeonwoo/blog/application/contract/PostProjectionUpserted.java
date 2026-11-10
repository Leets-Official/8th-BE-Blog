package com.leets_8th_be.kanghyeonwoo.blog.application.contract;

import java.time.LocalDateTime;

public record PostProjectionUpserted(
        long postId, String title, long authorId, LocalDateTime createdAt, boolean visible) {}
