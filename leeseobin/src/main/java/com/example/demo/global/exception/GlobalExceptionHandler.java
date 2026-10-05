package com.example.demo.global.exception;

import com.example.demo.global.exception.dto.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    // Spring MVC 표준 예외를 ErrorResponse 형식으로 변환
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
        Exception exception, Object body, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request
    ) {
        ResponseEntity<Object> response = super.handleExceptionInternal(exception, body, headers, statusCode, request);
        if (response == null) {
            return null;
        }

        HttpStatus httpStatus = HttpStatus.valueOf(statusCode.value());
        String detailMessage = exception instanceof MethodArgumentNotValidException validException
            ? validException.getBindingResult().getAllErrors().get(0).getDefaultMessage()
            : (response.getBody() instanceof ProblemDetail problemDetail ? problemDetail.getDetail() : null);
        ErrorResponse errorResponse = new ErrorResponse(
            httpStatus.value(),
            httpStatus.getReasonPhrase(),
            httpStatus.name(),
            detailMessage
        );
        return ResponseEntity.status(statusCode).headers(response.getHeaders()).body(errorResponse);
    }

    // Service Exception 처리
    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<ErrorResponse> handleServiceException(ServiceException serviceException) {
        ErrorCode errorCode = serviceException.getErrorCode();
        ErrorResponse errorResponse = new ErrorResponse(errorCode, serviceException.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.valueOf(errorCode.getHttpStatus()));
    }

    // 처리되지 않은 예외: 원인은 로그로만 남기고 응답에는 내부 정보를 노출하지 않음
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception exception) {
        log.error("Unhandled exception", exception);
        ErrorResponse errorResponse = new ErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR, null);
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
