package com.leets.blog.domain.comment.service;

import com.leets.blog.common.exception.CustomException;
import com.leets.blog.common.exception.ErrorCode;
import com.leets.blog.domain.comment.entity.Comment;
import com.leets.blog.domain.comment.repository.CommentRepository;
import com.leets.blog.domain.comment.web.dto.request.CommentRequest;
import com.leets.blog.domain.comment.web.dto.response.CommentDeleteResponse;
import com.leets.blog.domain.comment.web.dto.response.CommentListResponse;
import com.leets.blog.domain.comment.web.dto.response.CommentResponse;
import com.leets.blog.domain.member.entity.Member;
import com.leets.blog.domain.member.service.MemberReader;
import com.leets.blog.domain.post.entity.Post;
import com.leets.blog.domain.post.service.PostReader;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private static final int MIN_PAGE_SIZE = 1;
    private static final int MAX_PAGE_SIZE = 50;

    private final CommentRepository commentRepository;
    private final MemberReader memberReader;
    private final PostReader postReader;
    private final CommentReader commentReader;

    // 댓글 작성: 회원(401) → 게시글 ID(400) → 게시글 존재(404, 삭제·숨김 글에는 작성 불가)
    @Transactional
    public CommentResponse createComment(Long memberId, Long postId, CommentRequest request) {
        Member member = memberReader.getActiveMember(memberId);
        Post post = postReader.getVisiblePost(postId);
        Comment comment = commentRepository.save(Comment.builder()
                .member(member)
                .post(post)
                .content(request.content())
                .build());
        return CommentResponse.from(comment);
    }

    // 댓글 목록 조회 (오래된 순): 페이지(400) → 게시글 ID(400) → 게시글 존재(404)
    public CommentListResponse getComments(Long postId, int page, int size) {
        validatePage(page, size);
        Post post = postReader.getVisiblePost(postId);

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt", "id"));
        Page<Comment> result = commentRepository.findByPostIdAndDeletedAtIsNull(post.getId(), pageable);
        return CommentListResponse.from(result);
    }

    // 댓글 수정
    @Transactional
    public CommentResponse updateComment(Long memberId, Long commentId, CommentRequest request) {
        Comment comment = findWritableComment(memberId, commentId);
        comment.update(request.content());
        return CommentResponse.from(comment);
    }

    // 댓글 삭제 (soft delete, 이 댓글에 대한 신고 기록은 유지)
    @Transactional
    public CommentDeleteResponse deleteComment(Long memberId, Long commentId) {
        Comment comment = findWritableComment(memberId, commentId);
        comment.delete();
        return new CommentDeleteResponse(comment.getId());
    }

    private void validatePage(int page, int size) {
        if (page < 1) {
            throw new CustomException(ErrorCode.INVALID_PAGE_NUMBER);
        }
        if (size < MIN_PAGE_SIZE || size > MAX_PAGE_SIZE) {
            throw new CustomException(ErrorCode.INVALID_PAGE_SIZE);
        }
    }

    // 수정·삭제 공통: 회원(401) → 댓글 ID(400) → 댓글 존재(404, 숨김 글의 댓글 포함) → 작성자(403) 순으로 확인
    private Comment findWritableComment(Long memberId, Long commentId) {
        memberReader.getActiveMember(memberId);
        Comment comment = commentReader.getVisibleComment(commentId);
        if (!comment.isWrittenBy(memberId)) {
            throw new CustomException(ErrorCode.COMMENT_ACCESS_DENIED);
        }
        return comment;
    }
}
