package com.leets_8th_be.kanghyeonwoo.blog.application.usecase;

import com.leets_8th_be.kanghyeonwoo.blog.application.exception.AuthorNotFound;
import com.leets_8th_be.kanghyeonwoo.blog.application.model.PostView;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.CreatePostCommand;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.CreatePostUseCase;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.out.PostStore;
import com.leets_8th_be.kanghyeonwoo.blog.domain.model.Post;
import com.leets_8th_be.kanghyeonwoo.user.application.port.in.UserProfileLookup;

public class CreatePostService implements CreatePostUseCase {
    private final UserProfileLookup profiles;
    private final PostStore posts;

    public CreatePostService(UserProfileLookup profiles, PostStore posts) {
        this.profiles = profiles;
        this.posts = posts;
    }

    @Override
    public PostView create(CreatePostCommand command) {
        if (profiles.findById(command.authorId()).isEmpty()) {
            throw new AuthorNotFound();
        }

        return posts.save(new Post(command.title(), command.content(), command.authorId()));
    }
}
