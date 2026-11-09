# 02: 모든 API 경로를 V2로 전환

**What to build:** 현행 14개 엔드포인트를 `/api/v2`로 옮기고 OpenAPI 문서의 버전을 API 경로와 맞춘다.

**Blocked by:** 01: 의미 중심 응답 코드 enum과 공통 응답 계약.

**Status:** implemented

- [x] 게시글·댓글·신고·검색·헬스체크 컨트롤러의 경로 prefix를 `/api/v2`로 바꾼다.
- [x] `/api/v1` 별칭이나 호환 라우트를 제공하지 않는다.
- [x] `ApiPaths`에 V2 prefix 상수를 정의하고 모든 컨트롤러가 공통 상수를 사용한다.
- [x] OpenAPI `info.version`을 `2.0.0`으로 갱신하고 Swagger UI `/swagger-ui.html`, OpenAPI JSON `/v3/api-docs` 경로는 유지한다.
- [x] 각 엔드포인트의 기존 요청 필드, 응답 data 필드, 도메인 동작을 유지한다. 이 티켓에서 새 API 동작을 추가하지 않는다.
- [x] MVC 테스트로 현재 14개 경로의 V2 동작과 V1 경로의 미노출을 검증한다.
