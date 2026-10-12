package com.leets.mission.domain.post.converter;

import com.leets.mission.domain.post.dto.PostRequestDTO;
import com.leets.mission.domain.post.dto.PostResponseDTO;
import com.leets.mission.domain.post.entity.Post;
import com.leets.mission.domain.user.entity.User;
import org.springframework.data.domain.Page;

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

    // Post 엔티티 → 목록 항목 DTO
    public static PostResponseDTO.PreviewDTO toPreviewDTO(Post post) {
        return PostResponseDTO.PreviewDTO.builder()
                .postId(post.getId())
                .title(post.getTitle())
                .writer(post.getUser().getNickname())
                .createdAt(post.getCreatedAt())
                .build();
    }

    // 게시글 페이지 → 목록 응답 DTO
    public static PostResponseDTO.PreviewListDTO toPreviewListDTO(Page<Post> postPage) {
        return PostResponseDTO.PreviewListDTO.builder()
                .posts(postPage.getContent().stream()
                        .map(PostConverter::toPreviewDTO)
                        .toList())
                .page(postPage.getNumber())
                .size(postPage.getSize())
                .totalElements(postPage.getTotalElements())
                .totalPages(postPage.getTotalPages())
                .hasNext(postPage.hasNext())
                .build();
    }

    // 삭제된 게시글 ID → 삭제 응답 DTO
    public static PostResponseDTO.DeleteDTO toDeleteDTO(Long postId) {
        return PostResponseDTO.DeleteDTO.builder()
                .postId(postId)
                .build();
    }
}
