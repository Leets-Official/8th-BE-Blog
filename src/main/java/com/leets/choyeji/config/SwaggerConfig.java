package com.leets.choyeji.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP,
        scheme = "bearer", bearerFormat = "JWT")
public class SwaggerConfig {
    @Bean
    public OperationCustomizer errorResponseCustomizer(ObjectMapper objectMapper) {
        return (operation, handlerMethod) -> {
            ApiErrorResponses documentation = handlerMethod.getMethodAnnotation(ApiErrorResponses.class);
            if (documentation == null) {
                return operation;
            }
            Map<String, ApiResponse> errors = new LinkedHashMap<>();
            for (SwaggerError error : documentation.value()) {
                ApiResponse response = errors.computeIfAbsent(error.responseCode(), code -> new ApiResponse()
                        .description("").content(new Content().addMediaType("application/json",
                                new MediaType().schema(errorSchema()))));
                response.setDescription(response.getDescription().isEmpty() ? error.description()
                        : response.getDescription() + "\n\n" + error.description());
                response.getContent().get("application/json").addExamples(error.name(),
                        new Example().summary(error.description()).value(objectMapper.valueToTree(error.example())));
            }
            if (operation.getResponses() == null) {
                operation.setResponses(new ApiResponses());
            }
            errors.forEach(operation.getResponses()::addApiResponse);
            return operation;
        };
    }

    private Schema<?> errorSchema() {
        ObjectSchema data = new ObjectSchema();
        data.setNullable(true);
        data.setAdditionalProperties(new StringSchema());
        data.setDescription("입력 검증 실패 시 필드별 메시지, 그 외에는 null");
        ObjectSchema schema = new ObjectSchema();
        schema.addProperty("isSuccess", new BooleanSchema().example(false));
        schema.addProperty("code", new StringSchema().description("오류 코드"));
        schema.addProperty("message", new StringSchema().description("오류 메시지"));
        schema.addProperty("data", data);
        schema.setRequired(List.of("isSuccess", "code", "message", "data"));
        return schema;
    }
}
