package com.example.demo.config;

import com.example.demo.dto.response.ErrorResponse;
import com.example.demo.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

import static com.example.demo.exception.ErrorCode.*;

@Configuration
public class OpenApiErrorExamplesConfig {

    @Bean
    public OpenApiCustomizer errorResponseExamples(ObjectMapper objectMapper) {
        Map<String, ErrorCode[]> errorsByOperation = Map.ofEntries(
                Map.entry("signup", new ErrorCode[] {
                        INVALID_REQUEST_FORMAT, INVALID_INPUT, DUPLICATE_MEMBER, UNSUPPORTED_MEDIA_TYPE}),
                Map.entry("login", new ErrorCode[] {
                        INVALID_REQUEST_FORMAT, INVALID_INPUT, INVALID_CREDENTIALS, UNSUPPORTED_MEDIA_TYPE}),
                Map.entry("refresh", new ErrorCode[] {
                        INVALID_REQUEST_FORMAT, INVALID_INPUT, INVALID_REFRESH_TOKEN, UNSUPPORTED_MEDIA_TYPE}),
                Map.entry("findPosts", new ErrorCode[] {AUTHENTICATION_REQUIRED}),
                Map.entry("findPost", new ErrorCode[] {
                        AUTHENTICATION_REQUIRED, INVALID_POST_ID, INVALID_INPUT, POST_ACCESS_DENIED, POST_NOT_FOUND, POST_DELETED}),
                Map.entry("createPost", new ErrorCode[] {
                        AUTHENTICATION_REQUIRED, INVALID_REQUEST_FORMAT, INVALID_INPUT, TITLE_TOO_LONG, MEMBER_NOT_FOUND}),
                Map.entry("updatePost", new ErrorCode[] {
                        AUTHENTICATION_REQUIRED, INVALID_REQUEST_FORMAT, INVALID_INPUT, INVALID_POST_ID,
                        TITLE_REQUIRED, TITLE_TOO_LONG, POST_ACCESS_DENIED, POST_NOT_FOUND, POST_DELETED,
                        CONCURRENT_MODIFICATION}),
                Map.entry("deletePost", new ErrorCode[] {
                        AUTHENTICATION_REQUIRED, INVALID_INPUT, INVALID_POST_ID, POST_ACCESS_DENIED,
                        POST_NOT_FOUND, POST_DELETED, CONCURRENT_MODIFICATION}),
                Map.entry("likePost", new ErrorCode[] {
                        AUTHENTICATION_REQUIRED, INVALID_INPUT, INVALID_POST_ID, MEMBER_NOT_FOUND,
                        POST_NOT_FOUND, POST_DELETED, POST_ACCESS_DENIED, CONCURRENT_MODIFICATION}),
                Map.entry("unlikePost", new ErrorCode[] {
                        AUTHENTICATION_REQUIRED, INVALID_INPUT, INVALID_POST_ID, MEMBER_NOT_FOUND, CONCURRENT_MODIFICATION}),
                Map.entry("likeComment", new ErrorCode[] {
                        AUTHENTICATION_REQUIRED, INVALID_INPUT, MEMBER_NOT_FOUND, COMMENT_NOT_FOUND,
                        POST_DELETED, COMMENT_ACCESS_DENIED, POST_ACCESS_DENIED, CONCURRENT_MODIFICATION}),
                Map.entry("unlikeComment", new ErrorCode[] {
                        AUTHENTICATION_REQUIRED, INVALID_INPUT, MEMBER_NOT_FOUND, CONCURRENT_MODIFICATION}),
                Map.entry("reportPost", new ErrorCode[] {
                        AUTHENTICATION_REQUIRED, INVALID_REQUEST_FORMAT, INVALID_INPUT, INVALID_POST_ID,
                        MEMBER_NOT_FOUND, POST_NOT_FOUND, POST_DELETED, POST_ACCESS_DENIED,
                        SELF_REPORT_DENIED, DUPLICATE_REPORT, CONCURRENT_MODIFICATION}),
                Map.entry("reportComment", new ErrorCode[] {
                        AUTHENTICATION_REQUIRED, INVALID_REQUEST_FORMAT, INVALID_INPUT, MEMBER_NOT_FOUND,
                        COMMENT_NOT_FOUND, POST_DELETED, POST_ACCESS_DENIED, COMMENT_ACCESS_DENIED,
                        SELF_REPORT_DENIED, DUPLICATE_REPORT, CONCURRENT_MODIFICATION}),
                Map.entry("approve", new ErrorCode[] {
                        AUTHENTICATION_REQUIRED, ACCESS_DENIED, INVALID_INPUT, MEMBER_NOT_FOUND, REPORT_NOT_FOUND,
                        REPORT_STATE_CONFLICT, CONCURRENT_MODIFICATION})
        );

        return openApi -> openApi.getPaths().values().forEach(path ->
                path.readOperations().forEach(operation -> {
                    if ("findPost".equals(operation.getOperationId())) {
                        operation.addSecurityItem(new SecurityRequirement());
                        operation.addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
                    }
                    ErrorCode[] errors = errorsByOperation.get(operation.getOperationId());
                    if (errors == null) {
                        return;
                    }
                    for (ErrorCode error : errors) {
                        addExample(operation.getResponses(), error, objectMapper);
                    }
                    addExample(operation.getResponses(), INTERNAL_SERVER_ERROR, objectMapper);
                }));
    }

    private void addExample(io.swagger.v3.oas.models.responses.ApiResponses responses,
                            ErrorCode error, ObjectMapper objectMapper) {
        var response = responses.get(String.valueOf(error.getStatusValue()));
        if (response == null || response.getContent() == null) {
            return;
        }
        var json = response.getContent().get("application/json");
        if (json != null) {
            var value = objectMapper.convertValue(ErrorResponse.from(error), Map.class);
            json.addExamples(error.getCode(), new Example().summary(error.getMessage()).value(value));
        }
    }
}
