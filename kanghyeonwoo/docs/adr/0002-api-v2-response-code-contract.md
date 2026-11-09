# API V2 경로와 의미 중심 응답 코드

- Status: Accepted
- Date: 2026-10-29

## Context

API는 `/api/v1` 경로와 공통 `ApiResponse` 봉투를 사용한다. 응답 본문의 `code`는 임의 문자열이며 성공은 `S200`·`S201`, 공통 입력 오류는 `COMMON400_*`, 도메인 오류는 `REPORT4091` 같은 값을 혼용한다. 실제 HTTP 상태는 `ResponseEntity`가 별도로 전달한다. Swagger 설정은 이미 있으나 API 응답·DTO 문서화는 일부에 그친다.

V2에서 `code`를 Java enum으로 제한하고 의미 중심 코드명으로 바꾸면 기존 클라이언트 계약이 바뀐다. URL 버전과 응답 코드를 함께 갱신해 이 변경을 명시적으로 구분해야 한다.

## Decision

- 모든 현재 API 경로를 `/api/v2`로 전환하고 `/api/v1` 호환 경로는 제공하지 않는다.
- 공통 응답은 `success`, `code`, `message`, `data`를 유지한다. 실패 응답의 `data`는 `null`이다.
- `code`는 공통 `ApiResponseCode` enum으로 제한한다. JSON에는 enum의 의미 코드명이 문자열로 직렬화된다.
- `ApiResponseCode`는 각 코드와 실제 HTTP 상태를 연결한다. HTTP 상태는 응답 상태 라인에만 전달하고, 코드명에 상태 숫자나 일련번호를 넣지 않는다.
- 성공 코드는 `SUCCESS`와 `RESOURCE_CREATED`를 사용한다. 입력 오류는 `INVALID_INPUT_VALUE`, `TYPE_MISMATCH`, `MISSING_REQUEST_BODY`; 상태·업무 오류는 `POST_NOT_FOUND`, `USER_NOT_FOUND`, `DUPLICATE_RESOURCE`, `REPORT_ALREADY_RESOLVED`, `RESOURCE_DELETED` 등 의미가 분명한 코드로 표현한다.
- 일반적인 같은 오류 의미만 공통 코드로 공유한다. 리소스 미존재처럼 클라이언트가 구분할 대상은 `POST_NOT_FOUND`, `REPORT_NOT_FOUND`처럼 구체적인 코드로 둔다.
- 클라이언트는 `code`를 기준으로 분기하고 `message`는 사용자에게 표시할 설명으로 사용한다. 코드의 의미는 공개 후 재사용하거나 변경하지 않는다.
- OpenAPI 문서의 경로 버전은 `/api/v2`, 문서 `info.version`은 `2.0.0`으로 표기한다.

## Consequences

- V1 URL과 기존 본문 코드에 의존하는 클라이언트는 V2 경로와 새 enum 값에 맞춰 수정해야 한다.
- 공통 응답 래퍼와 예외 처리기가 임의의 문자열 코드를 더 이상 받지 않아 잘못된 코드 삽입을 컴파일 시점에 제한한다.
- 실제 상태와 코드 enum의 상태 매핑을 테스트해 불일치를 방지해야 한다.
- Swagger 명세에는 각 엔드포인트의 실제 HTTP 상태와 의미 코드 enum 값을 함께 보여줘야 한다.

## Alternatives considered

- V1을 유지하면서 V2를 병행한다: 과제 프로젝트에는 클라이언트 호환 운영의 이점보다 두 계약과 문서를 함께 유지하는 비용이 크므로 선택하지 않았다.
- 본문 코드에 HTTP 상태와 일련번호를 넣는다: HTTP 상태는 응답 상태 라인에서 이미 알 수 있고 URL·메시지와 함께 읽을 수 있으므로 중복 숫자와 임의 시퀀스를 제거한다.
- 응답 코드를 임의 문자열로 둔다: 오타와 미등록 코드가 가능하므로 enum으로 제한한다.

