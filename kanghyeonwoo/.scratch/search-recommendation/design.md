# 검색 기능 구현 설계안

Status: implemented

사용자와 확정한 요구사항은 [spec.md](spec.md), 용어는 [GLOSSARY.md](../../GLOSSARY.md)를 따른다. 사용자 구현 지시로 [티켓 분할안](ticket-proposal.md)의 작업을 진행한다.

## DB

파일 기반 H2를 사용한다. 테스트는 별도 인메모리 H2를 사용하여 로컬 파일과 격리한다. DB 파일은 Git 추적에서 제외한다.

### search_keywords

- `id`: PK
- `normalized_keyword`: 정규화한 검색어, 최대 50 Unicode 코드 포인트, UNIQUE. 보조 문자의 UTF-16 표현도 수용하도록 저장 컬럼은 VARCHAR(100).
- `suggestible`: 추천 사전 포함 여부
- `created_at`: 생성 시각

결과가 없는 검색어도 집계를 위해 행을 생성한다. 검색어 사전은 이 테이블에서 `suggestible=true`인 부분집합이다. 초기 데이터와 검색 결과가 있는 등록 조건 충족 검색어만 추천 대상으로 삼는다. 최초에는 결과가 없었어도 이후 결과가 생기면 추천 대상으로 승격한다.

### search_events

- `id`: PK
- `keyword_id`: search_keywords FK
- `searched_at`: 집계된 검색 시각
- 인덱스: `(keyword_id, searched_at)`, `(searched_at, keyword_id)`

중복 제외를 통과한 검색만 저장한다. 제외된 모든 HTTP 요청을 별도 저장하지 않는다. 검색어 사전 등록 여부와 무관하게 유효한 검색을 기록한다. 30일이 지난 이력은 주기적으로 삭제한다.

### search_dedup_states

- `id`: PK
- `keyword_id`: search_keywords FK
- `session_key`: 원본 세션 ID 대신 서버에서 생성한 세션별 불투명 식별자
- `last_counted_at`: 마지막 집계 시각
- 제약: UNIQUE `(keyword_id, session_key)`

집계 판정과 이력 저장 및 마지막 집계 시각 변경은 하나의 트랜잭션에서 처리한다. 단일 서버의 검색 쓰기 잠금을 커밋 완료까지 유지하고, 같은 검색어의 판정에는 DB 잠금도 적용한다. 유니크 제약으로 검색어와 중복 상태의 유일성을 보장한다. 오래된 중복 판정 상태도 정리한다.

### 기존 posts

원문 제목을 유지하고 `normalized_title`을 추가한다. 검색어와 같은 규칙으로 생성·수정 시 갱신하고 기존 제목은 시작 시 누락분을 채운다. 기존 대소문자 무시 제목 포함 검색은 정규화 제목의 리터럴 포함 검색으로 연결한다. `%`, `_` 같은 문자는 검색 와일드카드로 해석하지 않는다.

### search_keyword_buckets

- `id`: PK
- `keyword_id`: search_keywords FK
- `bucket_start`: UTC 분 시작 시각
- `search_count`: 해당 분에 집계된 검색 횟수
- 제약: UNIQUE `(keyword_id, bucket_start)`
- 인덱스: `(bucket_start, keyword_id)`

검색 이력·마지막 집계 상태·버킷 증가를 같은 트랜잭션으로 처리한다. 요청 시각 기준 정확한 60분 집계를 위해 완전한 분은 버킷에서, 양 끝의 부분 분은 원본 이력에서 읽는다. 시작 시 원본 이력으로 버킷을 복구하고 30일이 지난 버킷도 정리한다. B-tree 인덱스는 기간 조회를 지원하며 철자 유사도 탐색은 Java BK-tree가 담당한다.

검색용 테이블은 명시적 SQL 스키마와 JdbcTemplate 기반 SearchRepository로 관리한다. 기존 게시글·사용자는 JPA를 유지한다. 두 접근 방식은 같은 데이터소스와 트랜잭션 관리자를 사용한다.

## 서비스와 데이터 흐름

- `SearchNormalization`: NFC, Locale.ROOT 소문자화, 앞뒤 공백 제거, 연속 공백 통합. 길이와 편집 거리는 Unicode 코드 포인트 기준으로 일치시킨다.
- `SearchTrackingService`: 첫 페이지의 유효한 게시글 검색 기록, 세션별 중복 판정, 사전 승격.
- `SearchSuggestionService`: BK-tree 후보 탐색, 거리 및 최근 검색량 기반 정렬.
- `SearchTrendService`: 두 60분 구간 집계 및 상승 점수 순위 계산.
- `SearchKeywordStatsService`: 시간대별 검색량과 두 60분 구간의 검색량·상승 점수 조회.

