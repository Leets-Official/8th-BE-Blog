package com.leets.blog.global.swagger;

import com.leets.blog.global.exception.ErrorCode;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
public class SwaggerConfig {

    // 컨트롤러의 @SecurityRequirement에서 이 이름으로 인증 방식을 참조한다.
    public static final String BEARER_AUTH = "bearerAuth";

    private static final String ERROR_SCHEMA = "ErrorResponse";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Leets 8th BE Blog API")
                        .version("v1")
                        .description("""
                                회원가입/로그인, 게시글 CRUD, 신고, 게시글 숨김 API 문서입니다.

                                - 모든 응답은 `success`, `code`, `message`, `data` 형식의 공통 응답으로 내려갑니다.
                                - 🔒 표시가 있는 API는 로그인이 필요합니다.
                                  `POST /login`으로 받은 `accessToken`을 오른쪽 위 **Authorize** 버튼에 넣고 호출하세요.
                                - 서버를 켜면 테스트용 회원이 미리 등록되어 있습니다.
                                  (`hyunbo@leets.com` / `leets@leets.com`, 비밀번호 모두 `password1234`)
                                """))
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("로그인으로 받은 Access Token을 입력하세요. (Bearer는 빼고 토큰만)"))
                        .addSchemas(ERROR_SCHEMA, errorSchema()));
    }

    // 문서에 보이는 그룹 순서를 정한다. (그룹 이름과 설명은 각 컨트롤러의 @Tag에 있다)
    @Bean
    public OpenApiCustomizer tagOrderCustomizer() {
        List<String> order = List.of("인증", "게시글", "신고", "1주차 과제");
        return openApi -> openApi.getTags().sort(Comparator.comparingInt(tag -> order.indexOf(tag.getName())));
    }

    // @ApiErrorCodes에 적은 에러 코드를 HTTP 상태별로 묶어서 실패 응답과 예시를 문서에 추가한다.
    @Bean
    public OperationCustomizer errorCodeCustomizer() {
        return (operation, handlerMethod) -> {
            ApiErrorCodes errorCodes = handlerMethod.getMethodAnnotation(ApiErrorCodes.class);
            if (errorCodes == null) {
                return operation;
            }

            Map<HttpStatus, List<ErrorCode>> codesByStatus = Arrays.stream(errorCodes.value())
                    .collect(Collectors.groupingBy(ErrorCode::getStatus, LinkedHashMap::new, Collectors.toList()));
            codesByStatus.forEach((status, codes) ->
                    operation.getResponses().addApiResponse(String.valueOf(status.value()), toApiResponse(codes)));
            return operation;
        };
    }

    private ApiResponse toApiResponse(List<ErrorCode> codes) {
        MediaType mediaType = new MediaType().schema(new Schema<>().$ref("#/components/schemas/" + ERROR_SCHEMA));
        codes.forEach(code -> mediaType.addExamples(code.getCode(),
                new Example().summary(code.getMessage()).value(errorBody(code))));

        String description = codes.stream()
                .map(code -> code.getCode() + ": " + code.getMessage())
                .collect(Collectors.joining("<br>"));
        return new ApiResponse()
                .description(description)
                .content(new Content().addMediaType(org.springframework.http.MediaType.APPLICATION_JSON_VALUE, mediaType));
    }

    private Map<String, Object> errorBody(ErrorCode code) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", false);
        body.put("code", code.getCode());
        body.put("message", code.getMessage());
        // 입력값 검증 실패만 어떤 필드가 왜 틀렸는지 data에 담아 내려준다.
        body.put("data", code == ErrorCode.INVALID_INPUT
                ? List.of(Map.of("field", "title", "reason", "제목은 필수 입력 항목입니다."))
                : null);
        return body;
    }

    private Schema<?> errorSchema() {
        return new ObjectSchema()
                .description("실패 응답 공통 형식")
                .addProperty("success", new BooleanSchema().description("성공 여부 (실패 시 항상 false)").example(false))
                .addProperty("code", new StringSchema().description("에러 코드").example("POST4001"))
                .addProperty("message", new StringSchema().description("에러 메시지").example("해당 ID의 게시글을 찾을 수 없습니다."))
                .addProperty("data", new Schema<>()
                        .description("입력값 검증 실패(COMMON400_VALIDATION) 시 필드별 에러 목록, 그 외에는 null")
                        .nullable(true));
    }
}
