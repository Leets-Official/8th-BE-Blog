package com.example.demo.config;

import com.example.demo.global.ErrorCode;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Leets 8th BE Blog API")
                        .version("v1.0.0")
                        .description("""
                                게시글 / 댓글 / 신고 API 문서입니다.

                                - 모든 응답은 공통 형식 `{ isSuccess, code, message, data }` 으로 내려갑니다.
                                - 요청자는 요청 값(userId)이 아니라 Access Token 으로 식별합니다. 먼저 회원가입과 로그인을 하세요.
                                - 로그인으로 받은 Access Token 을 우측 상단 Authorize 에 넣으면 인증이 필요한 API 를 호출할 수 있습니다.
                                - 에러 응답의 `code` 값으로 실패 원인을 구분할 수 있습니다. (예: `REPORT_409_1`)
                                """))
                .components(new Components().addSecuritySchemes("bearerAuth",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }

    // @SecurityRequirement 가 붙은 API 는 401(인증 실패) 응답을 자동으로 문서에 추가한다.
    @Bean
    public OperationCustomizer unauthorizedResponseCustomizer() {
        return (operation, handlerMethod) -> {
            boolean secured = handlerMethod.hasMethodAnnotation(SecurityRequirement.class)
                    || AnnotatedElementUtils.hasAnnotation(handlerMethod.getBeanType(), SecurityRequirement.class);
            if (!secured) {
                return operation;
            }

            ErrorCode code = ErrorCode.AUTHENTICATION_REQUIRED;
            Map<String, Object> example = new LinkedHashMap<>();
            example.put("isSuccess", false);
            example.put("code", code.getCode());
            example.put("message", code.getMessage());
            example.put("data", null);

            Content content = new Content().addMediaType("application/json",
                    new MediaType()
                            .schema(new Schema<Object>().$ref("#/components/schemas/ErrorResponse"))
                            .addExamples(code.getCode(), new Example().value(example)));

            if (operation.getResponses() == null) {
                operation.setResponses(new ApiResponses());
            }
            operation.getResponses().addApiResponse("401",
                    new ApiResponse().description("인증 실패 (토큰 없음, 만료, 변조)").content(content));
            return operation;
        };
    }
}