PostSearchService는 PostService의 조회가 끝난 뒤 첫 페이지·검색어 조건을 판단하고 검색 결과 존재 여부와 세션 키를 SearchTrackingService에 전달한다. HTTP 세션 접근은 config의 인자 해석기에 분리하며 서비스는 Servlet 타입에 의존하지 않는다. 추천·트렌드·통계 조회는 검색 이력을 추가하지 않는다. 추적 저장에 실패하면 요청 전체를 실패 처리하고 부분 집계는 남기지 않는다.

DB를 원본으로 삼는다. 시작 시 초기 단어를 멱등 등록한 뒤 추천 사전을 읽어 BK-tree를 구성한다. 사전 변경은 커밋 후 트리에 반영하며, 트리 갱신 실패 시 DB에서 재구성한다. 트리 조회·변경을 동기화하여 중간 상태를 노출하지 않는다.

초기 단어 예시: spring, spring boot, java, jpa, 스프링, 스프링부트, 자바. 초기 단어 등록은 검색량을 생성하지 않는다.

## API 응답

기존 ApiResponse 래퍼를 유지한다. 아래는 data 부분의 형태다.

### GET /search/suggestions?keyword=sprng

```json
[{"keywordId":1,"keyword":"spring","distance":1,"recentSearchCount":8}]
```

입력은 정규화 후 2~50자. 정확히 같은 단어는 제외한다. 2~4자는 거리 1 이하, 5~50자는 거리 2 이하. 거리 오름차순, 최근 검색량 내림차순, 검색어 사전순으로 최대 5개. 후보가 없으면 빈 배열.

### GET /search/trends

```json
[{"rank":1,"keywordId":1,"keyword":"spring","currentCount":10,"previousCount":2,"riseScore":1.6}]
```

최근 검색량 5 이상이고 증가량이 양수인 검색어만 포함한다. 결과가 없었던 검색어도 집계 대상이다. 상승 점수 `(currentCount-previousCount)/max(previousCount,5)`는 부동소수점 나눗셈으로 계산한다. 점수, 최근 검색량 내림차순 후 검색어 사전순으로 최대 10개. 후보가 없으면 빈 배열. 퍼센트 상승률과 상승 점수를 혼용하지 않는다.

### GET /search/keywords/{keywordId}/stats

```json
{
  "keywordId":1,"keyword":"spring",
  "currentCount":10,"previousCount":2,"riseScore":1.6,
  "hourlyCounts":[{"startAt":"2026-10-26T14:00:00+09:00","count":3,"partial":true}]
}
```

예시는 시간대 한 개만 생략 없이 표현한 형식 예시다. 실제 hourlyCounts는 현재 시간대를 포함한 24개의 연속된 시간대를 시간순으로 반환한다. 시간 경계는 Asia/Seoul 기준이며 DB 시각은 UTC로 유지한다. 현재 구간은 요청 시각까지만 집계하고 partial=true로 표시한다. 기존 ID에 이력이 없으면 0을 채우고, 존재하지 않는 ID는 404를 반환한다.

요청마다 기준 시각을 한 번 고정한다. 급상승 구간은 `[기준-120분, 기준-60분)`, `[기준-60분, 기준)`로 계산한다. 통계의 시간 구간도 시작 포함·끝 제외로 계산한다.

## 검증

- 정규화: 대소문자, NFC, 공백, 길이 경계와 리터럴 검색.
- BK-tree: 전체 사전 편집 거리 계산 결과와 후보 일치, 거리 경계 및 순위 동점.
- 집계: 첫 페이지·빈 검색어·검색 결과 없음·세션별 독립성·5분 직전과 정확한 경계·동시 요청.
- 트렌드와 통계: 직전 검색량 0, 최소 검색량, 60분 경계, 동점, 시간대 0 채움, 미존재 ID.
- 수명주기: 파일 DB 재접속 후 사전·이력 복원, 중복 없는 초기 데이터, 신규 사전 반영 및 30일 정리.
- 기존 게시글 CRUD와 검색 테스트 회귀 확인.

Clock을 주입하여 시간을 고정한 테스트를 작성한다. 분 단위 집계 테이블은 포함하며 다중 서버 동기화와 과거 순위 스냅샷은 현재 범위에 포함하지 않는다.
