# 게시글 API 실행 안내

Java 21을 사용합니다. 기본 DB는 `localhost:3306`의 MySQL이며,
계정은 `root`, DB명은 `leets`입니다. `DB_PASSWORD`와 `JWT_SECRET` 환경변수를 설정한 후
`./gradlew.bat bootRun`으로 실행합니다.
Swagger UI: `http://localhost:8080/swagger-ui/index.html`.

| Method | URL | 설명 |
| --- | --- | --- |
| GET | `/posts/{postId}` | 게시글 조회 |
| GET | `/posts?page=0&size=10` | 최신 게시글부터 목록 조회 |
| POST | `/posts` | 게시글 생성 |
| PATCH | `/posts/{postId}` | 제목과 내용 수정 |
| DELETE | `/posts/{postId}` | 게시글 및 연결된 댓글 삭제 |

게시글 조회·목록은 인증 없이 사용할 수 있습니다. 생성·수정·삭제는 로그인이 필요하며,
수정·삭제는 작성자만 가능합니다. POST/PATCH 요청에는 `title`, `content`가 모두
필요합니다. 제목은 공백을 제외한 값이며 최대 255자, 내용은 공백을 제외한 값이어야 합니다.
`page`의 음수는 0으로, `size`는 1~100 범위로 보정합니다.

## MySQL 연결

다음 환경변수를 설정한 뒤 실행합니다. `leets` DB가 없으면 자동 생성합니다.
DB 비밀번호는 저장소에 커밋하지 마세요.

```powershell
$env:DB_PASSWORD = '비밀번호'
$env:JWT_SECRET = '별도로 생성해 보관한 32바이트 이상의 무작위 비밀키'
./gradlew.bat bootRun
```

IntelliJ에서는 Run/Debug Configurations의 Environment variables에
`DB_PASSWORD=실제비밀번호`와 `JWT_SECRET=생성한비밀키`를 추가하면 됩니다.
접속 주소나 계정을 변경할 경우 `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`으로 덮어쓸 수 있습니다.
`.env` 파일은 자동으로 읽지 않으므로 실행 환경에 환경변수를 설정해야 합니다.
H2를 사용할 때는 `SPRING_PROFILES_ACTIVE=h2`로 설정합니다.
H2 데이터는 `data/blog` 파일에 저장합니다.

엔티티에 맞춘 수동 생성 SQL은 [schema.sql](schema.sql)에 있습니다.
MySQL Workbench에서 파일을 열어 실행하거나 MySQL 클라이언트에서
`SOURCE C:/Users/CHOYEJI/Project/Leets/8th-BE-Blog/docs/schema.sql;`로 실행할 수 있습니다.
`created_at`은 생성 시 JPA에서 설정하며, `updated_at`은 최초 생성 시 null이고 수정 시 설정됩니다.
이 SQL은 없는 DB와 테이블을 생성하며, 이미 존재하는 테이블의 구조는 변경하지 않습니다.

`DDL_AUTO` 기본값은 `update`입니다. Hibernate가 기존 엔티티에 맞춰 테이블을 생성하거나
변경하므로 DB 계정에 해당 권한이 필요합니다. 기존 스키마를 검증만 하려면
`DDL_AUTO=validate`를 사용하세요.

## 로그인 및 권한 검사

회원가입 후 로그인해서 받은 `accessToken`을 사용합니다. 생성·수정·삭제 요청에
`Authorization: Bearer <accessToken>` 헤더를 넣어야 합니다.

Swagger에서는 상단 **Authorize** 버튼에 `Bearer ` 없이 토큰 값만 입력하면 됩니다.
자세한 설정·사용 방법은 [login.md](login.md)에 있습니다.
새 게시글은 로그인한 회원에 연결되며, 기존 게시글 작성자는 변경하지 않습니다.
과거 공용 익명 회원으로 생성한 글은 일반 로그인 회원이 수정·삭제할 수 없습니다.

생성 요청 예시:

```json
{
  "title": "게시글제목",
  "content": "게시글내용"
}
```

응답은 `isSuccess`, `code`, `message`, `data` 구조입니다.
입력 오류는 HTTP 400 / `COMMON_400_1`, 없는 게시글은 HTTP 404 / `POST_404_1`입니다.
토큰 누락·만료·위조 또는 존재하지 않는 회원의 토큰은 HTTP 401 / `AUTH_401_1`,
다른 작성자의 글 수정·삭제는 HTTP 403 / `POST_403_1`입니다.

## 검증

`./gradlew.bat test`는 H2 메모리 DB를 사용하여 로그인 토큰으로 게시글 생성·수정·삭제,
인증·작성자 권한 검사, 공개 조회, 입력 검증, 페이징, 댓글이 달린 게시글 삭제,
Swagger 인증 스키마를 검증합니다.
실제 MySQL 접속은 별도 접속 정보로 실행하여 확인해야 합니다.
