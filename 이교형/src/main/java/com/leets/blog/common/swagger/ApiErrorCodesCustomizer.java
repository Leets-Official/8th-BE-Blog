package com.leets.blog.common.swagger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leets.blog.common.exception.ErrorCode;
import com.leets.blog.common.response.ApiResponse;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

// @ApiErrorCodes에 적은 ErrorCode로 400/404/409 등 에러 응답 문서를 만듦
@Component
@RequiredArgsConstructor
public class ApiErrorCodesCustomizer implements OperationCustomizer {

    private static final String ERROR_SCHEMA_REF = "#/components/schemas/ErrorResponse";

    // 실제 응답 JSON을 만드는 스프링의 ObjectMapper
    private final ObjectMapper objectMapper;

    @Override
    public Operation customize(Operation operation, HandlerMethod handlerMethod) {
        ApiErrorCodes annotation = handlerMethod.getMethodAnnotation(ApiErrorCodes.class);
        if (annotation == null) {
            return operation;
        }

        // 같은 상태 코드의 에러는 한 응답 안에 예시 여러 개로 묶음 (TreeMap: 상태 코드 순)
        Map<HttpStatus, List<ErrorCode>> byStatus = Arrays.stream(annotation.value())
                .collect(Collectors.groupingBy(ErrorCode::getStatus, TreeMap::new, Collectors.toList()));

        byStatus.forEach((status, errorCodes) -> operation.getResponses()
                .addApiResponse(String.valueOf(status.value()), toResponse(status, errorCodes)));
        return operation;
    }

    private io.swagger.v3.oas.models.responses.ApiResponse toResponse(HttpStatus status, List<ErrorCode> errorCodes) {
        MediaType mediaType = new MediaType().schema(new Schema<>().$ref(ERROR_SCHEMA_REF));

        // 예시는 실제 응답을 만드는 ApiResponse.fail()로 만들어 문서와 실제 응답이 어긋나지 않게 함
        // (Swagger 내부 ObjectMapper는 null 필드를 생략해 "data": null이 빠지므로 Map으로 먼저 변환)
        errorCodes.forEach(errorCode -> mediaType.addExamples(errorCode.name(), new Example()
                .summary(errorCode.getMessage())
                .value(objectMapper.convertValue(ApiResponse.fail(errorCode), Map.class))));

        return new io.swagger.v3.oas.models.responses.ApiResponse()
                .description(status.getReasonPhrase())
                .content(new Content().addMediaType("application/json", mediaType));
    }
}
