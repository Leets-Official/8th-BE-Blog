package com.leets.mission.global.config;

import com.leets.mission.global.apiPayload.code.ErrorStatus;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI().info(new Info().title("Blog API").version("v1"));
    }

    // 응답 설명(description)에 적힌 에러 코드를 읽어서 코드별 에러 예시를 문서에 추가
    @Bean
    public OperationCustomizer errorExampleCustomizer() {
        return (operation, handlerMethod) -> {
            operation.getResponses().values().forEach(this::addErrorExamples);
            return operation;
        };
    }

    private void addErrorExamples(ApiResponse response) {
        if (response.getContent() == null || response.getDescription() == null) {
            return;
        }
        for (ErrorStatus error : ErrorStatus.values()) {
            if (response.getDescription().contains(error.getCode())) {
                Example example = new Example().summary(error.getMessage()).value(toErrorBody(error));
                response.getContent().values()
                        .forEach(mediaType -> mediaType.addExamples(error.getCode(), example));
            }
        }
    }

    // 실제 에러 응답과 같은 모양의 예시 (result는 입력값 검증 실패 때만 포함)
    private Map<String, Object> toErrorBody(ErrorStatus error) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("isSuccess", false);
        body.put("code", error.getCode());
        body.put("message", error.getMessage());
        if (error == ErrorStatus.COMMON_400_1) {
            body.put("result", Map.of("필드명", "오류 메시지"));
        }
        return body;
    }
}
