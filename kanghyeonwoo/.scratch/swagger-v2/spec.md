# Swagger 보완과 API V2 계약

Status: ready-for-agent

## Problem

프로젝트에는 `springdoc-openapi`와 `OpenApiConfig`, 컨트롤러의 일부 `@Tag`·`@Operation`, 일부 DTO의 `@Schema`가 이미 있다. 전체 API 명세와 DTO 스키마, 오류 응답 설명은 불완전하다. 현재 URL은 `/api/v1`이고 `ApiResponse.code`는 임의 문자열이다.

기존 초안에는 구현되지 않은 게시글 목록·숨김 및 댓글 CRUD, 관리자 전용 신고 처리, 별도 `ErrorResponse`가 포함되어 있다. 실제 API는 게시글 목록을 검색 API로 제공하고, 댓글은 목록·검색 API만 제공한다. 신고 처리는 인증·관리자 역할 검증 없이 존재하는 사용자 ID를 받아 처리한다. 오류 응답은 별도 ErrorResponse가 아니라 `ApiResponse<Void>`다.

## Goal

현재 구현된 모든 API를 `/api/v2`로 전환하고, 실제 요청·응답·상태 동작을 OpenAPI 문서에서 빠짐없이 확인할 수 있게 한다. 응답 코드 규격을 enum 기반 의미 코드로 바꾸고 전 도메인 DTO, 성공 응답, 오류 응답을 문서화한다.

## Decisions

- 문서화와 응답 계약·경로 버전 전환만 한다. 새 게시글·댓글 기능이나 인증/관리자 권한을 추가하지 않는다.
- 게시글, 댓글, 신고, 검색 및 헬스체크의 현재 엔드포인트를 모두 문서화한다.
- 모든 기존 `/api/v1` 엔드포인트를 `/api/v2`로 옮긴다. `/api/v1` 호환 경로는 제공하지 않는다.
- 공통 응답의 `success`, `message`, `data` 필드는 유지한다. 실패 응답의 `data`는 `null`이다.
- `code`는 공통 `ApiResponseCode` enum으로 제한하고 JSON에서는 enum 이름(예: `POST_NOT_FOUND`)을 문자열로 직렬화한다.
- `ApiResponseCode`는 해당 코드가 반환할 HTTP 상태를 매핑한다. HTTP 상태 숫자는 응답 상태 라인에만 둔다. 코드에 상태 숫자·일련번호를 넣지 않는다.
- 성공 코드는 `SUCCESS`(HTTP 200), `RESOURCE_CREATED`(HTTP 201)다.
- 공통 오류 코드는 `INVALID_INPUT_VALUE`, `TYPE_MISMATCH`, `MISSING_REQUEST_BODY`, `ENDPOINT_NOT_FOUND`, `METHOD_NOT_ALLOWED`, `INTERNAL_SERVER_ERROR`다.
- 리소스 및 업무 오류는 의미에 따라 `USER_NOT_FOUND`, `POST_NOT_FOUND`, `COMMENT_NOT_FOUND`, `REPORT_NOT_FOUND`, `SEARCH_KEYWORD_NOT_FOUND`, `POST_ACCESS_DENIED`, `SELF_REPORT_FORBIDDEN`, `DUPLICATE_RESOURCE`, `REPORT_ALREADY_RESOLVED`, `RESOURCE_DELETED`로 구분한다.
- 오류 응답은 기존 `ApiResponse<Void>` 형식으로 명세한다. 클라이언트는 안정적인 `code`로 분기하고 `message`는 표시 문구로 사용한다.
- Swagger 오류 응답 예시는 HTTP 상태별 공통 컴포넌트로 묶고, 각 컴포넌트에 해당 상태로 반환 가능한 코드·메시지 예시를 이름별로 제공한다. 예시 메시지는 `ApiResponseCode`의 기본 메시지를 사용해 런타임 정의와 따로 복제하지 않는다.
- `@ApiResponse` Swagger 어노테이션과 `ApiResponse<T>` 응답 DTO는 이름은 같지만 다른 타입이다. 코드에서 단순 이름이 충돌하면 Swagger 어노테이션의 완전한 이름을 사용한다.
- 엔드포인트별 업무 오류는 각 작업에 관련된 것만 선언하고, 405·500 등 공통 오류 응답은 재사용 가능한 OpenAPI 응답 컴포넌트로 제공한다.
- OpenAPI `info.version`은 `2.0.0`으로 표기한다. Swagger UI 경로 `/swagger-ui.html`, JSON 경로 `/v3/api-docs`는 유지한다.
- 사용자는 PR 본문과 캡처 이미지를 직접 준비한다. 구현자는 로컬 Try it out 검증은 하되 PR 본문과 이미지 파일은 만들지 않는다.

