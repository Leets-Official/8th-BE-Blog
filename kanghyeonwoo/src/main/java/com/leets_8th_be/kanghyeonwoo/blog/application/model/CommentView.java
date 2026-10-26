package com.leets_8th_be.kanghyeonwoo.blog.application.model;

import com.leets_8th_be.kanghyeonwoo.blog.domain.model.Comment;

import java.time.LocalDateTime;

public record CommentView(Comment comment, LocalDateTime createdAt) {
}
