package com.leets_8th_be.kanghyeonwoo.blog.application.port.in;

import com.leets_8th_be.kanghyeonwoo.blog.application.model.CommentView;
import com.leets_8th_be.kanghyeonwoo.blog.domain.model.CommentId;

import java.util.Optional;

public interface InspectCommentTargetUseCase {
    Optional<CommentView> inspect(CommentId id);
}
