package com.leets.mission.domain.report.dto;

import com.leets.mission.domain.report.entity.ReportStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// 신고 요청 DTO 모음
public class ReportRequestDTO {

    // 신고 생성 요청 (게시글/댓글 공통)
    public record CreateDTO(
            @NotNull(message = "사용자 ID를 입력해주세요.")
            Long userId,

            @NotBlank(message = "신고 사유를 입력해주세요.")
            @Size(max = 200, message = "신고 사유는 200자 이하로 입력해주세요.")
            String reason
    ) {
    }

    // 신고 처리 요청 (RESOLVED: 승인 → 대상 숨김, REJECTED: 반려)
    public record ProcessDTO(
            @NotNull(message = "처리 결과(RESOLVED 또는 REJECTED)를 입력해주세요.")
            ReportStatus status
    ) {
    }
}
