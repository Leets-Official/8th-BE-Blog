package com.leets_8th_be.kanghyeonwoo.blog.application.usecase;

import com.leets_8th_be.kanghyeonwoo.blog.application.exception.AuthorNotFound;
import com.leets_8th_be.kanghyeonwoo.blog.application.exception.CommentNotFound;
import com.leets_8th_be.kanghyeonwoo.blog.application.model.CommentView;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.CreateCommentCommand;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.CreateCommentUseCase;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.GetPostUseCase;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.out.CommentStore;
import com.leets_8th_be.kanghyeonwoo.blog.domain.model.Comment;
import com.leets_8th_be.kanghyeonwoo.user.application.port.in.UserProfileLookup;

public class CreateCommentService implements CreateCommentUseCase {
    private final UserProfileLookup profiles;
    private final GetPostUseCase posts;
    private final CommentStore comments;

    public CreateCommentService(UserProfileLookup profiles, GetPostUseCase posts, CommentStore comments) {
        this.profiles = profiles;
        this.posts = posts;
        this.comments = comments;
    }

    @Override
    public CommentView create(CreateCommentCommand command) {
        if (profiles.findById(command.authorId()).isEmpty()) throw new AuthorNotFound();
        posts.get(command.postId());
        if (command.parentId() != null) {
            var parent = comments.findTarget(command.parentId()).orElseThrow(CommentNotFound::new);
            if (!parent.postId().equals(command.postId())) throw new CommentNotFound();
        }
        return comments.create(command.postId(), Comment.create(command.authorId(), command.parentId(), command.content()));
    }
}
