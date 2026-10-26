package com.leets_8th_be.kanghyeonwoo.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

class ApiResponseTest {

    @Test
    @DisplayName("문자열 코드(S200)와 데이터로 성공 응답을 생성할 수 있다.")
    void successWithStringCode() {
        ApiResponse<String> response = ApiResponse.success("S200", "성공 메시지", "data");

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getCode()).isEqualTo("S200");
        assertThat(response.getMessage()).isEqualTo("성공 메시지");
        assertThat(response.getData()).isEqualTo("data");
    }

    @Test
    @DisplayName("기존 숫자 코드(200)로 성공 응답을 생성해도 호환된다.")
    void successWithIntCode() {
        ApiResponse<String> response = ApiResponse.success("data");

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getCode()).isEqualTo(200);
        assertThat(response.getData()).isEqualTo("data");
    }

    @Test
    @DisplayName("문자열 에러 코드(POST4001)로 실패 응답을 생성할 수 있다.")
    void failWithStringCode() {
        ApiResponse<Void> response = ApiResponse.fail("POST4001", "해당 ID의 게시글을 찾을 수 없습니다.");

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getCode()).isEqualTo("POST4001");
        assertThat(response.getMessage()).isEqualTo("해당 ID의 게시글을 찾을 수 없습니다.");
        assertThat(response.getData()).isNull();
    }
}
