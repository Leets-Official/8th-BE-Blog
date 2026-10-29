package org.example._thbe.domain.report.strategy;


import lombok.RequiredArgsConstructor;
import org.example._thbe.domain.post.entity.Post;
import org.example._thbe.domain.post.error.PostErrorCode;
import org.example._thbe.domain.post.repository.PostRepository;
import org.example._thbe.domain.report.enums.ReportContentType;
import org.example._thbe.global.exception.CustomException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostReportStrategy implements ReportStrategy {

    private final PostRepository postRepository;

    //컨텐츠 타입 확인
    public boolean isValidType(ReportContentType reportContentType) {
        return ReportContentType.POST == reportContentType;
    }


    //신고 당한 유저 아이디 반환 && 컨텐츠 존재 여부 확인
    public Long reportedUserId(Long postId) {
        return postRepository
                .findById(postId)
                .orElseThrow(()-> new CustomException(PostErrorCode.POST_NOT_FOUND))
                .getMember()
                .getId();
    }

    //신고 승인 시 게시글 삭제
    public void approve(Long postId) {
        Post post = postRepository
                .findById(postId)
                .orElseThrow(() -> new CustomException(PostErrorCode.POST_NOT_FOUND));
        postRepository.delete(post);
    }



}
