# 로그인

`POST /login` (기존 경로: `POST /auth/login`) · `Content-Type: application/json`

```json
{
  "email": "yeji@example.com",
  "password": "password123"
}
```

가입할 때처럼 이메일의 앞뒤 공백을 제거하고 소문자로 변환한다. 비밀번호는 원문 그대로 비교한다.

성공: HTTP 200

```json
{
  "isSuccess": true,
  "code": "COMMON_200_1",
  "message": "요청 응답이 성공했습니다.",
  "data": {
    "accessToken": "발급된 JWT",
    "refreshToken": "발급된 Refresh Token",
    "tokenType": "Bearer",
    "expiresIn": 3600,
    "refreshExpiresIn": 1209600
  }
}
```

- JWT는 HS256으로 서명하며, 회원 ID(`sub`), 발급 시각(`iat`), 만료 시각(`exp`), 고유 ID(`jti`)를 담는다. 유효기간은 1시간이며, 검증 시 기본 시계 오차 허용이 적용된다.
- `JWT_MEMBER_ID_CLAIM`은 기본 `sub`이며, 필요한 경우 `memberId`로 설정할 수 있다.
- 입력값 오류는 400(`COMMON_400_1`), 미가입 이메일이나 비밀번호 불일치는 동일한 401(`AUTH_401_2`)로 응답한다.
- 기존 DB에 평문 비밀번호로 직접 넣은 회원은 로그인할 수 없다. 회원가입 API로 생성한 BCrypt 암호화 비밀번호를 사용한다.
- Spring Security의 JWT 필터가 토큰을 검증하고 DB에서 현재 회원과 권한을 조회해 SecurityContext에 인증 정보를 설정한다. 세션을 생성하지 않는다.
- 게시글 작성·수정·삭제, 댓글 작성, 신고는 인증이 필요하고, `/admin/**`는 ADMIN 권한이 필요하다. 수정·삭제는 추가로 작성자 여부를 검사한다.

## 토큰 재발급

`POST /auth/refresh` · `Content-Type: application/json`

```json
{
  "refreshToken": "로그인 응답으로 받은 Refresh Token"
}
```

Access Token 없이 요청할 수 있으며, 성공 응답은 로그인과 같은 형식이다. 새 Access Token과 Refresh Token을 모두 저장해서 다음 요청에 사용한다.

- Refresh Token은 256비트 난수이며 유효기간은 발급 시점부터 14일이다. DB에는 SHA-256 해시만 저장한다.
- 재발급 시 기존 Refresh Token은 폐기한다. 동일 토큰의 동시 요청은 하나만 성공한다.
- 만료·위조·재사용한 토큰, Access Token을 대신 보낸 경우 또는 삭제된 회원의 토큰은 401(`AUTH_401_3`)이다. 토큰 누락·공백·길이 초과는 400이다.
- Refresh Token은 API 인증용 Bearer Token으로 사용할 수 없다. Refresh Token도 만료되면 다시 로그인한다.
- 재발급 전에 발급된 Access Token은 자신의 만료 시각까지 유효하다.

## 실행 설정

애플리케이션 실행 전에 UTF-8 기준 최소 32바이트의 무작위 비밀키를 `JWT_SECRET` 환경변수로 설정한다. 키가 없거나 너무 짧으면 시작에 실패한다. 키는 저장소에 커밋하지 않는다.

PowerShell에서 현재 터미널용 키를 생성하고 실행하는 예시:

```powershell
$jwtKeyBytes = New-Object byte[] 32
$jwtRandom = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$jwtRandom.GetBytes($jwtKeyBytes)
$jwtRandom.Dispose()
$env:JWT_SECRET = [Convert]::ToBase64String($jwtKeyBytes)
.\gradlew.bat bootRun
```

이 예시는 매번 새 키를 만든다. 같은 키를 유지하려면 실행 환경에 키를 별도로 보관한다. 키가 바뀌면 이전 키로 발급한 토큰은 검증에 실패한다. MySQL 접속 설정은 기존 환경변수를 사용한다.

인증이 필요한 API에 연결할 때는 `Authorization: Bearer <accessToken>` 헤더를 사용한다.

## Swagger에서 테스트

1. 서버를 재시작하고 `http://localhost:8080/swagger-ui/index.html`을 새로고침한다.
2. 회원가입 후 `POST /auth/login`을 실행한다.
3. 응답의 `data.accessToken` 값을 복사한다.
4. 상단 **Authorize** 버튼을 누르고 **토큰 값만** 입력한다. `Bearer `는 입력하지 않는다.
5. **Authorize**를 누르고 창을 닫은 뒤 게시글 작성·수정·삭제를 실행한다. Swagger가 `Authorization: Bearer <accessToken>` 헤더를 자동으로 추가한다.

`JWT_SECRET`은 서버 서명용 비밀키이므로 Swagger에 넣지 않는다. Access Token이 만료되면 `/auth/refresh`로 교체한 뒤 Authorize 값을 갱신한다. 인증 없이 작성하면 401, 다른 회원의 글을 수정·삭제하면 403을 반환한다. 게시글 조회·목록과 회원가입·로그인은 인증 없이 사용할 수 있다.
