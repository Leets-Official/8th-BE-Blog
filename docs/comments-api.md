# 댓글 API

기존 `comment` 테이블을 사용하며 추가 컬럼은 필요하지 않습니다.

| Method | URL | 인증 | 설명 |
| --- | --- | --- | --- |
| POST | `/posts/{postId}/comments` | 필요 | 로그인한 회원으로 댓글 작성 |
| GET | `/posts/{postId}/comments` | 불필요 | 해당 게시글의 댓글을 작성 순서(ID 오름차순)로 조회 |

## 작성

Swagger의 **Authorize**에 로그인 응답의 `accessToken` 값만 입력한 뒤 작성합니다.
직접 요청할 때는 `Authorization: Bearer <accessToken>` 헤더를 사용합니다.

```json
{
  "content": "댓글 내용"
}
```

`content`는 필수이며, 공백만 입력할 수 없고 최대 10,000자입니다.
성공 시 HTTP 200과 기존 공통 응답 형식을 반환합니다.

```json
{
  "isSuccess": true,
  "code": "COMMON_200_1",
  "message": "요청 응답이 성공했습니다.",
  "data": {
    "commentId": 1,
    "postId": 1,
    "content": "댓글 내용",
    "writer": "예지",
    "createdDate": "2026-11-01"
  }
}
```

## 목록

목록 조회의 `data`는 위 댓글 객체의 배열입니다. 댓글이 없는 게시글은 빈 배열 `[]`을 반환합니다.
다른 게시글의 댓글은 포함하지 않습니다. 이번 단계는 전체 목록 조회이며 페이징은 제공하지 않습니다.

## 오류

- 입력값 오류: HTTP 400 / `COMMON_400_1`
- 작성 시 토큰 누락·만료·위조 또는 존재하지 않는 회원: HTTP 401 / `AUTH_401_1`
- 작성·조회 대상 게시글 없음: HTTP 404 / `POST_404_1`

숨긴 게시글도 작성·목록 조회에서 404를 반환합니다.

게시글을 삭제하면 기존 게시글 삭제 로직에서 연결된 댓글도 함께 삭제합니다.
