package com.leets_8th_be.kanghyeonwoo.blog.application.usecase;

import com.leets_8th_be.kanghyeonwoo.blog.application.exception.InvalidCommentQuery;
import com.leets_8th_be.kanghyeonwoo.blog.application.model.CommentPage;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.CommentSearchQuery;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.GetCommentsUseCase;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.GetPostUseCase;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.out.CommentStore;

public class GetCommentsService implements GetCommentsUseCase {
    private final GetPostUseCase posts;
    private final CommentStore comments;

    public GetCommentsService(GetPostUseCase posts, CommentStore comments) {
        this.posts = posts;
        this.comments = comments;
    }

    @Override
    public CommentPage getComments(CommentSearchQuery query) {
        if (query.postId().value() < 1 || query.page() < 1 || query.size() < 1 || query.size() > 100) {
            throw new InvalidCommentQuery();
        }
        posts.get(query.postId());
        return comments.search(query);
    }
}
