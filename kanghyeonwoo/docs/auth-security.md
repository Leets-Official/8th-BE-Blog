# 인증·인가 운영 및 API

## 실행 설정

JJWT 0.12.6을 사용한다. `JWT_SECRET`은 최소 32바이트 무작위 값의 Base64 문자열로 지정한다. 예: `openssl rand -base64 32`. 실제 키를 저장소에 커밋하지 않는다. 미설정 시 로컬 실습용 프로세스 임시 키가 생성되므로 재시작하면 기존 JWT는 모두 무효가 된다. 배포에서는 고정된 비밀 키를 환경변수로 제공한다.

기존 사용자 프로필·게시글·댓글은 보존한다. 비밀번호가 없는 기존 프로필은 일반 로그인할 수 없고, 같은 이메일로 재가입하거나 카카오 계정으로 자동 병합할 수 없다. 공개 비밀번호 설정·관리자 승격 API는 없다. 관리자는 신뢰할 수 있는 DB 운영 경로에서 `users.role`을 `ADMIN`으로 지정한다. 가입 요청의 role은 권한 부여에 사용하지 않는다.

## 기존 DB 사전 점검

데이터 백업 후 아래 조회 결과가 비어 있는지 확인한다. 중복이 있으면 사용자와 협의해 수동으로 정리한다. 자동 이름 변경이나 데이터 삭제는 하지 않는다. 시작 시에도 같은 충돌을 검사한다.

```sql
SELECT LOWER(TRIM(email)), COUNT(*) FROM users GROUP BY LOWER(TRIM(email)) HAVING COUNT(*) > 1;
SELECT TRIM(username), COUNT(*) FROM users GROUP BY TRIM(username) HAVING COUNT(*) > 1;
```

새 DB는 JPA가 `uk_user_email`, `uk_user_nickname`을 생성한다. 시작 시 기존 DB의 유니크 인덱스도 확인하고, 충돌이 없는데 필수 인덱스가 없으면 생성한다. 생성 실패 시 기동을 중단한다. 기존 DB에서는 Hibernate의 자동 갱신 로그와 실제 인덱스를 확인하고, 필요하면 중복 정리 후 다음 제약을 적용한다. 이미 같은 이름의 제약이 있으면 다시 실행하지 않는다.

```sql
ALTER TABLE users ADD CONSTRAINT uk_user_email UNIQUE (email);
ALTER TABLE users ADD CONSTRAINT uk_user_nickname UNIQUE (username);
```

## 일반 인증

- `POST /api/v2/auth/signup`: `{ "email": "user@example.com", "password": "password123", "nickname": "사용자" }`. 성공 201, 입력 오류 400, 중복 409.
- `POST /api/v2/auth/login`: 이메일·비밀번호. 성공 data는 `accessToken`, `refreshToken`, `expiresIn`(600초). 실패는 계정 유무와 관계없이 401.
- `POST /api/v2/auth/reissue`: `{ "refreshToken": "..." }`. 새 Access/Refresh를 반환한다. Refresh는 회전 시점부터 다시 7일이다.
- `POST /api/v2/auth/logout`: 같은 Refresh 요청 형식. 해당 로그인만 폐기한다. 발급된 Access Token은 최대 10분 남는다.

보호 API는 `Authorization: Bearer <accessToken>`을 사용한다. Refresh Token은 보호 API에 사용할 수 없다. 모든 업무 API는 로그인해야 하며 신고 목록·처리는 ADMIN만 허용된다. 요청의 authorId/reporterId/processorId는 무시하고 인증 주체를 사용한다. Swagger의 Authorize에서 BearerAuth를 설정한다.

Refresh 원문은 DB에 저장하지 않고 SHA-256 지문만 저장한다. 로그인별 레코드의 행 잠금으로 회전을 직렬화한다. 이전 Refresh 재사용 시 해당 로그인 전체를 폐기하며 거절 응답에서도 폐기가 커밋된다. 따라서 정상 클라이언트도 동시 재발급을 합쳐 하나만 보내야 한다.

## 실제 카카오 앱 설정

