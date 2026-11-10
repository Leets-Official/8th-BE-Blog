# API별 Swagger 오류 예시 개선

Status: complete

## Problem

PR #119에서 HTTP 상태별 공통 응답을 참조하면서 게시글 조회의 404에도 신고·검색 오류 예시가 노출된다는 리뷰를 받았다.

## Confirmed decisions

- 게시글·댓글·신고·검색·헬스체크의 전체 API를 대상으로 한다.
- 각 API에서 실제 발생 가능한 오류만 문서화한다. 공통 입력 오류도 요청 형태에 맞춰 포함한다. 본문이 없는 GET에는 `MISSING_REQUEST_BODY`를 표시하지 않는다.
- 각 컨트롤러 메서드의 `@ApiErrorCodes`에 해당 API의 입력·업무 오류를 명시한다.
- `OperationCustomizer`가 선언된 코드를 `ApiResponseCode`의 HTTP 상태로 묶고, 기본 메시지로 이름별 예시를 생성한다. 오류 응답 스키마는 공유한다.
- 모든 API에 `INTERNAL_SERVER_ERROR` 예시를 추가한다.
- operation 선택 이전의 `ENDPOINT_NOT_FOUND`·`METHOD_NOT_ALLOWED`는 OpenAPI 전체 설명에서 안내한다.
- 예시 메시지는 대표 문구이며 실제 검증·업무 메시지는 상황에 따라 달라질 수 있음을 설명한다.
- 런타임 응답 코드·상태·오류 처리 동작은 유지한다.

## Completion criteria

- `/v3/api-docs`에서 전체 14개 operation의 오류 코드 목록을 실제 예외 분기에 맞게 검증한다.
- 예시의 HTTP 상태, 기본 메시지, `success: false`, `data: null`, 공유 오류 스키마 참조를 검증한다.
- 게시글 조회 404에는 `POST_NOT_FOUND`만 포함하며, 본문 없는 GET에 `MISSING_REQUEST_BODY`가 없어야 한다.
- 기존 성공 응답과 전체 테스트가 통과한다.
- 별도 브랜치 `강현우/119-swagger-error-examples`에 이번 개선을 커밋한다.

## Existing documentation

- ADR-0002의 의미 중심 응답 코드 및 HTTP 상태 매핑은 유지한다.
- 기존 `.scratch/swagger-v2/spec.md`의 공통 응답 컴포넌트 결정을 API별 예시 생성으로 갱신한다. 응답 계약 자체를 바꾸지 않는 가역적인 문서 구현 변경이므로 새 ADR은 만들지 않는다.

## Validation

- `./gradlew test`: 105개 테스트 통과, 실패 0건.
- `git diff --check` 통과.
