# 헥사고날 모듈러 모놀리스 리팩토링

Status: resolved

## Problem Statement

현재 애플리케이션은 컨트롤러·서비스·저장소 같은 기술 계층으로 코드가 나뉘어 있다. 서비스와 도메인 객체가 HTTP DTO, Spring Data 저장소, JPA 엔티티를 직접 참조하고 있어 업무 규칙을 웹·데이터베이스 구현과 분리하기 어렵다.

이 구조는 게시글·댓글·신고·검색 기능의 데이터 소유권도 흐리게 만든다. 검색은 게시글 테이블을 직접 읽고, 여러 도메인이 JPA `User` 엔티티를 공유하며, 신고 처리의 원자성은 단일 DB 트랜잭션에 기대고 있다. 현재는 독립 배포 서비스로 나누지 않지만, 추후 서비스 경계를 정할 수 있는 모듈 구조가 필요하다.

## Solution

기존 Spring Boot 애플리케이션과 Gradle 프로젝트 하나를 유지하면서 `user`, `blog`, `report`, `search` 기능 모듈을 만든다. 각 모듈은 도메인·유스케이스·포트·어댑터 경계를 갖고, 업무 규칙은 Spring MVC·Spring Data JPA·HTTP 응답 형식에 의존하지 않는다.

기존 API 경로, 응답 형식과 코드, 기존 DB 테이블 및 데이터를 보존한다. 검색 모듈은 게시글 테이블을 직접 조회하지 않도록 별도의 검색 프로젝션 테이블을 추가로 소유한다. 이 테이블은 기존 테이블과 데이터를 변경하거나 대체하지 않는다. 모듈은 자기 데이터만 소유하고 공개 포트와 식별자·불변 레코드로만 상호작용한다. 실제 독립 서비스로 추출할 때 전용 DB, 네트워크 통신, 이벤트 전달과 데이터 이관을 별도 설계한다.

## User Stories

1. As an API client, I want existing routes and response envelopes to remain unchanged, so that the refactor does not require client changes.
2. As a post author, I want to create a post through the existing endpoint, so that the refactored application preserves the current posting behavior.
3. As a reader, I want to retrieve post details through the existing endpoint, so that the refactored application preserves current read behavior.
4. As a post author, I want update and delete permissions to remain unchanged, so that refactoring does not weaken ownership rules.
5. As a reader, I want comment listing, replies, search, and visibility rules to remain unchanged, so that existing discussions retain their behavior.
6. As a reporter, I want duplicate-report prevention and report snapshots to remain unchanged, so that repeated requests cannot create duplicate reports and historical evidence remains available.
7. As a report processor, I want report resolution and content hiding to succeed or roll back together, so that report and content states cannot disagree in the current deployment.
8. As a search user, I want keyword search, suggestions, trends, and statistics to keep their current behavior, so that the search refactor is transparent to clients.
9. As a developer, I want post and comment rules to run without Spring MVC or JPA types, so that web and persistence implementations can change independently.
10. As a developer, I want report state transitions to be expressed without HTTP status or API response types, so that report policy remains independent of transport concerns.
11. As a developer, I want domain models separated from persistence entities, so that JPA mapping details do not become the business model.
12. As a module owner, I want each feature to own its repositories and persistence mappings, so that another feature cannot bypass its public operations by querying its tables directly.
13. As a module owner, I want cross-feature calls to use public ports and immutable records, so that internal domain and persistence objects do not leak across boundaries.
14. As a developer, I want the user module to own the current name-and-email profile records without implying authentication, so that author and processor lookups have a clear owner.
15. As a search module owner, I want to maintain a search-owned post projection from post-change events, so that keyword search does not read blog-owned tables directly.
16. As a developer, I want search projection updates to participate synchronously in the current database transaction, so that the monolith retains immediate consistency during this refactor.
17. As a developer, I want HTTP error mapping to remain in the web adapter, so that domain failures do not depend on API error codes or HTTP status types.
18. As a developer, I want architecture rules checked automatically, so that future changes cannot introduce dependencies from domain code to adapters or between module internals.
19. As a developer, I want the refactor delivered in buildable feature slices, so that regressions can be found and isolated as each module moves.
20. As a future service owner, I want module data ownership and public contracts to be explicit, so that a module can later be extracted with its own database and transport adapters.

## Implementation Decisions

