package com.leets_8th_be.kanghyeonwoo.blog.application.port.in;

import com.leets_8th_be.kanghyeonwoo.blog.domain.model.CommentId;
import com.leets_8th_be.kanghyeonwoo.blog.domain.model.PostId;
import com.leets_8th_be.kanghyeonwoo.shared.domain.UserId;

public record CreateCommentCommand(
        PostId postId, UserId authorId, CommentId parentId, String content) {}
