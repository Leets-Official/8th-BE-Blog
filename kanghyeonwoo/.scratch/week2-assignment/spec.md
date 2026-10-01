# Spec: Leets 백엔드 2주차 ERD 설계 기반 Spring Data JPA Entity 및 연관관계 구현

Status: ready-for-agent

## Problem Statement

관계형 데이터베이스(RDB) 테이블 중심 모델링과 객체 지향 패러다임 간의 불일치를 해소하면서, 사용자(User), 게시글(Post), 댓글(Comment), 대댓글(Reply), 신고(Report) 도메인을 안정적으로 표현할 수 있는 JPA 엔티티 아키텍처가 필요합니다. 특히 외래키 기반 단방향/양방향 매핑 경계 설정, 지연 로딩을 통한 N+1 성능 문제 예방, 부모 댓글 삭제 시 하위 대댓글 보존을 위한 마스킹 정책, 신고 대상(게시글/댓글)의 외래키 무결성 보장 및 캡슐화된 도메인 객체 상태 변경 규칙을 코드베이스 레벨에서 명확히 정립해야 합니다.

## Solution

Spring Boot 3.5.11 / Java 21 환경에서 Spring Data JPA를 기반으로 `BaseTimeEntity`, `User`, `Post`, `Comment`, `Report` 엔티티를 구현합니다. `Post`와 `Comment` 간에만 선택적 양방향 연관관계를 수립하고 연관관계 편의 메서드 및 영속성 전이(CascadeType.ALL, orphanRemoval=true)를 적용합니다. `User`는 결합도를 낮추기 위해 단방향 조회를 유지하며, `Comment`는 자기참조(Self-referencing) 계층 구조와 소프트 마스킹(`isDeleted`)을 통해 대댓글 보존 정책을 구현합니다. `Report`는 Nullable 외래키 전략을 통해 게시글/댓글 신고를 RDB 무결성과 함께 처리합니다. 모든 `@ManyToOne`에는 지연 로딩(`FetchType.LAZY`)을 강제하며, `@NoArgsConstructor(access = AccessLevel.PROTECTED)`와 빌더/도메인 팩토리 메서드를 조합하여 객체 무결성을 보장합니다.

## User Stories

1. As a platform user, I want a persistent `User` entity containing an ID, unique email, and username, so that I have a clean identity on the platform without unneeded authentication overhead.
2. As a platform user, I want each `Post` I write to reference my `User` identity lazily, so that authorship is maintained without loading user details unnecessarily.
3. As a platform user, I want each `Post` to contain a title and content body, so that articles can store essential written material.
4. As a platform user, I want each `Post` to manage its own collection of `Comment` entities, so that all comments for a post can be viewed and managed together.
5. As a content reader, I want a `Post` deletion to automatically cascade and remove all associated comments and orphaned items, so that obsolete discussion data does not linger in the database.
6. As a commenter, I want each `Comment` to reference its author `User` and target `Post` via lazy-loaded foreign keys, so that discussion integrity is enforced at the database level.
7. As a commenter, I want to leave a reply to an existing comment (대댓글), so that hierarchical nested discussions can be formed via a parent-child self-referencing structure.
8. As a commenter, when a parent comment is deleted, I want its text to be masked with a deletion notice while keeping my child reply visible, so that the nested conversation thread remains intact.
9. As a platform user, I want to submit a `Report` for an offensive `Post`, so that community moderation can trace the complaint with a clear reason and author reference.
10. As a platform user, I want to submit a `Report` for an offensive `Comment`, so that moderation can act upon inappropriate comments.
11. As a database administrator, I want foreign keys for `Report` to leverage native relational constraints for posts and comments, so that orphaned reports cannot exist.
12. As a system operator, I want every entity to automatically record its creation timestamp and last updated timestamp, so that all records are auditable without manual timestamp handling.
13. As a backend engineer, I want all many-to-one and one-to-one associations to strictly use lazy fetching (`FetchType.LAZY`), so that unexpected N+1 query cascades and memory bloat are prevented.
14. As a backend engineer, I want entities to prohibit public setters and enforce protected default constructors, so that partial or corrupted entity states are impossible to instantiate.
15. As a backend engineer, I want domain factory methods and builder patterns on entities, so that creation intent is self-documenting and validation rules are centralized.
16. As a backend engineer, I want bidirectional association convenience methods (e.g., `post.addComment(...)`), so that in-memory collections and database foreign key owners remain synchronized.
17. As a QA / reviewer, I want isolated JPA persistence integration tests (`@DataJpaTest`), so that schema generation, cascading rules, lazy loading, and constraint validations are verified before deploying web layers.

