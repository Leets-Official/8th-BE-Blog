package com.leets_8th_be.kanghyeonwoo.blog.adapter.in.contract;

import com.leets_8th_be.kanghyeonwoo.blog.application.contract.ContentTargetSnapshot;
import com.leets_8th_be.kanghyeonwoo.blog.application.contract.ContentTargetType;
import com.leets_8th_be.kanghyeonwoo.blog.application.contract.ContentVisibility;
import com.leets_8th_be.kanghyeonwoo.blog.application.exception.CommentNotFound;
import com.leets_8th_be.kanghyeonwoo.blog.application.exception.PostNotFound;
import com.leets_8th_be.kanghyeonwoo.blog.application.model.CommentView;
import com.leets_8th_be.kanghyeonwoo.blog.application.model.PostView;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.HideCommentUseCase;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.HidePostUseCase;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.InspectCommentTargetUseCase;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.InspectPostUseCase;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.ManageContentTargetsUseCase;
import com.leets_8th_be.kanghyeonwoo.blog.domain.model.CommentId;
import com.leets_8th_be.kanghyeonwoo.blog.domain.model.CommentState;
import com.leets_8th_be.kanghyeonwoo.blog.domain.model.PostId;
import com.leets_8th_be.kanghyeonwoo.blog.domain.model.PostState;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class BlogContentTargetsAdapter implements ManageContentTargetsUseCase {
    private final InspectPostUseCase inspectPosts;
    private final HidePostUseCase hidePosts;
    private final InspectCommentTargetUseCase inspectComments;
    private final HideCommentUseCase hideComments;

    public BlogContentTargetsAdapter(
            InspectPostUseCase inspectPosts,
            HidePostUseCase hidePosts,
            InspectCommentTargetUseCase inspectComments,
            HideCommentUseCase hideComments) {
        this.inspectPosts = inspectPosts;
        this.hidePosts = hidePosts;
        this.inspectComments = inspectComments;
        this.hideComments = hideComments;
    }

    @Override
    public Optional<ContentTargetSnapshot> inspect(ContentTargetType target, Long targetId) {
        return switch (target) {
            case POST -> inspectPosts.inspect(new PostId(targetId)).map(BlogContentTargetsAdapter::snapshot);
            case COMMENT -> inspectComments.inspect(new CommentId(targetId)).map(BlogContentTargetsAdapter::snapshot);
        };
    }

    @Override
    public Optional<ContentTargetSnapshot> hide(ContentTargetType target, Long targetId) {
        return switch (target) {
            case POST -> hidePost(targetId);
            case COMMENT -> hideComment(targetId);
        };
    }

    private Optional<ContentTargetSnapshot> hidePost(Long targetId) {
        try {
            return Optional.of(snapshot(hidePosts.hide(new PostId(targetId))));
        } catch (PostNotFound missing) {
            return Optional.empty();
        }
    }

    private Optional<ContentTargetSnapshot> hideComment(Long targetId) {
        try {
            return Optional.of(snapshot(hideComments.hide(new CommentId(targetId))));
        } catch (CommentNotFound missing) {
            return Optional.empty();
        }
    }

    private static ContentTargetSnapshot snapshot(PostView post) {
        return new ContentTargetSnapshot(
                post.authorId(),
                post.title(),
                post.content(),
                post.state() == PostState.DELETED
                        ? ContentVisibility.DELETED
                        : post.state() == PostState.HIDDEN ? ContentVisibility.HIDDEN : ContentVisibility.ACTIVE,
                null);
    }

    private static ContentTargetSnapshot snapshot(CommentView comment) {
        ContentVisibility visibility = comment.deleted()
                ? ContentVisibility.DELETED
                : comment.state() == CommentState.HIDDEN ? ContentVisibility.HIDDEN : ContentVisibility.ACTIVE;
        return new ContentTargetSnapshot(
                comment.authorId(), null, comment.content(), visibility, visibility(comment.postState()));
    }

    private static ContentVisibility visibility(PostState state) {
        return state == PostState.DELETED
                ? ContentVisibility.DELETED
                : state == PostState.HIDDEN ? ContentVisibility.HIDDEN : ContentVisibility.ACTIVE;
    }
}
