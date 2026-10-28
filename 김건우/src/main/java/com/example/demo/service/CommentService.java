package com.example.demo.service;

import com.example.demo.dto.CommentRequest;
import com.example.demo.dto.CommentResponse;
import com.example.demo.entity.Comment;
import com.example.demo.entity.Post;
import com.example.demo.entity.User;
import com.example.demo.global.CustomException;
import com.example.demo.global.ErrorCode;
import com.example.demo.repository.CommentRepository;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    // 댓글 작성 (채택 기능을 검증하기 위한 최소 API)
    @Transactional
    public CommentResponse createComment(Long postId, CommentRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
        if (post.isHidden()) {
            throw new CustomException(ErrorCode.POST_NOT_FOUND);
        }

        Comment comment = commentRepository.save(Comment.builder()
                .post(post)
                .user(user)
                .content(request.content())
                .build());
        return CommentResponse.from(comment);
    }

    // 댓글 채택 (게시글당 1개 제한)
    @Transactional
    public CommentResponse acceptComment(Long postId, Long commentId, Long userId) {
        // 1) 게시글 행을 "가장 먼저" 잠근다.
        //    같은 게시글에 대한 채택 요청이 직렬화되어 아래 exists 체크가 안전해진다.
        //    (MySQL REPEATABLE READ 에서는 잠금 이후에 첫 일반 SELECT 가 나가야 최신 커밋이 보임)
        Post post = postRepository.findByIdForUpdate(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        if (post.isHidden()) {
            throw new CustomException(ErrorCode.POST_NOT_FOUND);
        }
        if (!post.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.NOT_POST_OWNER);
        }

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));
        // URL 의 postId 와 댓글의 실제 게시글이 다르면 존재하지 않는 것으로 취급
        if (!comment.getPost().getId().equals(postId)) {
            throw new CustomException(ErrorCode.COMMENT_NOT_FOUND);
        }
        if (comment.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.SELF_ACCEPT_NOT_ALLOWED);
        }
        if (comment.isAccepted()) {
            throw new CustomException(ErrorCode.COMMENT_ALREADY_ACCEPTED);
        }
        if (commentRepository.existsByPostIdAndAcceptedTrue(postId)) {
            throw new CustomException(ErrorCode.ANOTHER_COMMENT_ACCEPTED);
        }

        comment.accept();
        return CommentResponse.from(comment);
    }
}
