# 게시글 API 유저 스토리 및 사용 시나리오

## 1. API 기능 목록

| 도메인 | 기능 | HTTP Method | API Path |
|---|---|---|---|
| 게시글 | 게시글 목록 조회 | GET | `/posts?page=1&size=10` |
| 게시글 | 게시글 상세 조회 | GET | `/posts/{post_id}` |
| 게시글 | 게시글 생성 | POST | `/posts` |
| 게시글 | 게시글 수정 | PATCH | `/posts/{post_id}` |
| 게시글 | 게시글 삭제 | DELETE | `/posts/{post_id}` |

# API 명세

## 1. 게시글 목록 조회

**Method:** `GET`  
**Path:** `/posts?page=1&size=10`

### Request

| 위치 | 이름 | 타입 | 필수 | 설명 |
|---|---|---|---|---|
| Query | page | int | X | 페이지 번호, 1부터 시작, 기본값 1 |
| Query | size | int | X | 페이지 크기, 1~50, 기본값 10 |

### Request Body

```json
{}
```

### Response

**Status:** `200 OK`  
**Content-Type:** `application/json`

```json
{
  "isSuccess": true,
  "code": 200,
  "data": {
    "posts": [
      {
        "postId": 1,
        "title": "새 게시글",
        "author": "작성자1",
        "createdAt": "2026-10-10T13:30:00",
        "commentCount": 3
      },
      {
        "postId": 2,
        "title": "새 게시글",
        "author": "작성자2",
        "createdAt": "2026-10-10T12:15:00",
        "commentCount": 5
      }
    ],
    "page": 1,
    "size": 10,
    "totalElements": 2,
    "totalPages": 1,
    "hasNext": false
  },
  "error": null
}
```

### Error Response

**Status:** `500 Internal Server Error`  
**Content-Type:** `application/json`

```json
{
  "isSuccess": false,
  "code": 500,
  "data": null,
  "error": {
    "code": "INTERNAL_SERVER_ERROR",
    "message": "서버 내부 오류가 발생했습니다."
  }
}
```

### 예외 상황

| HTTP 상태 | 예외 상황 | 에러 코드 |
|---|---|---|
| 500 | 데이터베이스 조회 실패 | `INTERNAL_SERVER_ERROR` |
| 400 | 페이지 크기 제한 초과 | `INVALID_PAGE_SIZE` |
| 400 | 잘못된 페이지 번호 | `INVALID_PAGE_NUMBER` |
| 400 | 잘못된 정렬 기준 | `INVALID_SORT_TYPE` |

- **데이터베이스 조회 실패:** 데이터베이스 연결 실패 또는 목록 조회 과정 중 서버 내부 오류가 발생한 경우
- **페이지 크기 제한 초과:** 설정된 한 페이지당 조회 가능한 최대 개수를 초과하여 요청한 경우
- **잘못된 페이지 번호:** `page` 파라미터로 0 이하의 값이나 숫자가 아닌 문자를 요청한 경우
- **잘못된 정렬 기준:** 지원되는 정렬 기준이 `latest`, `oldest`, `popular`일 때 `random` 등을 요청한 경우

---

## 2. 게시글 상세 조회

**Method:** `GET`  
**Path:** `/posts/{post_id}`

### Request

| 위치 | 이름 | 타입 | 필수 | 설명 |
|---|---|---|---|---|
| Path | postId | Long | O | 조회할 게시글 ID (1 이상) |

### Request Body

```json
{}
```

### Response

**Status:** `200 OK`  
**Content-Type:** `application/json`

```json
{
  "isSuccess": true,
  "code": "200",
  "data": {
    "postId": 10,
    "title": "새 게시글",
    "content": "내용입니다",
    "createdAt": "2026-10-07T13:00:00",
    "updatedAt": "2026-10-07T13:00:00",
    "deletedAt": null,
    "member": {
      "memberId": 1,
      "name": "홍길동"
    }
  },
  "error": null
}
```

### Error Response

**Status:** `400 BAD_REQUEST`

