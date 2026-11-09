package com.leets_8th_be.kanghyeonwoo.blog.application.port.in;

import com.leets_8th_be.kanghyeonwoo.blog.application.model.CommentPage;

public interface GetCommentsUseCase {
    CommentPage getComments(CommentSearchQuery query);
}
