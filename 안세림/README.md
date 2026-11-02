# 1주차 과제 - 안세림

## 구현 내용
- GET /health : 서버 상태 확인 (ok 반환)
- POST /string/repeat : 입력한 문자열을 string_one, string_two로 반환

## 구조
- controller : 요청/응답 처리
- service : 문자열 처리 로직
- dto : 요청/응답 객체

## 참고
- 이번 과제는 DB를 사용하지 않아 DataSource 자동설정을 제외했습니다.

---

# 2주차 과제 - 안세림

## 구현 내용
- Member, Post, Comment 엔티티 구현
- 생성/수정 시간은 BaseTimeEntity로 분리

## 연관관계
- Post → Member : 다대일 (단방향)
- Comment → Member : 다대일 (단방향)
- Post ↔ Comment : 일대다 / 다대일 (양방향, 게시글 삭제 시 댓글도 함께 삭제)

## ERD에서 바꾼 부분
- Comment PK 타입 INT → BIGINT로 통일
- adminKey 제외 (참조 테이블 없음, 인증/권한은 과제 범위 아님)
- rdate, mdate → created_at, updated_at (DATETIME)
- 컬럼명 스네이크 케이스로 통일

---

# 3주차 과제 - 안세림

## 구현 API
| 기능 | Method | URI |
|---|---|---|
| 목록 조회 | GET | /posts?page=&size=&keyword= |
| 상세 조회 | GET | /posts/{postId} |
| 작성 | POST | /posts |
| 수정 (부분 수정) | PATCH | /posts/{postId} |
| 삭제 | DELETE | /posts/{postId}?authorId= |

## 구조
- controller : PostController (요청/응답)
- service : PostService (비즈니스 로직, 예외 발생)
- repository : PostRepository, MemberRepository (Spring Data JPA)
- dto/post : 요청/응답 DTO (record)
- converter : PostConverter (Entity → DTO 변환)
- global/response : ApiResponse (공통 응답 형식)
- global/exception : ErrorCode, CustomException, GlobalExceptionHandler (전역 예외 처리)
- config : DataInitializer (테스트용 회원 2명 생성)

## 참고
- DB는 설치 없이 테스트할 수 있도록 H2(메모리 DB)를 사용했습니다.
- 팀 설계에 없던 MEMBER4001(존재하지 않는 회원), COMMON404(존재하지 않는 경로) 에러 코드를 추가했습니다.

---

# 4주차 과제 - 안세림

## 구현 기능
| 기능 | API | 상태 변화 / 중복 방지 |
|---|---|---|
| 게시글 신고 | POST /posts/{postId}/reports | PENDING으로 생성, 같은 글 재신고 시 409 |
| 댓글 신고 | POST /comments/{commentId}/reports | PENDING으로 생성, 같은 댓글 재신고 시 409 |
| 신고 처리 완료 | POST /reports/{reportId}/resolve | PENDING → RESOLVED, 이미 처리된 신고면 409 |
| 게시글 숨김 | POST /posts/{postId}/hide | ACTIVE → HIDDEN, 이미 숨긴 글이면 409 |

## 참고
- 숨김 처리된 게시글은 목록에서 제외되고, 상세 조회 시 404를 반환합니다.
- 신고가 달린 게시글을 삭제할 때 FK 오류가 나지 않도록, 게시글과 댓글에 달린 신고를 먼저 삭제합니다.
- 테스트용 게시글 1개, 댓글 1개를 서버 시작 시 생성합니다.