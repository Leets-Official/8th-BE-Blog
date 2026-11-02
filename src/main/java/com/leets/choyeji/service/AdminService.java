package com.leets.choyeji.service;

import com.leets.choyeji.dto.AdminReportListResponse;
import com.leets.choyeji.dto.AdminReportResponse;
import com.leets.choyeji.dto.PostResponse;
import com.leets.choyeji.dto.ReportResolveRequest;
import com.leets.choyeji.entity.Member;
import com.leets.choyeji.entity.Post;
import com.leets.choyeji.entity.Report;
import com.leets.choyeji.entity.ReportResult;
import com.leets.choyeji.entity.ReportStatus;
import com.leets.choyeji.exception.ApiException;
import com.leets.choyeji.repository.MemberRepository;
import com.leets.choyeji.repository.PostRepository;
import com.leets.choyeji.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {
    private final MemberRepository memberRepository;
    private final PostRepository postRepository;
    private final ReportRepository reportRepository;

    public AdminReportListResponse listReports(Long memberId, ReportStatus status, int page, int size) {
        requireAdmin(memberId);
        PageRequest pageable = PageRequest.of(Math.max(0, page), Math.max(1, Math.min(100, size)),
                Sort.by(Sort.Direction.DESC, "id"));
        Page<Report> reports = status == null ? reportRepository.findAll(pageable)
                : reportRepository.findAllByStatus(status, pageable);
        return AdminReportListResponse.from(reports.map(AdminReportResponse::from));
    }

    @Transactional
    public AdminReportResponse resolveReport(Long memberId, Long reportId, ReportResolveRequest request) {
        requireAdmin(memberId);
        Long postId = reportRepository.findPostIdById(reportId).orElseThrow(ApiException::reportNotFound);
        // 생성·수정·삭제·신고 처리·숨김 해제에서 모두 게시글을 먼저 잠가 순서를 통일합니다.
        Post post = postRepository.findByIdForUpdate(postId).orElseThrow(ApiException::reportNotFound);
        Report report = reportRepository.findByIdForUpdate(reportId).orElseThrow(ApiException::reportNotFound);
        report.resolve(request.result());
        if (request.result() == ReportResult.ACCEPTED) {
            post.hide();
        }
        reportRepository.flush();
        return AdminReportResponse.from(report);
    }

    @Transactional
    public PostResponse restorePost(Long memberId, Long postId) {
        requireAdmin(memberId);
        Post post = postRepository.findByIdForUpdate(postId).orElseThrow(ApiException::postNotFound);
        post.restore();
        return PostResponse.from(post);
    }

    private void requireAdmin(Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(ApiException::unauthorized);
        if (!member.isAdmin()) {
            throw ApiException.adminRequired();
        }
    }
}
