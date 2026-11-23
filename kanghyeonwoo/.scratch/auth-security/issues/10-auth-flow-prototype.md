# 10: 인증 흐름 프로토타입으로 정책 검토

**What to build:** 토큰 회전·기기별 로그아웃·소유권·카카오 가입 정책을 버튼으로 조작하는 독립 HTML.

**Blocked by:** None

**Status:** awaiting-user-feedback

- [x] 메모리 상태와 직접 조작 버튼 제공
- [x] 5가지 시나리오별 단계 버튼 제공
- [x] 별도 브랜치에 프로토타입 보존
- [ ] 사용자 체험 후 설계 질문의 결론 기록

## Context

질문: 현재 인증 정책을 사용자가 예상한 상태 전이로 설명할 수 있는가?

브랜치: `prototype/6주차-auth-flow`, 커밋: `ae63b29`.
HTML: `kanghyeonwoo/src/main/java/com/leets_8th_be/kanghyeonwoo/auth/auth-flow.prototype.html`.
로컬 작업 트리: `/private/tmp/blog-auth-prototype`.

현재 결론은 미확정이며 실제 서버·카카오·암호화에 연결하지 않는다. 구현 브랜치에는 HTML을 포함하지 않는다. 사용자 체험 후 검증된 결정만 구현에 반영한다.
