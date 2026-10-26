package com.leets_8th_be.kanghyeonwoo.blog.application.usecase;

import com.leets_8th_be.kanghyeonwoo.blog.application.exception.CommentNotFound;
import com.leets_8th_be.kanghyeonwoo.blog.application.exception.PostNotFound;
import com.leets_8th_be.kanghyeonwoo.blog.application.model.CommentView;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.GetCommentTargetUseCase;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.GetPostUseCase;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.out.CommentStore;
import com.leets_8th_be.kanghyeonwoo.blog.domain.model.CommentId;

public class GetCommentTargetService implements GetCommentTargetUseCase {
    private final CommentStore comments;
    private final GetPostUseCase posts;

    public GetCommentTargetService(CommentStore comments, GetPostUseCase posts) {
        this.comments = comments;
        this.posts = posts;
    }

    @Override
    public CommentView getTarget(CommentId id) {
        CommentView target = comments.findTarget(id).orElseThrow(CommentNotFound::new);
        posts.get(target.postId());
        return target;
    }
}
