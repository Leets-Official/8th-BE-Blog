package com.leets_8th_be.kanghyeonwoo.blog.application.port.out;

import com.leets_8th_be.kanghyeonwoo.blog.application.model.CommentPage;
import com.leets_8th_be.kanghyeonwoo.blog.application.model.CommentView;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.CommentSearchQuery;
import com.leets_8th_be.kanghyeonwoo.blog.domain.model.CommentId;

import java.util.Optional;
import com.leets_8th_be.kanghyeonwoo.blog.domain.model.Comment;

public interface CommentStore {
    CommentPage search(CommentSearchQuery query);

    Optional<CommentView> findTarget(CommentId id);

    Optional<CommentView> findForUpdate(CommentId id);

    CommentView save(Comment comment);
}
