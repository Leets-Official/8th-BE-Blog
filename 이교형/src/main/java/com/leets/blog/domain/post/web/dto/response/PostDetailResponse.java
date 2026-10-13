package com.leets.blog.domain.post.web.dto.response;

import com.leets.blog.domain.member.entity.Member;
import com.leets.blog.domain.post.entity.Post;

import java.time.LocalDateTime;

public record PostDetailResponse(
        Long postId,
        String title,
        String content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime deletedAt,
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
