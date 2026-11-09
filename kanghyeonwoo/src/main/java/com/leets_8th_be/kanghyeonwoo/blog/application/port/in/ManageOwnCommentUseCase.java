package com.leets_8th_be.kanghyeonwoo.blog.application.port.in;
import com.leets_8th_be.kanghyeonwoo.blog.domain.model.CommentId;
import com.leets_8th_be.kanghyeonwoo.shared.domain.UserId;
import com.leets_8th_be.kanghyeonwoo.blog.application.model.CommentView;
public interface ManageOwnCommentUseCase {
 CommentView update(CommentId id,UserId actor,String content);
 CommentView deleteOwn(CommentId id,UserId actor);
}
