package com.leets_8th_be.kanghyeonwoo.shared.web;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
    @DisplayName("기본 성공 응답도 문자열 코드(S200)를 사용한다.")
    void successWithDefaultCode() {
        ApiResponse<String> response = ApiResponse.success("data");

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getCode()).isEqualTo("S200");
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
