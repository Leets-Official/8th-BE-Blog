# Spec: Leets 백엔드 1주차 정기 과제 구현

Status: ready-for-agent

## Problem Statement

Spring Boot를 처음 접하는 백엔드 학습자로서, 서버의 기본 구조인 Controller-Service 계층 분리와 HTTP 요청/응답(GET, POST, JSON 파싱 및 직렬화)의 흐름을 이해하고 실제로 동작하는 최소 단위의 웹 API를 구성해야 합니다. 또한 실제 DB 연결 없이도 프로젝트가 원활하게 구동되도록 환경을 설정해야 합니다.

## Solution

Spring Boot 3 / Java 21 기반의 레이어드 아키텍처(Layered Architecture)를 적용하여 서버 상태를 확인할 수 있는 헬스체크 API(`GET /health`)와 요청된 문자열을 2개의 키로 복제하여 응답하는 API(`POST /string/repeat`)를 구현합니다. 비즈니스 로직은 Controller가 아닌 Service 계층에서 전담하며, Request/Response DTO를 분리하여 JSON 직렬화 규격을 준수합니다.

## User Stories

1. As an API client, I want to send a GET request to `/health`, so that I can verify the server is running and healthy by receiving a successful string response.
2. As an API client, I want to receive the string `"ok"` when calling the health check endpoint, so that my monitoring system can recognize a positive status.
3. As an API client, I want to send a POST request to `/string/repeat` with a JSON payload containing `value`, so that I can get the input string processed by the server.
4. As an API client, I want the repeat API response to be in JSON format with fields `string_one` and `string_two`, so that I receive the original string in both fields matching the spec.
5. As an API client, I want appropriate handling when sending empty or edge-case string values, so that the server processes requests without unexpected internal crashes.
6. As a developer, I want Controller to only handle HTTP request routing and response mapping, so that presentation logic is cleanly separated from business logic.
7. As a developer, I want Service to perform the actual string processing logic, so that business logic remains reusable and decoupled from the web transport layer.
8. As a developer, I want Request and Response DTOs to encapsulate the incoming and outgoing data, so that internal service logic is shielded from external API schema changes.
9. As a developer, I want Spring Data JPA auto-configuration to be safely bypassed when DB credentials are not yet configured, so that the application can start smoothly during early development stages.
10. As a QA / reviewer, I want comprehensive HTTP/WebMvc tests covering all endpoints, so that I can verify external behavior without manual testing.

## Implementation Decisions

- **Layered Architecture & Separation of Concerns**:
  - `AssignmentController` handles HTTP endpoint mapping (`/health`, `/string/repeat`), parameter binding, and delegates logic directly to `AssignmentService`.
  - `AssignmentService` processes the request data (providing status strings and duplicating input strings into response models).
  - Controller never directly constructs duplicate strings or implements business calculations inline.

- **DTO Model & Serialization**:
  - `RepeatStringRequest`: Encapsulates input payload with property `value`.
  - `RepeatStringResponse`: Encapsulates output payload with properties `stringOne` and `stringTwo`, mapped to JSON field names `string_one` and `string_two` via `@JsonProperty`.

- **API Contracts**:
  - `GET /health` ➔ Returns `text/plain` `"ok"`.
  - `POST /string/repeat` ➔ Accepts JSON `{"value": "<string>"}` and returns JSON `{"string_one": "<string>", "string_two": "<string>"}` with HTTP status 200 OK.

- **Infrastructure Configuration**:
  - Disable automatic DataSource / Hibernate JPA configuration at startup until database credentials and entities are introduced in subsequent assignments.

## Testing Decisions

- **Testing Seam**: The primary test seam is at the HTTP WebMvc layer using Spring's `MockMvc` test framework.
- **Behavior-Driven Verification**:
  - Verify that `GET /health` returns HTTP 200 status and response content `"ok"`.
  - Verify that `POST /string/repeat` with `{"value": "hello"}` returns HTTP 200 and JSON `{"string_one": "hello", "string_two": "hello"}`.
  - Verify edge cases such as empty string input or null field values.
- Tests will focus strictly on observable HTTP API behavior and contracts rather than private internal implementation details.

## Out of Scope

- Database persistence, table creation, and repository queries (scheduled for subsequent weeks).
- User authentication, authorization, and session management.
- Complex error response envelope structures beyond standard Spring Boot exception handling.

## Further Notes

- Target directory for implementation: `kanghyeonwoo/`
- Standard Leets convention: maintain PR template and issue tracking format for submission.