```json
{
  "isSuccess": false,
  "code": "404",
  "data": null,
  "error": {
    "code": "NOT_FOUND",
    "message": "해당 게시글을 찾을 수 없습니다."
  }
}
```

### 예외 상황

| HTTP 상태 | 예외 상황 | 에러 코드 |
|---|---|---|
| 403 | 권한 없음 | `POST_ACCESS_DENIED` |
| 404 | 삭제된 글 조회 | `POST_NOT_FOUND` |
| 400 | 잘못된 형식 | `INVALID_POST_ID` |

---

## 3. 게시글 생성

**Method:** `POST`  
**Path:** `/posts`

### Request

| 위치 | 이름 | 타입 | 필수 | 설명 |
|---|---|---|---|---|
| Header | Authorization | string | O | `Bearer {accessToken}` |
| Body | title | string | O | 공백 문자만 입력 불가, 100자 이하 |
| Body | content | string | O | 공백 문자만 입력 불가 |

### Request Body

```json
{
  "title": "새 게시글",
  "content": "내용입니다"
}
```

### Response

**Status:** `201 Created`  
**Content-Type:** `application/json`

```json
{
  "isSuccess": true,
  "code": "201",
  "data": {
    "postId": 10,
    "title": "새 게시글",
    "content": "내용입니다",
    "createdAt": "2026-10-07T13:00:00"
  },
  "error": null
}
```

### Error Response

**Status:** `400 BAD_REQUEST`

```json
{
  "isSuccess": false,
  "code": "404",
  "data": null,
  "error": {
    "code": "NOT_FOUND",
    "message": "~~~"
  }
}
```

### 예외 상황

| HTTP 상태 | 예외 상황 | 에러 코드 |
|---|---|---|
| 400 | 필드 누락 (제목 및 내용) | `REQUIRED_FIELD_MISSING` |
| 400 | 제목 길이 제한 초과 | `TITLE_LENGTH_EXCEED` |
| 400 | 본문 길이 제한 초과 | `CONTENT_LENGTH_EXCEED` |
| 401 | 로그인하지 않은 사용자 | `UNAUTHORIZED` |

---

## 4. 게시글 수정

**Method:** `PATCH`  
**Path:** `/posts/{post_id}`

### Request

| 위치 | 이름 | 타입 | 필수 | 설명 |
|---|---|---|---|---|
| Header | Authorization | string | O | `Bearer {accessToken}` |
| Path | postId | Long | O | 수정할 게시글 ID (1 이상) |
| Body | title | string | O | 공백 불가, 100자 이하 |
| Body | content | string | O | 공백 불가 |

### Request Body

```json
{
  "title": "수정된 게시글",
  "content": "수정된 내용입니다"
}
```

### Response

**Status:** `200 OK`  
**Content-Type:** `application/json`

```json
{
  "isSuccess": true,
  "code": "200",
  "data": {
    "postId": 10,
    "title": "수정된 게시글",
    "content": "수정된 내용입니다",
    "createdAt": "2026-10-07T13:00:00"
  },
  "error": null
}
```

### Error Response

**Status:** `404 POST_NOT_FOUND`

```json
{
  "isSuccess": false,
  "code": "404",
  "data": null,
  "error": {
    "code": "POST_NOT_FOUND",
    "message": "~~~"
  }
}
```

### 예외 상황

| HTTP 상태 | 예외 상황 | 에러 코드 |
|---|---|---|
| 400 | 잘못된 게시글 ID 형식 | `INVALID_POST_ID` |
| 400 | 필드 누락 (제목 또는 내용) | `REQUIRED_FIELD_MISSING` |
| 400 | 제목 길이 제한 초과 | `TITLE_LENGTH_EXCEED` |
| 400 | 본문 길이 제한 초과 | `CONTENT_LENGTH_EXCEEDED` |
| 401 | 로그인하지 않은 사용자 | `UNAUTHORIZED` |
| 403 | 수정 권한 없음 (작성자가 아님) | `POST_ACCESS_DENIED` |
| 404 | 존재하지 않거나 삭제된 게시글 | `POST_NOT_FOUND` |

