# 02: 요청 본문 오류 분류 검증과 보강

Status: resolved

**Blocked by:** None (can start immediately).

**What to build:** 본문 누락·JSON 구문 오류·필드 타입 오류·DTO 제약 위반을 클라이언트에게 적절한 HTTP 상태와 응답 코드로 전달한다.

## Scope

[명세](../spec.md)에 따라 기존 MockMvc 경계에서 본문 부재와 JSON 변환 실패를 재현한다. 현재 원인 예외 유무에 의존한 분류의 정확성을 확인하고 필요한 경우 보강한다.

## Completion criteria

- [x] 필수 본문 부재는 HTTP 400 `MISSING_REQUEST_BODY`다.
- [x] JSON 구문 오류·필드 타입 오류와 DTO 제약 위반은 HTTP 400 `INVALID_INPUT_VALUE`다.
- [x] 실제 요청의 HTTP 상태와 오류 JSON을 검증하며 제약 위반의 안내 메시지를 유지한다.
- [x] 변경 필요성 및 선택한 분류 기준을 기록하고 전체 테스트가 통과한다.
- [x] 선언 누락 검사와 실패 응답 인터페이스 정리는 별도 변경으로 유지한다.

## Comments

- MockMvc로 본문 부재, JSON 구문 오류, 숫자 필드에 문자열 전달, 공백 제목을 각각 검증했다. 모두 현재 예외 처리기로 명세대로 동작했다.
- 현재 버전에서 필수 본문 부재는 원인 예외 없는 읽기 오류, JSON 파싱·필드 변환 실패는 원인 예외가 있는 읽기 오류로 전달된다. 잘못된 분류가 재현되지 않아 런타임 로직을 추가하지 않았다. Spring·Jackson 갱신 시에도 공개 동작을 확인하도록 회귀 테스트를 남겼다.
- 네 경우 모두 `success: false`, 상황별 메시지와 명시적인 `data: null`을 검증한다. 현재 검증한 요청 범위를 넘어 모든 변환기에서 동일한 원인 예외 구조를 보장한다고 주장하지 않는다.
