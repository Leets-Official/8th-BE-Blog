package com.leets_8th_be.kanghyeonwoo.config;

import com.leets_8th_be.kanghyeonwoo.shared.web.ApiErrorCodes;
import com.leets_8th_be.kanghyeonwoo.shared.web.ApiResponseCode;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.servers.Server;

import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList("BearerAuth"))
                .components(
                        new Components()
                                .addSchemas("ApiResponseCode", apiResponseCodeSchema())
                                .addSchemas("ApiResponseVoid", apiResponseVoidSchema()))
                .info(
                        new Info()
                                .title("Leets 8th Blog RESTful API")
                                .description(
                                        """
                                        Leets 8기 백엔드 블로그 서비스 API 명세서입니다.

                                        ### 공통 라우팅 오류
                                        - 등록되지 않은 경로: HTTP 404, `ENDPOINT_NOT_FOUND`
                                        - 지원하지 않는 HTTP 메서드: HTTP 405, `METHOD_NOT_ALLOWED`

                                        오류 응답은 `success: false`, 의미 코드인 `code`, 설명 `message`,
                                        `data: null`로 구성됩니다. API별 예시는 기본 메시지를 사용하며,
                                        실제 메시지는 입력값과 오류 상황에 따라 달라질 수 있습니다.
                                        """)
                                .version("2.0.0")
                                .contact(new Contact().name("강현우").email("wooddang@example.com")))
                .servers(
                        List.of(new Server().url("http://localhost:8080").description("로컬 개발 서버")));
    }

    @Bean
    public OperationCustomizer apiErrorExamples() {
        return (operation, handlerMethod) -> {
            ApiErrorCodes annotation = handlerMethod.getMethodAnnotation(ApiErrorCodes.class);
            if (annotation == null) {
                return operation;
            }
            Map<HttpStatus, List<ApiResponseCode>> byStatus = new LinkedHashMap<>();
            for (ApiResponseCode code : annotation.value()) {
                byStatus.computeIfAbsent(code.getHttpStatus(), ignored -> new ArrayList<>())
                        .add(code);
            }
            byStatus.computeIfAbsent(HttpStatus.INTERNAL_SERVER_ERROR, ignored -> new ArrayList<>())
                    .add(ApiResponseCode.INTERNAL_SERVER_ERROR);
            byStatus.forEach(
                    (status, codes) ->
                            operation
                                    .getResponses()
                                    .addApiResponse(
                                            String.valueOf(status.value()),
                                            errorResponse(status, codes)));
            return operation;
        };
    }

    private static Schema<?> apiResponseCodeSchema() {
        return enumSchema(ApiResponseCode.values(), "응답 의미 코드");
    }

    private static Schema<?> enumSchema(Enum<?>[] values, String description) {
        StringSchema schema = new StringSchema();
        schema.setDescription(description);
        schema.setEnum(Arrays.stream(values).map(Enum::name).toList());
        return schema;
    }

    private static Schema<?> apiResponseVoidSchema() {
        Schema<?> codeSchema = new Schema<>().$ref("#/components/schemas/ApiResponseCode");
        return new ObjectSchema()
                .description("오류 응답에 사용하는 ApiResponse<Void> 형식입니다.")
                .addProperty("success", new BooleanSchema().description("성공 여부").example(false))
                .addProperty("code", codeSchema)
                .addProperty("message", new StringSchema().description("오류별 설명 메시지"))
                .addProperty(
                        "data",
                        new Schema<>().description("오류 응답은 null").nullable(true).example(null));
    }

    private static ApiResponse errorResponse(
            HttpStatus expectedStatus, List<ApiResponseCode> responseCodes) {
        MediaType mediaType =
                new MediaType().schema(new Schema<>().$ref("#/components/schemas/ApiResponseVoid"));
        for (ApiResponseCode responseCode : responseCodes) {
            if (responseCode.getHttpStatus() != expectedStatus) {
                throw new IllegalArgumentException(
                        responseCode + " does not map to " + expectedStatus);
            }
            String code = responseCode.name();
            String message = responseCode.getDefaultMessage();
            Map<String, Object> value = new LinkedHashMap<>();
            value.put("success", false);
            value.put("code", code);
            value.put("message", message);
            value.put("data", null);
            mediaType.addExamples(code, new Example().summary(message).value(value));
        }
        return new ApiResponse()
                .description(expectedStatus.getReasonPhrase())
                .content(new Content().addMediaType("application/json", mediaType));
    }
}
