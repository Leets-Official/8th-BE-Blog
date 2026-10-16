# 07: [Refactor] Report 엔티티의 Post/Comment 물리 FK를 논리적 targetId/targetType 참조로 전환

**What to build:**
게시글(Post) 또는 댓글(Comment) 삭제 시 물리적 외래키(FK) 제약조건으로 인해 삭제가 실패하는 문제를 방지하고 신고 이력 데이터를 안전하게 영구 보존하기 위해, Report 엔티티에서 Post/Comment의 @ManyToOne 물리 FK를 제거하고 targetType(POST, COMMENT), targetId(Long), targetTitleSnapshot(String), targetContentSnapshot(String) 논리 참조 구조로 리팩토링한다.

**Blocked by:** 06: [Feature] 게시글 삭제 및 작성자 권한 인가 구현 (DELETE /posts/{postId})

**Status:** ready-for-agent

- [x] ReportTargetType enum 정의 (POST, COMMENT)
- [x] Report 엔티티 필드 수정: targetType, targetId, targetTitleSnapshot, targetContentSnapshot 추가 및 post, comment FK 제거
- [x] Report.ofPost, Report.ofComment 팩토리 메서드가 대상 엔티티에서 id 및 스냅샷을 자동 추출하도록 갱신
- [x] ReportRepository 조회 메서드 갱신 (findByTargetTypeAndTargetId 등)
- [x] ReportTest 단위 테스트 갱신 및 통과 검증
