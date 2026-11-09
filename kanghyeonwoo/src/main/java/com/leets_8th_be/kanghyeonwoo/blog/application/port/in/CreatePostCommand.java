package com.leets_8th_be.kanghyeonwoo.blog.application.port.in;

import com.leets_8th_be.kanghyeonwoo.shared.domain.UserId;

public record CreatePostCommand(String title, String content, UserId authorId) {}
