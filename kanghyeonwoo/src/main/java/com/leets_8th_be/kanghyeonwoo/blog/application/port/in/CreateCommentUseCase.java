package com.leets_8th_be.kanghyeonwoo.blog.application.port.in;
import com.leets_8th_be.kanghyeonwoo.blog.application.model.CommentView;
public interface CreateCommentUseCase { CommentView create(CreateCommentCommand command); }
