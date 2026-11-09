package com.leets_8th_be.kanghyeonwoo.shared.web;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.springframework.data.domain.Page;

@Schema(description = "페이지 단위 목록 응답")
public record PageResponse<T>(
        @Schema(description = "현재 페이지의 항목") List<T> items,
        @Schema(description = "현재 페이지 번호 (1부터 시작)", example = "1", minimum = "1") int page,
        @Schema(description = "페이지 크기", example = "10", minimum = "1", maximum = "100") int size,
        @Schema(description = "전체 항목 수", example = "42", minimum = "0") long totalElements,
        @Schema(description = "전체 페이지 수", example = "5", minimum = "0") int totalPages) {
    public static <T> PageResponse<T> from(Page<T> result) {
        return new PageResponse<>(result.getContent(), result.getNumber() + 1,
                result.getSize(), result.getTotalElements(), result.getTotalPages());
    }
}
