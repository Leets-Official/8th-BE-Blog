# 05: [Feature] 게시글 수정 및 작성자 권한 인가 구현 (PATCH /posts/{postId})

**What to build:**
게시글 ID, 작성자ID(authorId), 수정할 제목 및 본문을 전달받아 게시글을 부분 수정하는 API(PATCH /posts/{postId})를 제공한다. 게시글이 없으면 POST4001(404), 요청한 작성자가 원글 작성자와 다르면 POST4003(403 Forbidden) 에러를 반환하며, 일치할 경우 도메인 메서드를 통해 엔티티를 갱신하고 수정된 상세 데이터를 반환한다.

**Blocked by:** 03: [Feature] 게시글 단건 상세 조회 및 404 예외 처리 구현 (GET /posts/{postId})

**Status:** ready-for-agent

- [x] PostUpdateRequest DTO 정의 (authorId 필수, 수정할 title/content 검증)
- [x] PostService.updatePost 구현 (작성자 불일치 시 ErrorCode.FORBIDDEN_POST_UPDATE 예외 발생)
- [x] PostController에 @PatchMapping("/posts/{postId}") 구현
- [x] 정상 수정(200 OK), 권한 없음(403 Forbidden), 미존재 글(404 Not Found) MockMvc 테스트 작성 및 통과 검증
