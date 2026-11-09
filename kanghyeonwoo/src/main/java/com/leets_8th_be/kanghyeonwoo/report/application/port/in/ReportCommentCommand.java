package com.leets_8th_be.kanghyeonwoo.report.application.port.in;

import com.leets_8th_be.kanghyeonwoo.blog.domain.model.CommentId;
import com.leets_8th_be.kanghyeonwoo.user.domain.model.UserId;

public record ReportCommentCommand(CommentId commentId, UserId reporterId, String reason) {
}
