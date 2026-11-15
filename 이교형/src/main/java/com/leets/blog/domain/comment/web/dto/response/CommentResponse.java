package com.leets.blog.domain.comment.web.dto.response;

import com.leets.blog.domain.comment.entity.Comment;
import com.leets.blog.domain.member.entity.Member;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

// 댓글 작성·수정 응답, 목록의 댓글 한 개
@Schema(description = "댓글 응답")
public record CommentResponse(

        @Schema(description = "댓글 ID", example = "1")
        Long commentId,

        @Schema(description = "게시글 ID", example = "1")
        Long postId,

        @Schema(description = "댓글 내용", example = "좋은 글이네요.")
        String content,

        @Schema(description = "작성 시각", example = "2026-11-02T15:00:00")
        LocalDateTime createdAt,

        @Schema(description = "작성자")
        MemberInfo member
) {

    // PostDetailResponse.MemberInfo와 Swagger 스키마 이름이 같아 하나로 합쳐지므로 설명을 똑같이 유지
    @Schema(description = "작성자 정보")
    public record MemberInfo(

            @Schema(description = "회원 ID", example = "1")
            Long memberId,

            @Schema(description = "닉네임", example = "길동이")
            String nickname
    ) {
        public static MemberInfo from(Member member) {
            return new MemberInfo(member.getId(), member.getNickname());
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
