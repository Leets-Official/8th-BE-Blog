package com.example.demo.global.exception;

import com.example.demo.global.response.ApiResponse;
import org.springframework.dao.DataIntegrityViolationException;
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

    // 3) JSON 형식이 깨졌거나, 필요한 헤더가 없거나, 숫자 자리에 글자가 온 경우
    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MissingRequestHeaderException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(Exception e) {
        return toResponse(ErrorCode.BAD_REQUEST);
    }

    // 4) 중복 검사를 통과했는데 그 사이 다른 요청이 같은 값을 먼저 저장한 경우 (DB unique 제약이 막아 줌)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrity(DataIntegrityViolationException e) {
        return toResponse(ErrorCode.DUPLICATE_RESOURCE);
    }

    // DTO의 검증 어노테이션 message에 적어 둔 ErrorCode 이름으로 에러 코드를 찾는다.
    // 예: @NotBlank(message = "POST_TITLE_EMPTY") → ErrorCode.POST_TITLE_EMPTY
    // 새 기능이 생겨도 여기는 고칠 필요 없이, DTO에 message만 적으면 된다.
    private ErrorCode toErrorCode(FieldError fieldError) {
        if (fieldError == null || fieldError.getDefaultMessage() == null) {
            return ErrorCode.BAD_REQUEST;
        }
        try {
            return ErrorCode.valueOf(fieldError.getDefaultMessage());
        } catch (IllegalArgumentException e) {   // message에 ErrorCode 이름이 아닌 값이 적힌 경우
            return ErrorCode.BAD_REQUEST;
        }
    }

    private ResponseEntity<ApiResponse<Void>> toResponse(ErrorCode errorCode) {
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.fail(errorCode.getCode(), errorCode.getMessage()));
    }
}
