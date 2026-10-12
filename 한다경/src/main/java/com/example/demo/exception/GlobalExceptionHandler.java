package com.example.demo.exception;

import com.example.demo.dto.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ErrorResponse handleValidationException(
            MethodArgumentNotValidException e
    ) {
        List<ErrorResponse.ValidationError> errors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::toValidationError)
                .toList();

        return new ErrorResponse("요청값이 올바르지 않습니다.", errors);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ErrorResponse handleInvalidRequestBody() {
        return new ErrorResponse("요청 본문이 올바르지 않습니다.");
    }
    private ErrorResponse.ValidationError toValidationError(FieldError fieldError) {
        return new ErrorResponse.ValidationError(
                fieldError.getField(),
                fieldError.getDefaultMessage()
        );
    }
}
