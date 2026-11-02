package com.leets.mission.domain.comment.service;

import com.leets.mission.domain.comment.converter.CommentConverter;
import com.leets.mission.domain.comment.dto.CommentRequestDTO;
import com.leets.mission.domain.comment.dto.CommentResponseDTO;
import com.leets.mission.domain.comment.entity.Comment;
import com.leets.mission.domain.comment.entity.CommentStatus;
import com.leets.mission.domain.comment.repository.CommentRepository;
import com.leets.mission.domain.post.entity.Post;
import com.leets.mission.domain.post.repository.PostRepository;
import com.leets.mission.domain.user.entity.User;
import com.leets.mission.domain.user.repository.UserRepository;
import com.leets.mission.global.apiPayload.code.ErrorStatus;
import com.leets.mission.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    // 댓글 작성
    @Transactional
    public CommentResponseDTO.DetailDTO createComment(Long postId, CommentRequestDTO.CreateDTO request) {
        Post post = findActivePost(postId);
        User user = findUser(request.userId());
        Comment comment = commentRepository.save(CommentConverter.toComment(request, post, user));
        return CommentConverter.toDetailDTO(comment);
    }

    // 댓글 목록 조회 (ACTIVE 댓글만, 작성순)
    public CommentResponseDTO.ListDTO getCommentList(Long postId) {
        Post post = findActivePost(postId);
        List<Comment> comments = commentRepository.findAllByPostAndStatusOrderByIdAsc(post, CommentStatus.ACTIVE);
        return CommentConverter.toListDTO(postId, comments);
    }

    // 숨김 게시글은 없는 게시글과 같이 취급 (404)
    private Post findActivePost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.POST_404_1));
        if (post.isHidden()) {
            throw new GeneralException(ErrorStatus.POST_404_1);
        }
        return post;
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_404_1));
    }
}