---

## 5. 게시글 삭제

**Method:** `DELETE`  
**Path:** `/posts/{post_id}`

### Request

| 위치 | 이름 | 타입 | 필수 | 설명 |
|---|---|---|---|---|
| Header | Authorization | string | O | `Bearer {accessToken}` |
| Path | postId | Long | O | 삭제할 게시글 ID (1 이상) |

### Request Body

```json
{}
```

### Response

**Status:** `200 OK`  
**Content-Type:** `application/json`

```json
{
  "isSuccess": true,
  "code": "204",
  "data": {
    "postId": 10
  },
  "error": null
}
```

### Error Response

**Status:** `404 NOT_FOUND`  
**Content-Type:** `application/json`

```json
{
  "isSuccess": false,
  "code": "404",
  "data": null,
  "error": {
    "code": "POST_NOT_FOUND",
    "message": "해당 게시글을 찾을 수 없습니다."
  }
}
```

### 예외 상황

| HTTP 상태 | 예외 상황 | 에러 코드 |
|---|---|---|
| 400 | 잘못된 게시글 ID 형식 | `INVALID_POST_ID` |
| 401 | 로그인하지 않은 사용자 | `UNAUTHORIZED` |
| 403 | 삭제 권한 없음 (작성자가 아님) | `POST_ACCESS_DENIED` |
| 404 | 존재하지 않거나 삭제된 게시글 | `POST_NOT_FOUND` |


## 2. 유저 스토리

| 기능 | 유저 스토리 |
|---|---|
| 게시글 목록 조회 | 사용자는 다른 사람들이 작성한 게시글을 확인하기 위해 게시글 목록을 조회할 수 있다. |
| 게시글 상세 조회 | 사용자는 관심 있는 게시글의 내용을 확인하기 위해 특정 게시글을 조회할 수 있다. |
| 게시글 생성 | 사용자는 자신의 생각이나 정보를 공유하기 위해 새로운 게시글을 작성할 수 있다. |
| 게시글 수정 | 사용자는 자신이 작성한 게시글의 내용을 변경하기 위해 게시글을 수정할 수 있다. |
| 게시글 삭제 | 사용자는 더 이상 공개하고 싶지 않은 자신이 작성한 게시글을 삭제할 수 있다. |

## 3. 기능별 사용 시나리오

### 3.1 게시글 목록 조회

**사용자:** 게시판 방문자

**사전 조건:** 게시판에 접근할 수 있는 상태

**기본 흐름**

1. 사용자가 게시판에 접속한다.
2. 시스템이 게시글 목록 조회 API를 호출한다.
3. 서버가 게시글 목록을 조회한다.
4. 시스템이 조회된 게시글 목록을 화면에 표시한다.

**예외 흐름**

조회에 실패하면 상황에 맞는 오류 메시지를 표시한다.

- E-1. 유효하지 않은 요청 파라미터 (400 Bad Request)
  - `INVALID_PAGE_SIZE`: 설정된 한 페이지당 조회 가능한 최대 개수를 초과하여 요청한 경우
  - `INVALID_PAGE_NUMBER`: 0 이하의 숫자나 유효하지 않은 문자형 페이지 번호를 요청한 경우
- E-2. 서버 및 데이터베이스 장애 (500 Internal Server Error)
  - `INTERNAL_SERVER_ERROR`: 데이터베이스 연결 실패 또는 조회 과정 중 서버 내부 오류가 발생한 경우

### 3.2 게시글 상세 조회

**사용자:** 게시판 방문자

**사전 조건:** 조회하려는 게시글이 존재한다.

**기본 흐름**

1. 사용자가 게시글 목록에서 특정 게시글을 선택한다.
2. 시스템이 해당 게시글의 상세 조회 API를 호출한다.
3. 서버가 게시글 정보를 반환한다.
4. 시스템이 게시글 제목과 내용을 화면에 표시한다.

**예외 흐름**

