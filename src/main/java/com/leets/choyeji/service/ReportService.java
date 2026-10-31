package com.leets.choyeji.service;

import com.leets.choyeji.dto.ReportRequest;
import com.leets.choyeji.dto.ReportResponse;
import com.leets.choyeji.entity.Member;
import com.leets.choyeji.entity.Post;
import com.leets.choyeji.entity.Report;
import com.leets.choyeji.exception.ApiException;
import com.leets.choyeji.repository.MemberRepository;
import com.leets.choyeji.repository.PostRepository;
import com.leets.choyeji.repository.ReportRepository;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final ReportRepository reportRepository;
    private final MemberRepository memberRepository;
    private final PostRepository postRepository;

    @Transactional
    public ReportResponse create(Long memberId, Long postId, ReportRequest request) {
        Member member = memberRepository.findById(memberId).orElseThrow(ApiException::unauthorized);
        Post post = postRepository.findById(postId).orElseThrow(ApiException::postNotFound);
        if (reportRepository.existsByPostIdAndMemberId(postId, memberId)) {
            throw ApiException.duplicateReport();
        }
        try {
            Report report = reportRepository.saveAndFlush(new Report(post, member, request.reason()));
            return ReportResponse.from(report);
        } catch (DataIntegrityViolationException exception) {
            // 동시에 들어온 동일 회원의 신고도 DB의 복합 UNIQUE 제약조건으로 차단합니다.
            for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
                if (cause instanceof ConstraintViolationException violation
                        && violation.getConstraintName() != null
                        && violation.getConstraintName().toLowerCase(Locale.ROOT).contains("uk_report_post_member")) {
                    throw ApiException.duplicateReport();
                }
            }
            throw exception;
        }
    }
}
