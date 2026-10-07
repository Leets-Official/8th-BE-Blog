package com.example.demo.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ErrorResponse {

    private final String message;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private final List<ValidationError> errors;

    public ErrorResponse(String message) {
        this(message, List.of());
    }

    @Getter
    @AllArgsConstructor
    public static class ValidationError {

        private final String field;
        private final String message;
    }
}
