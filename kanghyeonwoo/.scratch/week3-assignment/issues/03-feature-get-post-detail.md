# 03: [Feature] 게시글 단건 상세 조회 및 404 예외 처리 구현 (GET /posts/{postId})

**What to build:**
게시글 ID를 경로 변수로 받아 해당 게시글의 상세 정보(제목, 본문, 작성자ID, 생성일시, 수정일시)를 조회하는 API(GET /posts/{postId})를 제공한다. 대상 게시글이 DB에 없거나 삭제된 경우 POST4001(404 Not Found) 표준 에러를 반환한다.

**Blocked by:** 02: [Feature] 게시글 등록 API 및 입력값 검증 구현 (POST /posts)

**Status:** ready-for-agent

- [ ] PostDetailResponse DTO 정의 (id, title, content, authorId, createdAt, updatedAt)
- [ ] PostService.getPostById 구현 (없을 경우 CustomException(ErrorCode.POST_NOT_FOUND) 발생)
- [ ] PostController에 @GetMapping("/posts/{postId}") 구현
- [ ] 정상 조회(200 OK) 및 404 Not Found 예외 응답 MockMvc 테스트 작성 및 통과 검증
