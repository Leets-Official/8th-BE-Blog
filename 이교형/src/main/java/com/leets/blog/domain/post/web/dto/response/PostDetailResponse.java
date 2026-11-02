package com.leets.blog.domain.post.web.dto.response;

import com.leets.blog.domain.member.entity.Member;
import com.leets.blog.domain.post.entity.Post;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "게시글 상세 응답")
public record PostDetailResponse(

        @Schema(description = "게시글 ID", example = "1")
        Long postId,

        @Schema(description = "제목", example = "새 게시글")
        String title,

        @Schema(description = "본문", example = "내용입니다")
        String content,

        @Schema(description = "작성 시각", example = "2026-11-02T15:00:00")
        LocalDateTime createdAt,

        @Schema(description = "마지막 수정 시각", example = "2026-11-02T15:30:00")
        LocalDateTime updatedAt,

        @Schema(description = "삭제 시각 (삭제된 글은 조회되지 않아 항상 null)", nullable = true, example = "null")
        LocalDateTime deletedAt,

        @Schema(description = "작성자")
        MemberInfo member
) {

    // CommentResponse.MemberInfo와 Swagger 스키마 이름이 같아 하나로 합쳐지므로 설명을 똑같이 유지
    @Schema(description = "작성자 정보")
    public record MemberInfo(

            @Schema(description = "회원 ID", example = "1")
            Long memberId,

            @Schema(description = "이름", example = "홍길동")
            String name
    ) {
        public static MemberInfo from(Member member) {
            return new MemberInfo(member.getId(), member.getName());
        }
    }

    public static PostDetailResponse from(Post post) {
        return new PostDetailResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                post.getDeletedAt(),
                MemberInfo.from(post.getMember())
        );
    }
}
