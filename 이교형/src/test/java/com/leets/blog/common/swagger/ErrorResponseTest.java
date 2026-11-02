package com.leets.blog.common.swagger;

import com.leets.blog.common.response.ApiResponse;
import org.junit.jupiter.api.Test;

import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorResponseTest {

    // ApiResponse 필드를 바꾸면 문서용 ErrorResponse도 같이 바꿔야 함을 알려줌
    @Test
    void 문서용_ErrorResponse는_실제_ApiResponse와_필드_구성이_같다() {
        assertThat(fieldNames(ErrorResponse.class)).isEqualTo(fieldNames(ApiResponse.class));
    }

    private List<String> fieldNames(Class<? extends Record> type) {
        return Arrays.stream(type.getRecordComponents()).map(RecordComponent::getName).toList();
    }
}
