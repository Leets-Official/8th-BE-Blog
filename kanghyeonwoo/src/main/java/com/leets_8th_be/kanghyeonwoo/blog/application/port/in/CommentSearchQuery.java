package com.leets_8th_be.kanghyeonwoo.blog.application.port.in;

import com.leets_8th_be.kanghyeonwoo.blog.domain.model.PostId;

public record CommentSearchQuery(PostId postId, int page, int size, String keyword) {
}
