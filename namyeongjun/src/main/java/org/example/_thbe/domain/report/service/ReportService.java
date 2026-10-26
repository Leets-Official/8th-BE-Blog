package org.example._thbe.domain.report.service;


import lombok.RequiredArgsConstructor;
import org.example._thbe.domain.member.service.MemberService;
import org.example._thbe.domain.report.dto.ReportCreateRequest;
import org.example._thbe.domain.report.dto.ReportCreateResponse;
import org.example._thbe.domain.report.entity.Report;
import org.example._thbe.domain.report.enums.ReportContentType;
import org.example._thbe.domain.report.enums.ReportStatus;
import org.example._thbe.domain.report.error.ReportErrorCode;
import org.example._thbe.domain.report.repository.ReportRepository;
import org.example._thbe.domain.report.strategy.ReportStrategy;
import org.example._thbe.global.exception.CustomException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class ReportService {
    private static final int REPORT_THRESHOLD = 10;

    private final ReportRepository reportRepository;
    private final MemberService memberService;

    private final List<ReportStrategy> strategyList;

    //신고 생성 로직
    @Transactional
    public ReportCreateResponse createReport(
            ReportCreateRequest request,
            Long reporterId
    ) {

        //중복 체크
        if(reportRepository.existsByReporterIdAndContentTypeAndContentId(
                reporterId,
                request.contentType(),
                request.contentId()
        )) {
            throw new CustomException(ReportErrorCode.REPORT_ALREADY_EXIST);
        }


        // 자기자신 신고 방지 && 컨텐츠 존재 검증
        ReportStrategy strategy = findStrategy(request.contentType());

        if(strategy.reportedUserId(request.contentId()).equals(reporterId)) {
            throw new CustomException(ReportErrorCode.SELF_REPORT);
        }


        // 신고 생성
        Report report = Report.create(
                request.reason(),
                request.contentType(),
                request.contentId(),
                memberService.getMember(reporterId)
        );



        //일정 횟수 이상 자동 신고 승인
        if(isExceedThreshold(request.contentType(),request.contentId())) {
            report.changeStatus(ReportStatus.APPROVED);
            strategy.approve(request.contentId());
        }

        //DB 저장
        reportRepository.save(report);

        return ReportCreateResponse.from(report);

    }





    //신고 삭제 로직
    @Transactional
    public void deleteReport(Long reportId,Long userId) {
        //신고 존재 여부 검증
        Report report = reportRepository
                .findById(reportId)
                .orElseThrow(()->new CustomException(ReportErrorCode.REPORT_NOT_FOUND));

        //신고자와 사용자 일치 검증
        if(!report.getReporter().getId().equals(userId)) {
            throw new CustomException(ReportErrorCode.REPORT_DELETE_NO_PERMISSION);
        }

        //처리된 신고 삭제 불가
        if(report.getStatus() != ReportStatus.PENDING) {
            throw new CustomException(ReportErrorCode.REPORT_ALREADY_HANDLED);
        }

        reportRepository.delete(report);
    }





    //신고횟수 확인 메서드
    private boolean isExceedThreshold(ReportContentType contentType, Long contentId) {
        long count = reportRepository.countByContentTypeAndContentId(contentType,contentId);

        return count+1 >=REPORT_THRESHOLD;
    }


    // 전략 클래스 탐색 메서드
    private ReportStrategy findStrategy(ReportContentType contentType) {
        return strategyList
                .stream()
                .filter((s)->s.isValidType(contentType))
                .findFirst()
                .orElseThrow(()-> new CustomException(ReportErrorCode.UNSUPPORTED_CONTENT_TYPE));
    }


}
