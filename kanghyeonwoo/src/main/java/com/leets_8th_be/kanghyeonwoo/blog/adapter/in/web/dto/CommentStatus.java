package com.leets_8th_be.kanghyeonwoo.blog.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "댓글 공개 상태")
public enum CommentStatus {
    ACTIVE,
    HIDDEN
}