- Keep one Spring Boot application and one Gradle project. This is a modular monolith, not a set of independently deployed services.
- Create four feature modules: `user` owns name-and-email profile data only; `blog` owns posts and comments; `report` owns report records and their `PENDING` to `RESOLVED` lifecycle; `search` owns search history, vocabulary, statistics, and its post-search projection.
- Organize each feature around `domain`, `application` (inbound and outbound ports plus use cases), and `adapter` (web and persistence). Spring configuration and shared web response handling belong to the application assembly/web boundary, not domain code.
- Keep domain and application rules free of Spring MVC, Spring Data, JPA, `ResponseEntity`, `ApiResponse`, and transport DTO dependencies.
- Use domain models separate from JPA persistence models. Spring Data repositories and persistence mappings remain private to their owning feature's persistence adapter.
- Cross-module contracts expose module-owned identifiers such as `UserId` and immutable request/query/result records. They do not expose domain entities, JPA entities, or repositories.
- Modules access other modules only through public application ports. `user` exposes profile lookup; `blog` exposes the post/comment operations needed by other modules; `report` requests target information and hiding through `blog` ports.
- `blog` owns content visibility state. `report` owns report state and resolution decisions. A HIDE resolution calls the blog port; report resolution and content hiding stay in one database transaction in the current monolith.
- `search` owns keyword search behavior and its post projection. `blog` emits post-change events; synchronous in-process handling updates the search projection in the same database transaction for this phase.
- The search-owned post projection uses a new additive table. Existing tables, columns, constraints, and stored rows are preserved; the new table is populated from existing posts during startup synchronization.
- Keep one physical database and preserve existing tables/data during this refactor. Additive tables owned by a feature are allowed when required by its ownership boundary; the search projection is the only such addition in this refactor. Persistence adapters map foreign module references as identifiers rather than JPA object relationships, while existing database foreign-key constraints remain managed by the schema.
- At actual independent service extraction, move each service's owned data into a dedicated database. Table splitting, schema migration, outbox delivery, eventual search consistency, network transport, and distributed compensation are later decisions.
- Preserve existing HTTP routes, request/response shapes, `ApiResponse`, and existing error-code values. Web adapters translate semantic domain/application failures to the established HTTP status and API error response.
- Represent use-case failures with semantic, framework-independent exceptions. Keep API error code mapping in the web adapter.
- Apply Spring transaction boundaries with an adapter/decorator around use-case ports. Preserve existing atomic behavior, especially report resolution plus content hiding.
- Check module dependency rules with ArchUnit: domain code cannot depend on application adapters or frameworks, and a module cannot import another module's persistence internals.
- Migration order: establish the user profile module and lookup port; move post creation and detail retrieval as the first vertical slice; move remaining post and comment behavior; move report behavior; move search last.

## Testing Decisions

- A good test observes a module's public behavior or externally visible contract. Tests should not assert private class layout or implementation-specific call sequences.
- Test domain state rules and use cases as plain Java behavior with fake outbound ports, including invalid post ownership and invalid report transitions.
- Test each persistence adapter against the database for existing schema mapping, identifiers, foreign-key constraints, paging, transaction rollback, and report concurrency behavior.
- Keep MockMvc/API contract tests for existing routes, status codes, response envelopes, error codes, and serialized fields.
- Add ArchUnit tests for dependency direction and module isolation.
- Preserve and run the existing regression coverage for search aggregation/deduplication, report idempotency and state changes, comment visibility, and post CRUD.
- Existing prior art includes controller-level MockMvc tests, JPA integration tests, search persistence/concurrency tests, and report state/concurrency tests.

## Out of Scope

- Independently deploying services during this refactor.
- Adding authentication, authorization roles, or a user account lifecycle. The `user` module owns the existing profile records only.
- Changing public API routes, payloads, response envelopes, existing error-code values, or existing DB tables/data in this phase. Additive module-owned tables required by the approved design are allowed.
- Choosing HTTP versus gRPC, a message broker, retry/circuit-breaker policy, or a production outbox implementation before services are actually extracted.
- Implementing distributed transactions or a saga now. Current report/content state changes remain atomic in the single-database monolith.
- Replacing the database engine or adding a new business feature.

## Further Notes

- The initial post slice requires author existence lookup through the user module's public port.
- Existing post list routes remain stable. Keyword-based post search is owned by `search`; post creation, detail, update, and deletion are owned by `blog`.
- The search projection is synchronous only while the application shares one transaction manager. When extracted, post-change delivery will require an outbox or equivalent reliable event mechanism, and search may become eventually consistent.
- This spec records the approved architecture and migration sequence. Implement the refactor incrementally, keeping the application buildable at each slice.
