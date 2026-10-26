package com.leets.mission.dto.post;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PostUpdateRequest(
        @NotBlank(message = "제목은 필수 항목이며 공백일 수 없습니다.")
        @Size(max = 100, message = "제목은 공백 포함 100자 이하이어야 합니다.")
        String title,

        @NotBlank(message = "본문은 필수 항목이며 공백일 수 없습니다.")
        @Size(max = 100000, message = "본문은 공백 포함 100,000자 이하이어야 합니다.")
        String content
) {}