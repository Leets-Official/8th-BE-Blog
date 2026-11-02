package com.leets.mission.domain.report.handler;

import com.leets.mission.domain.post.entity.Post;
import com.leets.mission.domain.post.repository.PostRepository;
import com.leets.mission.domain.report.entity.ReportTargetType;
import com.leets.mission.global.apiPayload.code.ErrorStatus;
import com.leets.mission.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

// 게시글 신고 대상 처리
@Component
@RequiredArgsConstructor
public class PostReportTargetHandler implements ReportTargetHandler {

    private final PostRepository postRepository;

    @Override
    public ReportTargetType getTargetType() {
        return ReportTargetType.POST;
    }

    // 숨김 게시글은 없는 게시글과 같이 취급 (404)
    @Override
    public Long getActiveAuthorId(Long postId) {
        Post post = postRepository.findById(postId)
                .filter(p -> !p.isHidden())
                .orElseThrow(() -> new GeneralException(ErrorStatus.POST_404_1));
        return post.getUser().getId();
    }

    @Override
    public void hide(Long postId) {
        postRepository.findById(postId)
                .filter(p -> !p.isHidden())
                .ifPresent(Post::hide);
    }
}
