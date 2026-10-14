package com.example.demo.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// 부분 수정(PATCH): title, content 는 null 허용 (null = 수정 안 함)
// (?s) 는 줄바꿈이 있는 본문도 통과시키기 위한 옵션
public record PostPatchRequest(
        @NotNull(message = "userId를 입력해주세요.") Long userId,
        @Size(max = 255, message = "제목은 255자 이하여야 합니다.")
        @Pattern(regexp = "(?s).*\\S.*", message = "제목은 공백일 수 없습니다.") String title,
        @Pattern(regexp = "(?s).*\\S.*", message = "내용은 공백일 수 없습니다.") String content
) {}