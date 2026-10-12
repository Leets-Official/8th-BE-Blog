package com.leets.mission.domain.post.converter;

import com.leets.mission.domain.post.dto.PostRequestDTO;
import com.leets.mission.domain.post.dto.PostResponseDTO;
import com.leets.mission.domain.post.entity.Post;
import com.leets.mission.domain.user.entity.User;

// 게시글 Entity ↔ DTO 변환
public class PostConverter {

    private PostConverter() {
    }

    // 생성 요청 DTO → Post 엔티티
    public static Post toPost(PostRequestDTO.CreateDTO request, User user) {
        return Post.builder()
                .title(request.title())
                .content(request.content())
                .user(user)
                .build();
    }

    // Post 엔티티 → 상세 응답 DTO
    public static PostResponseDTO.DetailDTO toDetailDTO(Post post) {
        return PostResponseDTO.DetailDTO.builder()
                .postId(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .writer(post.getUser().getNickname())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}
