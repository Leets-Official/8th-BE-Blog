# Java 코드 품질 검사

`./gradlew check`는 Spotless, Checkstyle, PMD와 테스트를 실행한다. Spotless는 포맷과 사용하지 않는 import를 정리한다. Checkstyle은 일반 와일드카드 import와 여러 최상위 타입을 금지하며 static 와일드카드 import는 허용한다. PMD는 설정 파일에 명시된 결함 패턴을 검사한다.

SonarQube 서버에서 프로젝트를 만들고 분석 토큰을 발급한 뒤 다음 명령을 실행한다. 프로젝트 키가 서버 설정과 다르면 `-Dsonar.projectKey=서버의키`를 추가한다.

```sh
export SONAR_HOST_URL='http://localhost:9000'
read -rs 'SONAR_TOKEN?SonarQube 토큰: '
export SONAR_TOKEN
./gradlew sonar
unset SONAR_TOKEN
```

위 `read` 문법은 zsh 기준이다. SonarQube for IDE 확장과 서버 분석은 각각 별도로 실행된다. 서버의 품질 프로필에 따라 검사 규칙이 결정된다.
