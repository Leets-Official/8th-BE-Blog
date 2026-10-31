# 게시글 API 실행 안내

Java 21을 사용합니다. 기본 DB는 `localhost:3306`의 MySQL이며,
계정은 `root`, DB명은 `leets`입니다. `DB_PASSWORD` 환경변수에 비밀번호를 설정한 후
`./gradlew.bat bootRun`으로 실행합니다.
Swagger UI: `http://localhost:8080/swagger-ui/index.html`.

| Method | URL | 설명 |
| --- | --- | --- |
| GET | `/posts/{postId}` | 게시글 조회 |
| GET | `/posts?page=0&size=10` | 최신 게시글부터 목록 조회 |
| POST | `/posts` | 게시글 생성 |
| PATCH | `/posts/{postId}` | 제목과 내용 수정 |
| DELETE | `/posts/{postId}` | 게시글 및 연결된 댓글 삭제 |

현재 모든 게시글 API는 로그인과 권한 검사 없이 사용할 수 있습니다.
누구나 기존 게시글도 수정·삭제할 수 있습니다. POST/PATCH 요청에는 `title`, `content`가 모두
필요합니다. 제목은 공백을 제외한 값이며 최대 255자, 내용은 공백을 제외한 값이어야 합니다.
`page`의 음수는 0으로, `size`는 1~100 범위로 보정합니다.

## MySQL 연결

다음 환경변수를 설정한 뒤 실행합니다. `leets` DB가 없으면 자동 생성합니다.
DB 비밀번호는 저장소에 커밋하지 마세요.

```powershell
$env:DB_PASSWORD = '비밀번호'
./gradlew.bat bootRun
```

IntelliJ에서는 Run/Debug Configurations의 Environment variables에
`DB_PASSWORD=실제비밀번호`를 추가하면 됩니다.
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

## 로그인 및 권한 검사 비활성화

JWT 검증, 회원 인증, 작성자 권한 검사와 관련 설정·의존성·기존 인증 테스트는
주석으로 보존했습니다. `Authorization` 헤더와 `JWT_SECRET`은 필요하지 않으며 사용하지 않습니다.

회원 FK가 필수인 기존 엔티티와 DB 구조를 유지하기 위해 새 게시글은 공용 `익명` 회원에 연결합니다.
이 회원은 첫 게시글 생성 시 자동으로 생성하고 이후 재사용합니다.
기존 게시글의 작성자는 유지합니다.

생성 요청 예시:

```json
{
  "title": "게시글제목",
  "content": "게시글내용"
}
```

응답은 `isSuccess`, `code`, `message`, `data` 구조입니다.
입력 오류는 HTTP 400 / `COMMON_400_1`, 없는 게시글은 HTTP 404 / `POST_404_1`입니다.

## 검증

`./gradlew.bat test`는 H2 메모리 DB를 사용하여 인증 없는 게시글 생성·수정·삭제,
익명 회원 자동 생성 및 재사용, 입력 검증, 페이징, 댓글이 달린 게시글 삭제를 검증합니다.
실제 MySQL 접속은 별도 접속 정보로 실행하여 확인해야 합니다.
