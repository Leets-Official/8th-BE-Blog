# Auth 헥사고날 리팩터링 리뷰

기준 커밋: a6c98ad. 구현 커밋: e7b24c5, 39c73a3, 77048a9, dca98fd, e20a8d0, 0529c5f.

## Standards

문서화된 규칙 위반 0건, 별도 수정이 필요한 smell 0건.
코어는 Java 및 자체 domain/application 계약에만 의존한다. 웹·Spring·JPA·JJWT·user 연결은 어댑터에 있다. 입력 어댑터는 입력 포트를 호출하고 config가 구체 구현을 조립한다. 업무 실패의 HTTP 매핑과 트랜잭션 정책도 코어에서 분리했다.

UserAccountAdapter는 user의 공개 포트와 불변 계약을 사용한다. RefreshTokenReused에 한정한 커밋 예외와 카카오 콜백의 state 소비 정책은 운영 문서 및 ADR-0003과 일치한다. ArchUnit은 코어의 허용 의존성과 입력 어댑터의 직접 출력 포트 접근 금지를 검사한다. 트랜잭션 장식자의 위임은 별도 트랜잭션 책임이 있어 불필요한 Middle Man으로 판단하지 않았다.

## Spec

지적 0건.
JWT issuer·클레임·서명·수명·검증, 계정 중복 메시지·가입 rollback, Refresh 재사용 폐기 커밋과 행 잠금, 기기별 로그아웃·잔여 Access 정책을 유지했다. 카카오 state·브라우저 연결·목적·만료·일회성, 닉네임 가입 실패 시 티켓 복구도 유지했다. 명시적 JPA 엔티티 이름·테이블·컬럼·OAuth 목적 문자열은 기존 행과 JPQL의 호환성을 보존한다.

## 최종 검증

`./gradlew clean check --console=plain`: 성공.
테스트 130개, 실패 0, 오류 0, 건너뜀 0.

- 격리 테스트는 가입·로그인·재사용 폐기·카카오 일회용 교환의 입력 포트를 검증한다.
- MockMvc는 기존 API·동시 재발급·기기별 로그아웃·카카오·Swagger 계약을 검증한다.
- 외부 REST 계약, ArchUnit, Spotless, PMD main/test를 통과했다.
- 실제 카카오 계정 로그인은 앱 설정 부재로 미검증이다. 외부 설정 준비 후 확인해야 한다.

구조 및 트랜잭션 정책은 docs/auth-architecture.md에 기록했다.
