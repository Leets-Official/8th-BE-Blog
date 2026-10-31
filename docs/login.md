# 로그인

`POST /auth/login` · `Content-Type: application/json`

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
    "tokenType": "Bearer",
    "expiresIn": 3600
  }
}
```

- JWT는 HS256으로 서명하며, 회원 ID(`sub`), 발급 시각(`iat`), 만료 시각(`exp`)을 담는다. 유효기간은 1시간이며, 검증 시 기본 시계 오차 허용이 적용된다.
- `JWT_MEMBER_ID_CLAIM`은 기본 `sub`이며, 필요한 경우 `memberId`로 설정할 수 있다.
- 입력값 오류는 400(`COMMON_400_1`), 미가입 이메일이나 비밀번호 불일치는 동일한 401(`AUTH_401_2`)로 응답한다.
- 기존 DB에 평문 비밀번호로 직접 넣은 회원은 로그인할 수 없다. 회원가입 API로 생성한 BCrypt 암호화 비밀번호를 사용한다.
- 현재 단계는 토큰 발급 및 검증까지다. 게시글 API의 인증·작성자 권한 연결은 다음 단계이며, 리프레시 토큰은 제공하지 않는다.

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
