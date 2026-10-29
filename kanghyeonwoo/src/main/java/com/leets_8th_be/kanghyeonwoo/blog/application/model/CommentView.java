package com.leets_8th_be.kanghyeonwoo.blog.application.model;

import com.leets_8th_be.kanghyeonwoo.blog.domain.model.Comment;
import com.leets_8th_be.kanghyeonwoo.blog.domain.model.PostId;
import com.leets_8th_be.kanghyeonwoo.blog.domain.model.PostState;

import java.time.LocalDateTime;

public record CommentView(Comment comment, PostId postId, PostState postState, LocalDateTime createdAt) {
}
