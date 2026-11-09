# Domain Glossary: Leets Backend Blog (Week 1)

## Architecture & Response Models

### `ApiResponse<T>` (Response Envelope)
모든 REST API 응답을 일관되게 래핑하는 제네릭 공통 응답 DTO.
- **필드 구성**:
  - `success` (boolean): 요청 성공 여부 (`true` / `false`)
  - `code` (`ApiResponseCode` enum): 성공 또는 구체적인 오류 종류. JSON에는 enum 이름이 문자열로 직렬화된다 (예: `SUCCESS`, `POST_NOT_FOUND`).
  - `message` (String): 클라이언트 안내 또는 디버깅 메시지
  - `data` (T): 실제 비즈니스 응답 데이터 (실패 시 `null`)
- **표준 응답 예시 (성공)**:
***
{
  "success": true,
  "code": "SUCCESS",
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
  "code": "INVALID_INPUT_VALUE",
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
  - `GET /api/v2/health` ➔ `ApiResponse<String>`

API V2에서는 실제 HTTP 상태와 응답 본문의 `code`를 구분한다. `ResponseEntity`가 HTTP 상태를 결정하며, `ApiResponseCode` enum은 의미 코드를 제공하고 해당 상태에 매핑된다. 자세한 결정은 [ADR-0002](docs/adr/0002-api-v2-response-code-contract.md)를 따른다.
