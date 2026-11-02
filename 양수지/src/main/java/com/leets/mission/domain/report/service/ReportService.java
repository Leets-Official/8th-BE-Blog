package com.leets.mission.domain.report.service;

import com.leets.mission.domain.report.converter.ReportConverter;
import com.leets.mission.domain.report.dto.ReportRequestDTO;
import com.leets.mission.domain.report.dto.ReportResponseDTO;
import com.leets.mission.domain.report.entity.Report;
import com.leets.mission.domain.report.entity.ReportTargetType;
import com.leets.mission.domain.report.handler.ReportTargetHandler;
import com.leets.mission.domain.report.repository.ReportRepository;
import com.leets.mission.domain.user.entity.User;
import com.leets.mission.domain.user.repository.UserRepository;
import com.leets.mission.global.apiPayload.code.ErrorStatus;
import com.leets.mission.global.apiPayload.exception.GeneralException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final Map<ReportTargetType, ReportTargetHandler> handlers;

    // 등록된 핸들러를 신고 대상 종류별로 묶어 둠
    public ReportService(ReportRepository reportRepository, UserRepository userRepository,
                         List<ReportTargetHandler> handlers) {
        this.reportRepository = reportRepository;
        this.userRepository = userRepository;
        this.handlers = handlers.stream()
                .collect(Collectors.toMap(ReportTargetHandler::getTargetType, Function.identity()));
    }

    // 신고 생성 (본인 글/댓글 신고 불가)
    @Transactional
    public ReportResponseDTO.DetailDTO report(ReportTargetType targetType, Long targetId,
                                              ReportRequestDTO.CreateDTO request) {
        Long authorId = handlers.get(targetType).getActiveAuthorId(targetId);
        User reporter = findUser(request.userId());
        if (authorId.equals(reporter.getId())) {
            throw new GeneralException(ErrorStatus.REPORT_400_1);
        }
        return createReport(request, reporter, targetType, targetId);
    }

    // 신고 처리 (PENDING -> RESOLVED/REJECTED), 승인 시 신고 대상을 숨김
    @Transactional
    public ReportResponseDTO.DetailDTO processReport(Long reportId, ReportRequestDTO.ProcessDTO request) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.REPORT_404_1));
        report.process(request.status()); //이미 처리된 신고면 409
        if (report.isResolved()) {
            hideTarget(report);
        }
        try {
            // 다른 요청이 먼저 처리했으면 version이 달라 UPDATE가 실패
            reportRepository.flush();
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new GeneralException(ErrorStatus.REPORT_409_2);
        }
        return ReportConverter.toDetailDTO(report);
    }

    private void hideTarget(Report report) {
        handlers.get(report.getTargetType()).hide(report.getTargetId());
    }

    // 같은 사용자가 같은 대상을 이미 신고했으면 409
    private ReportResponseDTO.DetailDTO createReport(ReportRequestDTO.CreateDTO request, User reporter,
                                                     ReportTargetType targetType, Long targetId) {
        if (reportRepository.existsByReporterAndTargetTypeAndTargetId(reporter, targetType, targetId)) {
            throw new GeneralException(ErrorStatus.REPORT_409_1);
        }
        try {
            // 동시 요청이 위 확인을 함께 통과해도 DB Unique 제약조건이 막도록 바로 INSERT
            Report report = reportRepository.saveAndFlush(ReportConverter.toReport(request, reporter, targetType, targetId));
            return ReportConverter.toDetailDTO(report);
        } catch (DataIntegrityViolationException e) {
            throw new GeneralException(ErrorStatus.REPORT_409_1);
        }
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_404_1));
    }
}
