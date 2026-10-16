# 01: [Prefactor] 표준 ApiResponse 문자열 코드 확장 및 도메인 ErrorCode/글로벌 예외 처리 기반 구축

**What to build:**
ApiResponse가 기존의 숫자 HTTP 상태 코드뿐만 아니라 기획서 규격의 문자열 코드("S200", "S201", "POST4001" 등)를 지원하도록 확장하고, 비즈니스 에러 코드 열거형(ErrorCode)과 도메인 커스텀 예외(CustomException), 그리고 GlobalExceptionHandler를 정비하여 전역적으로 통일된 표준 응답 포맷을 제공한다.

**Blocked by:** None (can start immediately)

**Status:** ready-for-agent

- [x] ApiResponse에 String code 지원 생성자 및 정적 팩토리 메서드(success, fail) 추가
- [x] ErrorCode 열거형(HttpStatus, code, message) 정의: POST4001, POST4003, COMMON400_VALIDATION, COMMON400_TYPE, COMMON400_BODY, COMMON405, COMMON500
- [x] BusinessException / CustomException 구현
- [x] GlobalExceptionHandler에서 CustomException, MethodArgumentNotValidException 등을 ErrorCode 규격에 맞게 ApiResponse.fail()로 반환하도록 매핑
- [x] 단위/통합 테스트 작성 및 통과 검증
