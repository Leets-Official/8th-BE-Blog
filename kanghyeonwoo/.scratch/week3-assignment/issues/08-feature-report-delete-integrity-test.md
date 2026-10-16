# 08: [Feature] 신고가 달린 게시글 및 댓글 삭제 무결성 엔드투엔드 검증 (MockMvc & Service 통합 테스트)

**What to build:**
게시글 및 그 하위 댓글에 신고(Report)가 실제로 접수되어 있는 상태에서 DELETE /posts/{postId} 호출 시, RDB 외래키 제약조건 위반 예외 없이 게시글과 댓글이 성공적으로 삭제(200 OK)됨을 검증하고, 삭제 이후에도 Report 테이블의 신고 이력과 스냅샷 데이터가 손실 없이 영구 보존되는지 검증하는 MockMvc 통합 테스트를 구현한다.

**Blocked by:** 07: [Refactor] Report 엔티티의 Post/Comment 물리 FK를 논리적 targetId/targetType 참조로 전환

**Status:** ready-for-agent

- [ ] PostControllerTest에 신고가 접수된 게시글 삭제 시 200 OK 성공 및 FK 에러 미발생 검증 테스트 추가
- [ ] 삭제 후 ReportRepository를 통해 신고 내역(targetType=POST, targetId, 스냅샷)이 DB에 온전히 남아있는지 검증
- [ ] 신고가 접수된 댓글을 포함한 게시글 삭제 시 댓글 및 게시글은 삭제되고 댓글 신고 내역은 보존되는지 검증