조회에 실패하면 상황에 맞는 오류 메시지를 표시한다.

- E-1. 유효하지 않은 요청 형식 (400 Bad Request / `INVALID_POST_ID`)
- E-2. 게시글 접근 권한 없음 (403 Forbidden / `POST_ACCESS_DENIED`)
- E-3. 존재하지 않거나 삭제된 게시글 (404 Not Found / `POST_NOT_FOUND`)

### 3.3 게시글 생성

**사용자:** 로그인한 사용자

**사전 조건:** 사용자가 로그인한 상태

**기본 흐름**

1. 사용자가 글쓰기 버튼을 클릭한다.
2. 제목과 내용을 입력한다.
3. 등록 버튼을 클릭한다.
4. 시스템이 게시글 생성 API를 호출한다.
5. 서버가 게시글을 저장한다.
6. 시스템이 작성된 게시글을 표시한다.

**예외 흐름**

생성에 실패하면 상황에 맞는 오류 메시지를 표시한다.

- E-1. 필수 필드 누락 (400 Bad Request / `REQUIRED_FIELD_MISSING`)
- E-2. 제목 길이 제한 초과 (400 Bad Request / `TITLE_LENGTH_EXCEEDED`)
- E-3. 본문 길이 제한 초과 (400 Bad Request / `CONTENT_LENGTH_EXCEEDED`)
- E-4. 로그인하지 않은 사용자 (401 Unauthorized / `UNAUTHORIZED`)

### 3.4 게시글 수정

**사용자:** 게시글 작성자

**사전 조건:** 사용자가 로그인했으며 해당 게시글의 작성자이다.

**기본 흐름**

1. 사용자가 자신이 작성한 게시글에 접속한다.
2. 수정 버튼을 클릭하고 제목 또는 내용을 수정한다.
3. 저장 버튼을 클릭한다.
4. 시스템이 게시글 수정 API를 호출한다.
5. 서버가 수정된 내용을 저장하고 결과를 반환한다.

**예외 흐름**

수정에 실패하면 상황에 맞는 오류 메시지를 표시한다.

- E-1. 잘못된 게시글 ID 형식 (400 Bad Request / `INVALID_POST_ID`)
- E-2. 필수 필드 누락 (400 Bad Request / `REQUIRED_FIELD_MISSING`)
- E-3. 제목 길이 제한 초과 (400 Bad Request / `TITLE_LENGTH_EXCEEDED`)
- E-4. 본문 길이 제한 초과 (400 Bad Request / `CONTENT_LENGTH_EXCEEDED`)
- E-5. 로그인하지 않은 사용자 (401 Unauthorized / `UNAUTHORIZED`)
- E-6. 수정 권한 없음 (403 Forbidden / `POST_ACCESS_DENIED`)
- E-7. 존재하지 않거나 삭제된 게시글 (404 Not Found / `POST_NOT_FOUND`)

### 3.5 게시글 삭제

**사용자:** 게시글 작성자

**사전 조건:** 사용자가 로그인했으며 해당 게시글의 작성자이다.

**기본 흐름**

1. 사용자가 자신이 작성한 게시글에 접속한다.
2. 삭제 버튼을 클릭한다.
3. 시스템이 삭제 확인 메시지를 표시한다.
4. 사용자가 삭제를 확인한다.
5. 시스템이 게시글 삭제 API를 호출한다.
6. 서버가 게시글을 삭제한다.
7. 시스템이 게시글 목록 화면으로 이동한다.

**예외 흐름**

삭제에 실패하면 상황에 맞는 오류 메시지를 표시한다.

- E-1. 잘못된 게시글 ID 형식 (400 Bad Request / `INVALID_POST_ID`)
- E-2. 로그인하지 않은 사용자 (401 Unauthorized / `UNAUTHORIZED`)
- E-3. 삭제 권한 없음 (403 Forbidden / `POST_ACCESS_DENIED`)
- E-4. 존재하지 않거나 이미 삭제된 게시글 (404 Not Found / `POST_NOT_FOUND`)