## Current endpoint inventory

- `POST /api/v2/posts`
- `GET /api/v2/posts/{postId}`
- `PATCH /api/v2/posts/{postId}`
- `DELETE /api/v2/posts/{postId}`
- `GET /api/v2/posts` (검색·페이징)
- `GET /api/v2/posts/{postId}/comments` (목록·본문 검색)
- `POST /api/v2/posts/{postId}/reports`
- `POST /api/v2/comments/{commentId}/reports`
- `GET /api/v2/reports` (필터·스냅샷 검색)
- `PATCH /api/v2/reports/{reportId}/resolve`
- `GET /api/v2/search/keywords/{keywordId}/stats`
- `GET /api/v2/search/trends`
- `GET /api/v2/search/suggestions`
- `GET /api/v2/health`

## Response-code mapping

| HTTP status | Enum code | Meaning |
| --- | --- | --- |
| 200 | `SUCCESS` | 조회·수정·삭제·신고 처리 성공 |
| 201 | `RESOURCE_CREATED` | 게시글·신고 생성 성공 |
| 400 | `INVALID_INPUT_VALUE`, `TYPE_MISMATCH`, `MISSING_REQUEST_BODY` | 검증·바인딩 오류 |
| 403 | `POST_ACCESS_DENIED`, `SELF_REPORT_FORBIDDEN` | 작성자 전용 동작·본인 신고 제한 |
| 404 | `USER_NOT_FOUND`, `POST_NOT_FOUND`, `COMMENT_NOT_FOUND`, `REPORT_NOT_FOUND`, `SEARCH_KEYWORD_NOT_FOUND` | 대상 미존재 |
| 404 | `ENDPOINT_NOT_FOUND` | 등록되지 않은 경로 |
| 409 | `DUPLICATE_RESOURCE`, `REPORT_ALREADY_RESOLVED`, `RESOURCE_DELETED` | 중복·처리 완료·삭제 상태 충돌 |
| 405 | `METHOD_NOT_ALLOWED` | 허용되지 않은 HTTP 메서드 |
| 500 | `INTERNAL_SERVER_ERROR` | 처리되지 않은 서버 오류 |

오류별 메시지는 요청 데이터 검증이나 실제 대상 종류에 따라 구체화할 수 있지만, 같은 의미 코드의 HTTP 상태는 고정한다. 구현되지 않은 오류 상태(예: `RESOURCE_LOCKED`)를 선등록하지 않는다.

## Out of scope

- 신규 CRUD·숨김·채택 API 구현
- 로그인, 인증, 관리자 역할과 신고 처리 권한 변경
- V1 호환 경로 유지
- PR 본문 및 스크린샷 작성

## Ticket sequence

01 응답 코드 enum과 V2 공통 응답 계약
02 전 엔드포인트 V2 경로 및 OpenAPI 버전 전환
03 Swagger 전체 응답·DTO 명세와 계약 검증

## Completion criteria

- 모든 현행 엔드포인트가 `/api/v2`에서 동작하고 `/api/v1`은 노출되지 않는다.
- 응답 `code`는 enum 값으로만 생성되고 JSON은 합의한 enum 이름 문자열을 반환한다.
- 각 enum 코드와 HTTP 상태 매핑, 성공 생성 상태, 오류 봉투 JSON이 테스트된다.
- OpenAPI JSON에 14개 현행 엔드포인트, 성공·오류 HTTP 응답, 공통 코드 enum, 모든 DTO 필드·제약·예시가 포함된다.
- Swagger UI Try it out으로 로컬에서 대표 성공·검증 실패·중복 신고·처리 완료 시나리오를 확인한다.
- 문서가 관리자 전용 처리, 별도 ErrorResponse, 구현되지 않은 API를 주장하지 않는다.
