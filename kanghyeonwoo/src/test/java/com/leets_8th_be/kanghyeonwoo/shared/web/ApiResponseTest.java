package com.leets_8th_be.kanghyeonwoo.shared.web;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApiResponseTest {

    @Test
    @DisplayName("의미 코드 enum과 데이터로 성공 응답을 생성할 수 있다.")
    void successWithEnumCode() {
        ApiResponse<String> response = ApiResponse.success(ApiResponseCode.SUCCESS, "성공 메시지", "data");

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getCode()).isEqualTo(ApiResponseCode.SUCCESS);
        assertThat(response.getMessage()).isEqualTo("성공 메시지");
        assertThat(response.getData()).isEqualTo("data");
    }

    @Test
    @DisplayName("기본 성공 응답은 SUCCESS 코드를 사용한다.")
    void successWithDefaultCode() {
        ApiResponse<String> response = ApiResponse.success("data");

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getCode()).isEqualTo(ApiResponseCode.SUCCESS);
        assertThat(response.getData()).isEqualTo("data");
    }

    @Test
    @DisplayName("의미 코드 enum으로 실패 응답을 생성할 수 있다.")
    void failWithEnumCode() {
        ApiResponse<Void> response = ApiResponse.fail(ApiResponseCode.POST_NOT_FOUND, "해당 ID의 게시글을 찾을 수 없습니다.");

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getCode()).isEqualTo(ApiResponseCode.POST_NOT_FOUND);
        assertThat(response.getMessage()).isEqualTo("해당 ID의 게시글을 찾을 수 없습니다.");
        assertThat(response.getData()).isNull();
    }

    @Test
    @DisplayName("응답 코드 enum은 HTTP JSON 본문에서 의미 코드 문자열로 직렬화된다.")
    void codeSerializesAsSemanticString() throws Exception {
        var objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();

        String json = objectMapper.writeValueAsString(ApiResponse.success("ok"));

        assertThat(json).contains("\"code\":\"SUCCESS\"");
    }
}
