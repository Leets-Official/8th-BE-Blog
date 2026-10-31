# 게시글 신고 API

`POST /posts/{postId}/reports` · 로그인 필요

Swagger에서 로그인 후 **Authorize**에 `accessToken` 값만 입력합니다.
직접 요청할 때는 `Authorization: Bearer <accessToken>` 헤더를 사용합니다.

```json
{
  "reason": "광고성 게시글입니다."
}
```

신고 사유는 필수이며 공백만 입력할 수 없고 최대 500자입니다.
신고자는 요청 본문이 아닌 로그인 토큰의 회원 ID로 결정합니다.

성공: HTTP 200

```json
{
  "isSuccess": true,
  "code": "COMMON_200_1",
  "message": "요청 응답이 성공했습니다.",
  "data": {
    "id": 1,
    "postId": 10,
    "reason": "광고성 게시글입니다.",
    "status": "PENDING",
    "result": null,
    "createdAt": "2026-11-01T12:00:00"
  }
}
```

## 상태와 중복 방지

- DB PK 컬럼은 `report.id`이며 응답에도 `id`를 사용합니다.
- 접수 시 `status`는 문자열 `PENDING`, `result`는 `NULL`로 저장합니다.
- 같은 회원은 같은 게시글을 한 번만 신고할 수 있습니다. 사유를 바꿔도 재신고는 불가합니다.
- 다른 회원이 동일 게시글을 신고하거나, 같은 회원이 다른 게시글을 신고할 수 있습니다.
- 사전 중복 확인과 `(post_id, member_id)` UNIQUE 제약조건을 함께 사용하여 동시 중복 요청도 차단합니다.
- 숨긴 게시글은 신규 신고할 수 없으며 404를 반환합니다.
- 관리자 처리 및 게시글 숨김·해제는 [admin-api.md](admin-api.md)에 있습니다.

## 오류

| HTTP | 코드 | 의미 |
| --- | --- | --- |
| 400 | `COMMON_400_1` | 신고 사유 입력 오류 |
| 401 | `AUTH_401_1` | 토큰 누락·만료·위조 또는 존재하지 않는 회원 |
| 404 | `POST_404_1` | 존재하지 않는 게시글 |
| 409 | `REPORT_409_1` | 같은 회원이 이미 신고한 게시글 |

## DB 및 게시글 삭제

이전에 안내한 MySQL `report` 테이블 구조와 매핑됩니다. 신규 생성 SQL은 [schema.sql](schema.sql)에 있습니다.
이미 테이블을 생성했다면 PK `id`와 `uk_report_post_member` UNIQUE 제약조건이 있는지 확인합니다.
기본 `DDL_AUTO=update`에서는 Hibernate가 테이블 생성을 시도하며, 이 SQL 파일은 자동 실행되지 않습니다.

게시글 삭제 시 연결된 댓글 및 신고를 같은 트랜잭션에서 함께 삭제합니다. 다른 게시글의 댓글·신고는 유지합니다.

`./gradlew.bat test`에서 신고 접수, 검증, 중복·동시 요청 방지와 신고가 있는 게시글 삭제를 H2 메모리 DB로 검증합니다.
