package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "게시글 신고 요청")
public record ReportRequest(
        @Schema(description = "신고자 ID. 글 작성자 본인은 신고할 수 없음", example = "2")
        @NotNull(message = "userId를 입력해주세요.") Long userId,
        @Schema(description = "신고 사유 (최대 500자)", example = "광고성 게시글입니다.")
        @NotBlank(message = "신고 사유를 입력해주세요.")
        @Size(max = 500, message = "신고 사유는 500자 이하여야 합니다.") String reason
) {}
