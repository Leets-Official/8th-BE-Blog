package com.leets.blog.domain.post.service;

import com.leets.blog.common.exception.CustomException;
import com.leets.blog.common.exception.ErrorCode;
import com.leets.blog.domain.post.entity.Post;
import com.leets.blog.domain.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

// 여러 서비스가 함께 쓰는 게시글 조회 (호출한 서비스의 트랜잭션 안에서 실행)
@Component
@RequiredArgsConstructor
public class PostReader {

    private final PostRepository postRepository;

    // 0 이하 ID는 400, 없거나 삭제·숨김된 글은 404 (숨김 글은 작성자에게도 없는 글로 취급)
    public Post getVisiblePost(Long postId) {
        Post post = getPostIncludingHidden(postId);
        if (post.isHidden()) {
            throw new CustomException(ErrorCode.POST_NOT_FOUND);
        }
        return post;
    }

    // 관리자 숨김 처리용: 숨김 글도 찾아야 ALREADY_HIDDEN(409)을 줄 수 있음 (일반 조회에는 getVisiblePost 사용)
    public Post getPostIncludingHidden(Long postId) {
        if (postId < 1) {
            throw new CustomException(ErrorCode.INVALID_POST_ID);
        }
        return postRepository.findByIdAndDeletedAtIsNull(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
    }
}
