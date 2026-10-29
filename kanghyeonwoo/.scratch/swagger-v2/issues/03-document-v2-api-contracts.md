# 03: V2 전체 API 응답·DTO 명세 및 OpenAPI 계약 검증

**What to build:** 현재 API의 실제 동작에 맞춰 성공·오류 응답과 요청·응답 DTO를 OpenAPI 문서에 완전하게 표현한다.

**Blocked by:** 01: 응답 코드 enum과 공통 응답 계약; 02: 모든 API 경로 V2 전환.

**Status:** implemented; manual Swagger UI scenario remains unverified

- [x] 게시글·댓글·신고·검색·헬스체크 모든 컨트롤러의 `@Tag`, `@Operation`, 파라미터 설명을 보완한다.
- [x] 모든 현재 요청·응답 DTO와 `ApiResponse`, `PageResponse` 필드에 설명·현실적인 예시를 추가한다.
- [x] `@NotBlank`, `@Size`, 범위 검증 및 Enum 허용 값과 OpenAPI 필수·길이·허용 값 스키마를 일치시킨다.
- [x] 엔드포인트마다 실제 가능한 성공·실패 HTTP 상태를 `@ApiResponse(s)`로 선언한다. 오류 본문은 `ErrorResponse`가 아닌 `ApiResponse<Void>`로 표현하고 enum 코드 이름을 예시로 제공한다.
- [x] `@ApiResponse`는 HTTP 응답 명세 어노테이션이며 `ApiResponse<T>` 공통 응답 DTO와 이름이 다른 개념임을 코드에서 분명히 한다.
- [x] 이름 충돌 시 Swagger 응답 어노테이션을 완전한 이름으로 지정하고, 공통 405·500 응답은 재사용 가능한 OpenAPI 컴포넌트로 제공한다.
- [x] 신고 처리는 기존 정책대로 존재하는 사용자 ID를 가진 사용자가 수행할 수 있다고 설명한다. ADMIN 제한을 문서에 추가하지 않는다.
- [x] 문서에 구현되지 않은 댓글 CRUD, 게시글 숨김 API 등을 추가하지 않는다. 게시글 목록은 검색·페이징 API로 설명한다.
- [x] OpenAPI JSON 테스트에서 현행 14개 경로·메서드, 응답 상태, 응답 코드 enum 값, DTO 스키마 및 제약을 검증한다.
- [ ] 브라우저에서 로컬 Swagger UI Try it out으로 대표 성공·검증 오류·중복 신고·신고 처리 흐름을 직접 실행한다. UI 진입 경로는 HTTP 200으로 확인했고, 시나리오는 MockMvc 테스트로 검증했다. PR 본문과 캡처 이미지는 작성하지 않는다.
