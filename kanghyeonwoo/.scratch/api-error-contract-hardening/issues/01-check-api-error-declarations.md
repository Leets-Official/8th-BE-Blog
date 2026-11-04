# 01: Swagger 오류 선언 누락 검사

Status: resolved

**Blocked by:** None (can start immediately).

**What to build:** 새 API의 오류 선언이 누락되면 자동 검증이 실패하고 해당 경로와 메서드를 식별할 수 있게 한다.

## Scope

[명세](../spec.md)에 따라 실제 등록된 프로젝트 REST API 메서드의 `@ApiErrorCodes` 선언을 검사한다. 업무 오류가 없는 API도 빈 선언을 요구하고 프레임워크·Swagger 내부 엔드포인트는 제외한다.

## Completion criteria

- [x] 신규 API에도 고정 목록 갱신 없이 선언 누락 검사가 적용된다.
- [x] 누락된 API 메서드를 실패 메시지에서 식별할 수 있다.
- [x] 기존 전체 API의 OpenAPI 오류 예시 검증과 전체 테스트가 통과한다.
- [x] 본문 오류 분류와 응답 생성 인터페이스는 이 작업에서 변경하지 않는다.

## Comments

- Spring의 실제 등록 메서드에서 프로젝트 REST 컨트롤러만 검사한다. 합성 REST 컨트롤러 어노테이션도 인식하며, 빈 오류 선언은 허용한다.
- 헬스체크의 선언을 일시 제거한 검사에서 `/api/v2/health`와 `HealthController#healthCheck`가 포함된 실패를 확인했다. 선언 복원 후 Swagger 통합 테스트가 통과했다.
