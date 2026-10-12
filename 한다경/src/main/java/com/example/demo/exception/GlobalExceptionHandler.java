package com.example.demo.exception;

import com.example.demo.dto.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import java.util.Objects;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException e
    ) {
        List<ErrorResponse.ValidationError> errors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::toValidationError)
                .toList();

        ErrorCode errorCode = findValidationErrorCode(e.getBindingResult().getFieldErrors());
        ErrorResponse response = new ErrorResponse(
                errorCode.getStatusValue(),
                errorCode.getCode(),
                errorCode.getMessage(),
                errors
        );

        return ResponseEntity.status(errorCode.getStatus()).body(response);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ErrorResponse handleInvalidRequestBody() {
        return toErrorResponse(ErrorCode.INVALID_REQUEST_FORMAT);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        ErrorCode errorCode = e.getErrorCode();

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(toErrorResponse(errorCode));
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ErrorResponse handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        if ("postId".equals(e.getName())) {
            return toErrorResponse(ErrorCode.INVALID_POST_ID);
        }

        return toErrorResponse(ErrorCode.INVALID_INPUT);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MissingRequestHeaderException.class)
    public ErrorResponse handleMissingHeader(MissingRequestHeaderException e) {
        if ("X-User-Id".equals(e.getHeaderName())) {
            return toErrorResponse(ErrorCode.MISSING_USER_ID);
        }

        return toErrorResponse(ErrorCode.INVALID_INPUT);
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ErrorResponse handleUnexpectedException(Exception e) {
        return toErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR);
    }

    private ErrorResponse toErrorResponse(ErrorCode errorCode) {
        return new ErrorResponse(
                errorCode.getStatusValue(),
                errorCode.getCode(),
                errorCode.getMessage()
        );
    }

    private ErrorResponse.ValidationError toValidationError(FieldError fieldError) {
        ErrorCode errorCode = findErrorCodeByName(fieldError.getDefaultMessage());
        String message = errorCode == null ? fieldError.getDefaultMessage() : errorCode.getMessage();

        return new ErrorResponse.ValidationError(
                fieldError.getField(),
                message
        );
    }

    private ErrorCode findValidationErrorCode(List<FieldError> fieldErrors) {
        return fieldErrors.stream()
                .map(FieldError::getDefaultMessage)
                .map(this::findErrorCodeByName)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(ErrorCode.INVALID_INPUT);
    }

    private ErrorCode findErrorCodeByName(String name) {
        try {
            return ErrorCode.valueOf(name);
        } catch (IllegalArgumentException | NullPointerException e) {
            return null;
        }
    }
}
