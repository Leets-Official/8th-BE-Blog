# Swagger 오류 선언 누락 검사

Status: ready-for-agent

## Scope

[명세](../spec.md)에 따라 실제 등록된 프로젝트 REST API 메서드의 `@ApiErrorCodes` 선언을 검사한다. 업무 오류가 없는 API도 빈 선언을 요구하고 프레임워크·Swagger 내부 엔드포인트는 제외한다.

## Completion criteria

- 신규 API에도 고정 목록 갱신 없이 선언 누락 검사가 적용된다.
- 누락된 API 메서드를 실패 메시지에서 식별할 수 있다.
- 기존 전체 API의 OpenAPI 오류 예시 검증과 전체 테스트가 통과한다.
- 본문 오류 분류와 응답 생성 인터페이스는 이 작업에서 변경하지 않는다.