## Implementation Decisions

- **Domain Entities & Associations**:
  - `BaseTimeEntity`: Abstract mapped superclass managing `createdAt` and `updatedAt` with JPA Auditing (`@EntityListeners(AuditingEntityListener.class)`).
  - `User`: Minimal profile entity holding `id` (PK, IDENTITY), `username` (not null), and `email` (unique, not null). Unidirectional relationship with content entities to avoid memory bloating and high coupling.
  - `Post`: Entity holding `id` (PK, IDENTITY), `title` (not null), `content` (TEXT, not null), `@ManyToOne(fetch = FetchType.LAZY)` to `User` (`user_id`), and bidirectional `@OneToMany` to `Comment` with `cascade = CascadeType.ALL` and `orphanRemoval = true`. Provides domain update (`update(title, content)`) and association helper (`addComment(comment)`).
  - `Comment`: Entity holding `id` (PK, IDENTITY), `content` (not null), `isDeleted` (boolean default false), `@ManyToOne(fetch = FetchType.LAZY)` to `User` (`user_id`), and `@ManyToOne(fetch = FetchType.LAZY)` to `Post` (`post_id`).
  - `Comment` Hierarchy (대댓글): Self-referencing `@ManyToOne(fetch = FetchType.LAZY)` `parent` and `@OneToMany(mappedBy = "parent")` `children`. Provides `addChildComment(child)` and `delete()` (setting `isDeleted = true` to preserve thread context).
  - `Report`: Entity holding `id` (PK, IDENTITY), `reason` (not null), reporter `User` (`@ManyToOne(fetch = FetchType.LAZY)`), and individual nullable lazy references: `post` (`@ManyToOne(fetch = FetchType.LAZY)`, nullable) and `comment` (`@ManyToOne(fetch = FetchType.LAZY)`, nullable). Provides static factories `ofPost(reporter, post, reason)` and `ofComment(reporter, comment, reason)`.

- **Encapsulation & Instantiation**:
  - `@NoArgsConstructor(access = AccessLevel.PROTECTED)` across all entities for proxy generation safety.
  - No `@Setter` annotations; mutations happen through explicit domain methods.
  - `@Builder` on constructors combined with domain static factory methods (`create`, `of`).

- **Infrastructure & Configuration**:
  - Enable JPA Auditing via `@EnableJpaAuditing` configuration class.
  - Spring Boot 3.5.11 compatibility.
  - In-memory H2 / test database setup for entity DDL generation and query inspection.

## Testing Decisions

- **Testing Seam**: The highest seam for this milestone is the **JPA Data Access / Persistence Layer Seam** via `@DataJpaTest` and Spring Data JPA repositories / `TestEntityManager`.
- **Test Quality & External Behavior**:
  - Tests verify observable schema behavior, relational integrity, constraint violations, and lazy proxy loading rather than internal field states.
  - Verify `User`, `Post`, and `Comment` insertion and identity generation.
  - Verify bidirectional `Post` -> `Comment` cascading persistence and orphan removal.
  - Verify parent comment soft deletion (`isDeleted = true`) preserves children replies in the collection.
  - Verify `Report` creation for both posts and comments with lazy loading.
  - Verify automatic `createdAt` and `updatedAt` auditing population.

## Out of Scope

- HTTP REST controllers, presentation endpoints, and WebMvc routing (scheduled for the upcoming CRUD milestone).
- User authentication, passwords, JWT tokens, Spring Security, and session management.
- Complex full-text search indexing and Elasticsearch integration.
- Production database migration scripts (Flyway/Liquibase).

## Further Notes

- Code location: `kanghyeonwoo/src/main/java/com/leets_8th_be/kanghyeonwoo/`
- Tests location: `kanghyeonwoo/src/test/java/com/leets_8th_be/kanghyeonwoo/`
- Obsidian tracking: `PARA/WORKSTATION/11. Circle/Leets/Leets 26-2 2주차 과제 - ERD 설계 및 Spring Entity 구현.md` and `Leets 26-2 2주차 PR 내용.md`.
