package com.leets_8th_be.kanghyeonwoo.blog.application.model;

import com.leets_8th_be.kanghyeonwoo.blog.domain.model.CommentId;
import com.leets_8th_be.kanghyeonwoo.blog.domain.model.CommentState;
import com.leets_8th_be.kanghyeonwoo.blog.domain.model.PostId;
import com.leets_8th_be.kanghyeonwoo.blog.domain.model.PostState;
import com.leets_8th_be.kanghyeonwoo.shared.domain.UserId;

import java.time.LocalDateTime;

public record CommentView(
        CommentId id,
        UserId authorId,
        CommentId parentId,
        String content,
        String displayContent,
        CommentState state,
        boolean deleted,
        PostId postId,
        PostState postState,
        LocalDateTime createdAt) {}
