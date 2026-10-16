# 04: [Feature] 게시글 목록 조회 및 페이징/검색 API 구현 (GET /posts)

**What to build:**
게시글 목록을 최신순으로 페이징 조회하고, 선택적 키워드 검색을 지원하는 API(GET /posts?page=1&size=10&keyword=...)를 제공한다. 응답은 게시글 카드용 요약 정보 배열을 담은 ApiResponse(S200)로 반환한다.

**Blocked by:** 02: [Feature] 게시글 등록 API 및 입력값 검증 구현 (POST /posts)

**Status:** ready-for-agent

- [x] PostSummaryResponse DTO 정의 (id, title, authorId, createdAt)
- [x] PostRepository에 페이징 및 키워드(title 또는 content) 검색 쿼리 메서드 정의
- [x] PostService.getPosts(pageable, keyword) 구현 (Spring Data의 Page index 0-based를 고려하여 요청 page 1-based 매핑)
- [x] PostController에 @GetMapping("/posts") 구현
- [x] 페이징 및 검색 조건별 MockMvc 테스트 작성 및 통과 검증
