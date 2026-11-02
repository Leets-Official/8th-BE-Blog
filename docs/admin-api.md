# 관리자 신고 처리와 게시글 숨김

## 기존 MySQL 테이블 변경 및 관리자 지정

추가 컬럼은 `member.role`과 `post.status`입니다. 먼저 확인합니다.

```sql
USE leets;
SHOW COLUMNS FROM member LIKE 'role';
SHOW COLUMNS FROM post LIKE 'status';
```

없는 컬럼만 추가합니다. 이전에 `post.status`를 추가했다면 해당 ALTER는 실행하지 않습니다.

```sql
ALTER TABLE member ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'USER';
ALTER TABLE post ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE';
```

신규 DB 전체 생성 SQL은 [schema.sql](schema.sql)에 있습니다. 기본 `DDL_AUTO=update`에서도
컬럼 추가를 시도하며 기본값은 기존 회원 `USER`, 기존 게시글 `ACTIVE`입니다.

회원가입 API로 만든 계정을 DB에서 관리자로 지정합니다. 본인의 실제 이메일로 바꿔 실행합니다.

```sql
UPDATE member SET role = 'ADMIN' WHERE email = 'your-admin@example.com';
SELECT member_id, email, role FROM member WHERE email = 'your-admin@example.com';
```

공개 회원가입은 항상 `USER`로 생성합니다. 관리자 생성·승격 API는 제공하지 않습니다.
관리자 계정으로 로그인하고 Swagger **Authorize**에 `accessToken` 값만 입력합니다.
역할은 JWT 내용이 아닌 DB에서 요청마다 검사하므로 권한 회수도 기존 토큰에 즉시 적용됩니다.

## API

| Method | URL | 기능 |
| --- | --- | --- |
| GET | `/admin/reports` | 최신 신고부터 페이징 조회 |
| PATCH | `/admin/reports/{id}/resolve` | 신고 승인 또는 기각 |
| PATCH | `/admin/posts/{postId}/restore` | 게시글 숨김 해제 |

모두 관리자 인증이 필요합니다. 토큰 누락·위조·만료·미존재 회원은 401(`AUTH_401_1`),
일반 회원은 403(`AUTH_403_1`)입니다.

### 신고 목록

`GET /admin/reports?status=PENDING&page=0&size=10`

`status` 생략 시 전체 조회, `PENDING` / `RESOLVED`를 지정하면 해당 상태만 조회합니다.
페이지 기본값은 0, 크기 기본값은 10이며 크기는 1~100으로 보정합니다.
응답 `data`는 `reports`, `page`, `size`, `totalElements`, `totalPages`, `hasNext`를 포함합니다.
각 신고에는 `id`, `postId`, `memberId`(신고자), `reason`, `status`, `result`,
`postStatus`(게시글의 현재 상태), `createdAt`, `updatedAt`이 있습니다.

### 신고 처리

```json
{
  "result": "ACCEPTED"
}
```

- 승인 `ACCEPTED`: 신고를 `RESOLVED` / `ACCEPTED`로, 게시글을 `HIDDEN`으로 함께 변경합니다.
- 기각 `REJECTED`: 신고를 `RESOLVED` / `REJECTED`로 변경하고 게시글 상태는 그대로 둡니다.
- 이미 처리된 신고는 결과가 같아도 409(`REPORT_409_2`)입니다.
- 없는 신고는 404(`REPORT_404_1`), 누락·잘못된 결과는 400(`COMMON_400_1`)입니다.
- 응답 `data`는 목록의 신고 객체와 같은 구조이며 처리된 `status`, `result`, `postStatus`를 확인할 수 있습니다.
- 하나의 게시글에 여러 신고가 있으면 각 신고는 개별 처리합니다. 기각이 이전 승인의 숨김을 해제하지 않습니다.

승인과 숨김은 한 트랜잭션으로 처리합니다. 게시글 행을 먼저 잠그고 신고 행을 잠가
동시 중복 처리와 생성·수정·삭제·숨김 해제 간 상태 충돌을 방지합니다.

### 숨김 해제

`PATCH /admin/posts/{postId}/restore`는 본문 없이 호출합니다.
성공하면 `status: "ACTIVE"`인 게시글 응답을 반환합니다. 이미 ACTIVE여도 성공합니다.
없는 게시글은 404(`POST_404_1`)입니다. 처리 완료된 신고의 상태·결과는 변경하지 않습니다.

## 일반 API에 미치는 영향

- 게시글 응답에 `status`를 추가합니다.
- 목록은 ACTIVE만 포함하며 페이지 개수도 ACTIVE 게시글 기준입니다.
- HIDDEN 게시글의 상세·수정·삭제, 댓글 목록·작성, 신규 신고는 404입니다.
- 관리자는 신고 목록에서 숨긴 게시글의 ID와 상태를 확인하고 숨김을 해제할 수 있습니다.
- 숨김은 게시글·댓글·신고를 삭제하지 않습니다. 숨김을 해제하면 기존 댓글도 다시 조회할 수 있습니다.
- ACTIVE 게시글 삭제 시 기존 동작대로 댓글·신고도 함께 삭제합니다.
