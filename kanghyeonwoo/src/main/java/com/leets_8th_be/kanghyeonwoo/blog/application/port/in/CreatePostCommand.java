package com.leets_8th_be.kanghyeonwoo.blog.application.port.in;

import com.leets_8th_be.kanghyeonwoo.user.domain.model.UserId;

public record CreatePostCommand(String title, String content, UserId authorId) {
}
