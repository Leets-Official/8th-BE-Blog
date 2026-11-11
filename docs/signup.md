# 회원가입

`POST /auth` (기존 경로: `POST /auth/signup`) · `Content-Type: application/json`

```json
{
  "email": "yeji@example.com",
  "password": "password123",
  "nickname": "예지"
}
```

- 이메일: 필수, 이메일 형식, 최대 255자. 앞뒤 공백 제거 및 소문자 변환 후 중복 검사.
- 비밀번호: 공백만 입력 불가, 8~50자 및 UTF-8 기준 72바이트 이하. BCrypt로 암호화하여 저장.
- 닉네임: 필수, 최대 50자. 앞뒤 공백을 제거하고 중복 검사.

성공: HTTP 200

```json
{
  "isSuccess": true,
  "code": "COMMON_200_1",
  "message": "요청 응답이 성공했습니다.",
  "data": {
    "memberId": 1,
    "email": "yeji@example.com",
    "nickname": "예지"
  }
}
```

입력 오류는 400, 중복 이메일은 409(`AUTH_409_1`), 중복 닉네임은 409(`AUTH_409_2`)로 응답한다. 회원가입은 로그인이나 JWT 발급을 수행하지 않는다.

이메일·닉네임 중복은 `uk_member_email`, `uk_member_nickname` DB 제약조건으로 동시 요청에서도 차단한다. 신규 DB용 `schema.sql`에도 반영되어 있다. 기존 DB는 이메일·닉네임을 정규화했을 때 중복되는 회원이 있는지 확인하고 정리한 뒤 제약조건을 추가해야 한다. 기존 회원 데이터 및 비밀번호는 자동 변환하지 않는다.

기본 `DDL_AUTO=update`에서는 새 테이블과 제약조건을 Hibernate가 추가한다. 기존 닉네임 중복 데이터가 있으면 제약조건 생성이 실패할 수 있으므로 실행 로그와 DB 제약조건을 확인한다. `DDL_AUTO=validate` 또는 `none`을 사용하는 기존 DB에서는 `schema.sql`의 `refresh_token` 테이블을 생성하고, 닉네임 중복을 정리한 뒤 아래 SQL을 한 번 실행한다.

```sql
SELECT TRIM(nickname), COUNT(*) FROM member GROUP BY TRIM(nickname) HAVING COUNT(*) > 1;
-- 위 결과 및 앞뒤 공백을 가진 기존 닉네임을 정리한 후 실행
ALTER TABLE member ADD CONSTRAINT uk_member_nickname UNIQUE (nickname);
```
