package com.leets_8th_be.kanghyeonwoo.blog.application.model;

import com.leets_8th_be.kanghyeonwoo.blog.domain.model.PostId;
import com.leets_8th_be.kanghyeonwoo.blog.domain.model.PostState;
import com.leets_8th_be.kanghyeonwoo.user.domain.model.UserId;

import java.time.LocalDateTime;

public record PostView(PostId id, String title, String content, UserId authorId,
                       PostState state, LocalDateTime createdAt, LocalDateTime updatedAt) {
}
