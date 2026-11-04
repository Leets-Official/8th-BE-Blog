# 요청 본문 오류 분류 검증과 보강

Status: ready-for-agent

## Scope

[명세](../spec.md)에 따라 기존 MockMvc 경계에서 본문 부재와 JSON 변환 실패를 재현한다. 현재 원인 예외 유무에 의존한 분류의 정확성을 확인하고 필요한 경우 보강한다.

## Completion criteria

- 필수 본문 부재는 HTTP 400 `MISSING_REQUEST_BODY`다.
- JSON 구문 오류·필드 타입 오류와 DTO 제약 위반은 HTTP 400 `INVALID_INPUT_VALUE`다.
- 실제 요청의 HTTP 상태와 오류 JSON을 검증하며 제약 위반의 안내 메시지를 유지한다.
- 변경 필요성 및 선택한 분류 기준을 기록하고 전체 테스트가 통과한다.
- 선언 누락 검사와 실패 응답 인터페이스 정리는 별도 변경으로 유지한다.
