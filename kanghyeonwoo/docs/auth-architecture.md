# Auth 헥사고날 경계

코어는 domain과 application에 있으며 Java 표준 라이브러리와 auth 내부 계약에만 의존한다. 웹 응답 코드, Spring, JPA, JJWT, 다른 모듈의 구현을 참조하지 않는다.

```text
auth/
  domain/
    model/          로그인 상태·OAuth 티켓·토큰 종류·지문
    exception/      업무 실패 이유·Refresh 재사용 예외
  application/
    port/in/        가입·로그인·인증·재발급·로그아웃·카카오 유스케이스
    port/out/       계정·암호화·JWT·상태 저장·카카오 신원·난수 생성
    model/          불변 입출력 모델
    usecase/        프레임워크 없는 흐름 조정
  adapter/
    in/web/         요청 검증·응답 DTO·HTTP 오류 매핑·카카오 리다이렉트
    in/security/    Bearer 추출·입력 포트 호출·SecurityContext·오류 응답
    out/user/       user 모듈 공개 계약 연결
    out/crypto/     BCrypt·암호학적 난수
    out/jwt/        JJWT 서명·검증
    out/persistence/ JPA 저장·행 잠금·도메인 변환
    out/kakao/      실제 REST 토큰 교환·사용자 조회
    transaction/    유스케이스 장식 및 트랜잭션 정책
  config/           빈 조립·SecurityFilterChain
```

컨트롤러와 필터는 입력 포트만 호출한다. 구체 유스케이스와 어댑터를 연결하는 책임은 config에 있다. 출력 어댑터는 코어가 정의한 포트를 구현하며 user 데이터는 user의 공개 계약을 통해 접근한다.

## 실패와 트랜잭션

AuthenticationFailure는 업무 이유와 메시지만 갖는다. HTTP 상태와 ApiResponseCode 매핑은 웹 어댑터에 있다. JWT 검증 실패도 업무 실패로 변환하므로 코어는 JJWT 예외를 알 필요가 없다.

Refresh 재사용은 로그인 상태를 폐기한 뒤 RefreshTokenReused를 발생시킨다. 트랜잭션 어댑터는 이 예외만 rollback에서 제외하여 거절 응답에서도 폐기를 커밋한다. 일반 재발급·가입 실패는 rollback한다. 카카오 콜백의 업무 실패는 기존 정책대로 소비한 state 삭제를 커밋한다. 카카오 닉네임 가입 실패는 rollback하여 유효한 가입 티켓으로 재시도할 수 있다.

## 호환성과 검사

기존 HTTP 경로·JSON 필드·메시지·토큰 정책을 유지한다. JPA 테이블·컬럼과 JPQL 엔티티 이름을 유지하여 기존 로그인 상태·OAuth 티켓을 읽는다. JWT issuer, type/sid/jti 클레임, 서명 방식과 환경변수도 유지한다. 고정 JWT_SECRET 배포는 기존 토큰을 검증할 수 있다. 키 미설정의 프로세스 임시 키는 기존과 같이 재시작 때 바뀐다.

AuthBoundaryTest와 HexagonalBoundaryTest가 코어의 의존 방향을 검사한다. 입력 어댑터의 출력 포트·구체 서비스 직접 의존도 금지한다. 유스케이스 단위 테스트, MockMvc의 RTR 동시성·기기별 로그아웃·카카오 브라우저 연결 검사와 외부 REST 계약 검사로 동작을 확인한다.

실계정 카카오 로그인은 앱 키·콜백 설정 후 확인해야 한다. 설정 방법은 [운영 안내](auth-security.md)를 따른다.
