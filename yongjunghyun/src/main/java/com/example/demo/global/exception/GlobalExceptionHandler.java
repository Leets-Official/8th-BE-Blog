package com.example.demo.global.exception;

import com.example.demo.global.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1) Service에서 직접 던진 예외 (게시글 없음, 작성자 아님 등)
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Void>> handleCustom(CustomException e) {
        return toResponse(e.getErrorCode());
    }

    // 2) @Valid 검증 실패 (제목 비어 있음 등)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        return toResponse(toErrorCode(fieldError));
    }

    // 3) JSON 형식이 깨졌거나, X-Member-Id 헤더가 없거나, 숫자 자리에 글자가 온 경우
    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MissingRequestHeaderException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(Exception e) {
        return toResponse(ErrorCode.BAD_REQUEST);
    }

    // 어떤 칸이 어떤 규칙을 어겼는지 보고 에러 코드를 고른다
    private ErrorCode toErrorCode(FieldError fieldError) {
        if (fieldError == null) {
            return ErrorCode.BAD_REQUEST;
        }
        String field = fieldError.getField();      // 예: "title"
        String rule = fieldError.getCode();        // 예: "NotBlank", "Size"

        if (field.equals("title") && "Size".equals(rule)) {
            return ErrorCode.POST_TITLE_TOO_LONG;
        }
        if (field.equals("title")) {
            return ErrorCode.POST_TITLE_EMPTY;
        }
        if (field.equals("content")) {
            return ErrorCode.POST_CONTENT_EMPTY;
        }
        return ErrorCode.BAD_REQUEST;
    }

    private ResponseEntity<ApiResponse<Void>> toResponse(ErrorCode errorCode) {
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.fail(errorCode.getCode(), errorCode.getMessage()));
    }
}