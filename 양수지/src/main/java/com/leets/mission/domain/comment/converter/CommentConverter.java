package com.leets.mission.domain.comment.converter;

import com.leets.mission.domain.comment.dto.CommentRequestDTO;
import com.leets.mission.domain.comment.dto.CommentResponseDTO;
import com.leets.mission.domain.comment.entity.Comment;
import com.leets.mission.domain.post.entity.Post;
import com.leets.mission.domain.user.entity.User;

import java.util.List;

// 댓글 Entity ↔ DTO 변환
public class CommentConverter {

    private CommentConverter() {
    }

    // 작성 요청 DTO -> Comment 엔티티
    public static Comment toComment(CommentRequestDTO.CreateDTO request, Post post, User user) {
        return Comment.builder()
                .content(request.content())
                .post(post)
                .user(user)
                .build();
    }

    // Comment 엔티티 -> 상세 응답 DTO
    public static CommentResponseDTO.DetailDTO toDetailDTO(Comment comment) {
        return CommentResponseDTO.DetailDTO.builder()
                .commentId(comment.getId())
                .postId(comment.getPost().getId())
                .content(comment.getContent())
                .writer(comment.getUser().getNickname())
                .createdAt(comment.getCreatedAt())
                .build();
    }

    // 댓글 목록 -> 목록 응답 DTO
    public static CommentResponseDTO.ListDTO toListDTO(Long postId, List<Comment> comments) {
        return CommentResponseDTO.ListDTO.builder()
                .postId(postId)
                .comments(comments.stream()
                        .map(CommentConverter::toDetailDTO)
                        .toList())
                .count(comments.size())
                .build();
    }
}
