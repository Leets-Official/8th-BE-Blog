package org.example._thbe.domain.report.service;


import lombok.RequiredArgsConstructor;
import org.example._thbe.domain.report.dto.ReportListResponse;
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

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportAdminService {

    private final ReportRepository reportRepository;
    private final List<ReportStrategy> strategyList;


    //신고 목록 조회
    public ReportListResponse getReportList(ReportStatus status) {
        return ReportListResponse
                .from(reportRepository.findByStatusWithReporter(status));
    }



    //신고 승인 로직
    @Transactional
    public void approveReport(Long reportId) {

        //신고 승인 처리
        Report report = reportRepository
                .findById(reportId)
                .orElseThrow(()-> new CustomException(ReportErrorCode.REPORT_NOT_FOUND));

        report.changeStatus(ReportStatus.APPROVED);


        //도메인 승인 처리
        ReportStrategy strategy = findStrategy(report.getContentType());
        strategy.approve(report.getContentId());


        //동일 컨텐츠 일괄 승인 처리
        reportRepository.updateReportStatusByContent(ReportStatus.APPROVED
                ,report.getContentType(),
                report.getContentId());

    }

    //신고 반려 로직
    @Transactional
    public void rejectReport(Long reportId) {

        Report report = reportRepository
                .findById(reportId)
                .orElseThrow(()->new CustomException(ReportErrorCode.REPORT_NOT_FOUND));

        report.changeStatus(ReportStatus.REJECTED);

    }




    private ReportStrategy findStrategy(ReportContentType contentType) {
        return strategyList
                .stream()
                .filter((s)->s.isValidType(contentType))
                .findFirst()
                .orElseThrow(()-> new CustomException(ReportErrorCode.UNSUPPORTED_CONTENT_TYPE));
    }







}