1. [카카오 개발자 콘솔](https://developers.kakao.com/)에서 앱을 만들고 카카오 로그인을 활성화한다.
2. 웹 플랫폼과 Redirect URI를 등록한다. 서버 콜백은 예를 들어 `http://localhost:8080/api/v2/auth/kakao/callback`이다. 환경변수 값과 정확히 일치해야 한다.
3. 카카오계정 이메일 동의 항목을 설정한다. 앱 권한에 따라 이메일 동의 항목 설정 자격을 준비해야 한다. 유효하고 인증된 이메일이 없으면 신규 가입이 거절된다.
4. `KAKAO_CLIENT_ID`에 REST API 키를 지정한다. 새 REST API 키는 Client Secret이 기본 활성화되므로 `KAKAO_CLIENT_SECRET`도 확인해 지정한다. 콘솔에서 명시적으로 비활성화한 경우에만 생략한다.
5. `KAKAO_CALLBACK_URL`에 등록한 서버 콜백, `KAKAO_FRONTEND_URL`에 프런트엔드 완료 화면의 고정 URL을 지정한다. 요청에서 임의 리다이렉트 URL을 받지 않는다.

[카카오 로그인 공식 REST 문서](https://developers.kakao.com/docs/ko/kakaologin/rest-api)의 인가 코드 교환 및 `/v2/user/me`를 실제 호출한다. 통신 연결·읽기는 각각 5초로 제한한다. 설정 누락은 503을 반환한다.

## 프런트엔드 카카오 흐름

1. 브라우저를 `GET /api/v2/auth/kakao/start`로 이동한다. 서버는 5분짜리 state와 HttpOnly 브라우저 연결 쿠키를 발급한다.
2. 카카오 콜백 검증 후 프런트엔드 URL에 60초짜리 일회용 `code`를 전달한다. JWT는 URL에 넣지 않는다.
3. `POST /api/v2/auth/kakao/exchange`에 `{ "code": "..." }`를 보낸다. 시작 브라우저의 쿠키가 필요하다. 기존 회원은 `data.tokens`, 신규 회원은 `data.signupToken`을 받는다.
4. 신규 회원은 닉네임을 입력하고 `POST /api/v2/auth/kakao/signup`에 `{ "signupToken": "...", "nickname": "..." }`를 보내 JWT를 받는다. 가입용 토큰은 5분 동안 유효하다. 닉네임 충돌이면 다른 닉네임으로 재시도할 수 있다.

브라우저 쿠키는 OAuth 요청 연결에만 사용하며 업무 API 인증에는 사용하지 않는다. 세션은 만들지 않는다. 프런트엔드와 API를 같은 사이트의 리버스 프록시로 제공하는 구성을 전제로 한다. 다른 사이트 배포는 별도 CORS·쿠키 정책 설계가 필요하다. HTTPS에서는 Secure 쿠키를 발급한다.

카카오 앱 설정이 아직 없어 실계정 로그인은 미검증이다. 설정 이후 시작 → 동의 → 콜백 → 코드 교환 → 신규 닉네임 가입 → 재로그인까지 실제 브라우저로 확인해야 한다. 이메일 중복 계정·동의 거절도 확인한다.

## 로컬 환경 파일 사용

`.env.example`은 변수 목록 예시다. 실제 키는 Git에서 제외된 `.env.kakao.local`에 저장하고 파일 권한을 600으로 제한한다. Spring Boot는 dotenv 파일을 자동으로 읽지 않으므로, 직접 작성·확인한 파일만 아래처럼 내보내고 실행한다.

```bash
set -a
source ./.env.kakao.local
set +a
./gradlew bootRun
```

프런트엔드 완료 화면은 별도로 준비해야 한다. `KAKAO_FRONTEND_URL`에 아무 페이지나 지정하면 리다이렉트 이후 코드 교환·닉네임 가입을 완료할 수 없다.

## 코드 품질 검사

프로젝트 디렉터리에서 다음 명령을 사용한다. Spotless Gradle 플러그인으로 포맷을 관리하며 별도 전역 설치가 필요 없다.

```bash
./gradlew spotlessApply    # 공백 4칸 들여쓰기·줄바꿈·미사용 import 자동 정리
./gradlew spotlessCheck    # 자동 수정 없이 포맷 검사
./gradlew pmdMain pmdTest  # 설정된 정적 분석 규칙으로 소스·테스트 검사
./gradlew check           # 포맷 검사 + PMD + 전체 테스트
```

Spotless 8.11.0과 Palantir Java Format 2.102.0을 사용하며, PMD 7.10.0 규칙은 `config/pmd/ruleset.xml`에 있다. PMD는 잘못된 null 검사, 리소스 누수, equals/hashCode 불일치, finally 반환, 잘못된 반복문·switch, 불필요한 import를 검사한다. 정적 분석 통과는 런타임 오류가 없다는 보장은 아니다.

기존 `javaFormat`과 `javaFormatCheck` 명령도 각각 `spotlessApply`, `spotlessCheck`의 별칭으로 유지한다. IDE에서도 Spotless와 같은 포맷을 사용해 저장 시 서로 다른 규칙으로 재정렬되지 않게 한다.
