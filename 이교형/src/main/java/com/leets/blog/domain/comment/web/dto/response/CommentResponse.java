package com.leets.blog.domain.comment.web.dto.response;

import com.leets.blog.domain.comment.entity.Comment;
import com.leets.blog.domain.member.entity.Member;

import java.time.LocalDateTime;

// 댓글 작성·수정 응답, 목록의 댓글 한 개
public record CommentResponse(
        Long commentId,
        Long postId,
        String content,
        LocalDateTime createdAt,
        MemberInfo member
) {

    public record MemberInfo(
            Long memberId,
            String name
    ) {
        public static MemberInfo from(Member member) {
            return new MemberInfo(member.getId(), member.getName());
        }
    }

    public static CommentResponse from(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getPost().getId(),
                comment.getContent(),
                comment.getCreatedAt(),
                MemberInfo.from(comment.getMember())
        );
    }
}
