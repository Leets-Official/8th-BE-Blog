package com.leets_8th_be.kanghyeonwoo.blog.application.port.in;

import com.leets_8th_be.kanghyeonwoo.blog.application.model.CommentView;
import com.leets_8th_be.kanghyeonwoo.blog.domain.model.CommentId;

public interface HideCommentUseCase {
    CommentView hide(CommentId id);
}
