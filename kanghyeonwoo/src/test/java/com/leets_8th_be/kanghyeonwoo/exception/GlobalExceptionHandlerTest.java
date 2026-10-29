package com.leets_8th_be.kanghyeonwoo.exception;

import com.leets_8th_be.kanghyeonwoo.shared.web.ApiResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("CustomException 발생 시 ErrorCode의 상태 코드와 표준 에러 응답을 반환한다.")
    void handleCustomException() {
        CustomException ex = new CustomException(ErrorCode.POST_NOT_FOUND);

        ResponseEntity<ApiResponse<Void>> response = handler.handleCustomException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getCode()).isEqualTo("POST4001");
        assertThat(response.getBody().getMessage()).isEqualTo("해당 ID의 게시글을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("서버 내부 예외 발생 시 COMMON500 에러 응답을 반환한다.")
    void handleGeneralException() {
        Exception ex = new RuntimeException("Unexpected error");

        ResponseEntity<ApiResponse<Void>> response = handler.handleGeneralException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getCode()).isEqualTo("COMMON500");
    }
}
