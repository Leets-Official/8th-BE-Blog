# Domain Glossary: Leets Backend Blog (Week 1)

## Architecture & Response Models

### `ApiResponse<T>` (Response Envelope)
모든 REST API 응답을 일관되게 래핑하는 제네릭 공통 응답 DTO.
- **필드 구성**:
  - `success` (boolean): 요청 성공 여부 (`true` / `false`)
  - `code` (String): 비즈니스 응답 코드 (예: `S200`, `S201`, `COMMON400_VALIDATION`)
  - `message` (String): 클라이언트 안내 또는 디버깅 메시지
  - `data` (T): 실제 비즈니스 응답 데이터 (실패 시 `null`)
- **표준 응답 예시 (성공)**:
***
{
  "success": true,
  "code": "S200",
  "message": "문자열 복제에 성공하였습니다.",
  "data": {
    "string_one": "hello",
    "string_two": "hello"
  }
}
***
- **표준 응답 예시 (실패)**:
***
{
  "success": false,
  "code": "COMMON400_VALIDATION",
  "message": "문자열 값은 비어있거나 공백일 수 없습니다.",
  "data": null
}
***

## Infrastructure & Handlers

### `GlobalExceptionHandler`
`@RestControllerAdvice` 기반의 전역 예외 처리기.
- **역할**: 유효성 검증 실패(`MethodArgumentNotValidException`), JSON 파싱 에러(`HttpMessageNotReadableException`), 기타 서버 예외를 가로채어 일관된 `ApiResponse.fail(...)`을 반환.

### `AssignmentController`
프레젠테이션 계층 컨트롤러.
- 모든 응답을 `ResponseEntity<ApiResponse<T>>` 형태로 반환. HTTP 상태와 JSON 본문을 구분한다.
- **엔드포인트**:
  - `GET /api/v1/health` ➔ `ApiResponse<String>`
