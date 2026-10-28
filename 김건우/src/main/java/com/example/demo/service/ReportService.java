package com.example.demo.service;

import com.example.demo.dto.ReportRequest;
import com.example.demo.dto.ReportResponse;
import com.example.demo.entity.Post;
import com.example.demo.entity.Report;
import com.example.demo.entity.User;
import com.example.demo.global.CustomException;
import com.example.demo.global.ErrorCode;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.ReportRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private final ReportRepository reportRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    // 게시글 신고 (중복 신고 방지)
    @Transactional
    public ReportResponse createReport(Long postId, ReportRequest request) {
        User reporter = userRepository.findById(request.userId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        // 숨김 글은 일반 사용자에게 존재하지 않는 글
        if (post.isHidden()) {
            throw new CustomException(ErrorCode.POST_NOT_FOUND);
        }
        if (post.getUser().getId().equals(reporter.getId())) {
            throw new CustomException(ErrorCode.SELF_REPORT_NOT_ALLOWED);
        }

        // 1차 방어: 사전 체크로 친절한 에러 반환
        if (reportRepository.existsByPostIdAndReporterId(postId, reporter.getId())) {
            throw new CustomException(ErrorCode.ALREADY_REPORTED);
        }

        // 2차 방어: 동시 요청이 사전 체크를 같이 통과해도 DB 유니크 제약이 막는다
        try {
            Report report = reportRepository.saveAndFlush(Report.builder()
                    .post(post)
                    .reporter(reporter)
                    .reason(request.reason())
                    .build());
            return ReportResponse.from(report);
        } catch (DataIntegrityViolationException e) {
            throw new CustomException(ErrorCode.ALREADY_REPORTED);
        }
    }

    // 신고 처리 완료 (PENDING -> RESOLVED)
    @Transactional
    public ReportResponse resolveReport(Long reportId) {
        Report report = reportRepository.findByIdForUpdate(reportId)
                .orElseThrow(() -> new CustomException(ErrorCode.REPORT_NOT_FOUND));

        report.resolve();
        return ReportResponse.from(report);
    }
}
