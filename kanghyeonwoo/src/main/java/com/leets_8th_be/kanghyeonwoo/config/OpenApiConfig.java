package com.leets_8th_be.kanghyeonwoo.config;

import com.leets_8th_be.kanghyeonwoo.shared.web.ApiResponseCode;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .components(new Components()
                        .addSchemas("ApiResponseCode", apiResponseCodeSchema())
                        .addSchemas("ApiResponseVoid", apiResponseVoidSchema())
                        .addResponses("BadRequest", errorResponse(HttpStatus.BAD_REQUEST, "요청 값이 올바르지 않습니다.",
                                ApiResponseCode.INVALID_INPUT_VALUE, ApiResponseCode.TYPE_MISMATCH,
                                ApiResponseCode.MISSING_REQUEST_BODY))
                        .addResponses("Forbidden", errorResponse(HttpStatus.FORBIDDEN, "요청 권한이 없습니다.",
                                ApiResponseCode.POST_ACCESS_DENIED, ApiResponseCode.SELF_REPORT_FORBIDDEN))
                        .addResponses("NotFound", errorResponse(HttpStatus.NOT_FOUND, "요청 대상 리소스를 찾을 수 없습니다.",
                                ApiResponseCode.USER_NOT_FOUND, ApiResponseCode.POST_NOT_FOUND,
                                ApiResponseCode.COMMENT_NOT_FOUND, ApiResponseCode.REPORT_NOT_FOUND,
                                ApiResponseCode.SEARCH_KEYWORD_NOT_FOUND, ApiResponseCode.ENDPOINT_NOT_FOUND))
                        .addResponses("Conflict", errorResponse(HttpStatus.CONFLICT, "요청한 작업이 현재 리소스 상태와 충돌합니다.",
                                ApiResponseCode.DUPLICATE_RESOURCE, ApiResponseCode.REPORT_ALREADY_RESOLVED,
                                ApiResponseCode.RESOURCE_DELETED))
                        .addResponses("MethodNotAllowed", errorResponse(HttpStatus.METHOD_NOT_ALLOWED,
                                "허용되지 않은 HTTP 메서드입니다.", ApiResponseCode.METHOD_NOT_ALLOWED))
                        .addResponses("InternalServerError", errorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                                "처리되지 않은 서버 오류입니다.", ApiResponseCode.INTERNAL_SERVER_ERROR)))
                .info(new Info()
                        .title("Leets 8th Blog RESTful API")
                        .description("Leets 8기 백엔드 블로그 서비스 API 명세서입니다.")
                        .version("2.0.0")
                        .contact(new Contact()
                                .name("강현우")
                                .email("wooddang@example.com")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("로컬 개발 서버")
                ));
    }

    private static Schema<?> apiResponseCodeSchema() {
        return enumSchema(ApiResponseCode.values(), "응답 의미 코드");
    }

    private static Schema<?> enumSchema(Enum<?>[] values, String description) {
        StringSchema schema = new StringSchema();
        schema.setDescription(description);
        schema.setEnum(java.util.Arrays.stream(values).map(Enum::name).toList());
        return schema;
    }

    private static Schema<?> apiResponseVoidSchema() {
        Schema<?> codeSchema = new Schema<>().$ref("#/components/schemas/ApiResponseCode");
        return new ObjectSchema()
                .description("오류 응답에 사용하는 ApiResponse<Void> 형식입니다.")
                .addProperty("success", new BooleanSchema().description("성공 여부").example(false))
                .addProperty("code", codeSchema)
                .addProperty("message", new StringSchema().description("오류별 설명 메시지"))
                .addProperty("data", new Schema<>().description("오류 응답은 null").nullable(true).example(null));
    }

    private static io.swagger.v3.oas.models.responses.ApiResponse errorResponse(HttpStatus expectedStatus,
                                                                                String description,
                                                                                ApiResponseCode... responseCodes) {
        MediaType mediaType = new MediaType()
                .schema(new Schema<>().$ref("#/components/schemas/ApiResponseVoid"));
        for (ApiResponseCode responseCode : responseCodes) {
            if (responseCode.getHttpStatus() != expectedStatus) {
                throw new IllegalArgumentException(responseCode + " does not map to " + expectedStatus);
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
        return new io.swagger.v3.oas.models.responses.ApiResponse()
                .description(description)
                .content(new Content().addMediaType("application/json", mediaType));
    }
}
