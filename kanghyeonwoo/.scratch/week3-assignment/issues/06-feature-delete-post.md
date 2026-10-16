# 06: [Feature] 게시글 삭제 및 작성자 권한 인가 구현 (DELETE /posts/{postId})

**What to build:**
게시글 ID와 작성자ID(authorId 쿼리 파라미터)를 전달받아 게시글을 삭제하는 API(DELETE /posts/{postId}?authorId=...)를 제공한다. 게시글이 없으면 POST4001(404), 작성자가 다르면 POST4003(403)을 반환하며, 일치할 경우 게시글을 삭제하고 삭제된 게시글 ID를 응답 데이터로 반환한다. 연관된 댓글들은 CascadeType.ALL에 의해 함께 삭제된다.

**Blocked by:** 03: [Feature] 게시글 단건 상세 조회 및 404 예외 처리 구현 (GET /posts/{postId})

**Status:** ready-for-agent

- [x] PostDeleteResponse DTO 정의 (deletedPostId)
- [x] PostService.deletePost 구현 (작성자 불일치 검증 및 삭제)
- [x] PostController에 @DeleteMapping("/posts/{postId}") 구현
- [x] 정상 삭제(200 OK), 권한 없음(403), 미존재 글(404) MockMvc 테스트 작성 및 통과 검증
