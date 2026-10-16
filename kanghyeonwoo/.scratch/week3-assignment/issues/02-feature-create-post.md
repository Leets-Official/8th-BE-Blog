# 02: [Feature] 게시글 등록 API 및 입력값 검증 구현 (POST /posts)

**What to build:**
클라이언트가 제목, 본문, 작성자ID(authorId)를 전달하여 신규 게시글을 등록하는 API(POST /posts)를 제공한다. 필수값 누락이나 공백인 경우 Bean Validation을 통해 400 에러(COMMON400_VALIDATION)를 반환하고, 성공 시 201 Created와 함께 생성된 게시글 상세 데이터를 ApiResponse에 담아 반환한다.

**Blocked by:** 01: [Prefactor] 표준 ApiResponse 문자열 코드 확장 및 도메인 ErrorCode/글로벌 예외 처리 기반 구축

**Status:** ready-for-agent

- [ ] PostCreateRequest DTO 정의 (title, content, authorId 필수값 검증)
- [ ] PostResponse DTO 정의 (id, title, content, authorId, createdAt)
- [ ] PostService.createPost 구현 (User 조회 및 Post 영속화)
- [ ] PostController에 @PostMapping("/posts") 구현 및 201 Created 응답 반환
- [ ] MockMvc 컨트롤러 슬라이스 및 통합 테스트 작성 및 통과 검증
