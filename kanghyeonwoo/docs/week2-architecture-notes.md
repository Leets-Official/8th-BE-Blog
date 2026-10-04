# 2주차 핵심 키워드 및 아키텍처 의사결정 (Keyword & Architecture Decisions)

이 문서는 2주차 과제(ERD 기반 Spring Data JPA 엔티티 및 연관관계 구현)에서 활용된 **핵심 개념/애노테이션**과 **주요 설계 의사결정(Architecture Decisions)**을 리뷰어 및 팀원들이 한눈에 참고할 수 있도록 정리한 기술 문서입니다.

---

## 🔑 Keyword (주요 개념 및 애노테이션 정리)

### 1. JPA 엔티티 및 스키마 매핑
* `@Entity`: JPA가 관리하는 데이터베이스 매핑 대상 클래스임을 선언합니다.
* `@Table(name = "테이블명")`: 엔티티가 매핑될 데이터베이스 테이블명을 명시합니다. (`users`, `posts`, `comments`, `reports`)
* `@Id`: 테이블의 기본키(Primary Key)를 나타냅니다.
* `@GeneratedValue(strategy = GenerationType.IDENTITY)`: 기본키 생성을 데이터베이스의 `AUTO_INCREMENT`에 위임하여 고유 식별자를 발급합니다.
* `@Lob`: 대용량 본문 텍스트 데이터(`TEXT`)를 저장하기 위해 지정합니다. (`Post.content`, `Comment.content`)

### 2. JPA Auditing & 공통 엔티티
* `@MappedSuperclass`: 공통 매핑 정보가 필요할 때 부모 클래스에 선언하여, 상속받는 자식 엔티티에 컬럼(`created_at`, `updated_at`)만 제공하는 추상 클래스입니다.
* `@EntityListeners(AuditingEntityListener.class)`: 엔티티의 영속/수정 이벤트를 감지하여 생성 및 수정 시간을 자동 추적합니다.
* `@CreatedDate` / `@LastModifiedDate`: 엔티티가 생성되거나 수정될 때 현재 시각을 자동으로 주입합니다.
* `@EnableJpaAuditing`: 스프링 부트 환경에서 JPA Auditing 기능을 활성화하는 설정 애노테이션입니다.

### 3. 연관관계 매핑 및 성능 최적화
* `@ManyToOne`: 다대일(N:1) 단방향/양방향 연관관계를 설정합니다. (외래키가 위치하는 주인이 됩니다)
* `@OneToMany(mappedBy = "...")`: 일대다(1:N) 연관관계를 설정하며, `mappedBy`를 통해 연관관계의 주인이 아님(읽기 전용 참조)을 지정합니다.
* `@JoinColumn(name = "..._id")`: 외래키(FK) 컬럼명을 명시적으로 지정합니다.
* `FetchType.LAZY` (지연 로딩): 연관된 엔티티를 즉시 조회하지 않고, 실제 해당 객체를 참조하는 시점에 쿼리를 날리는 로딩 전략입니다. 불필요한 즉시 로딩(`EAGER`)으로 인한 N+1 쿼리 및 메모리 낭비를 방지하기 위해 모든 다대일 관계에 기본 적용했습니다.
* `cascade = CascadeType.ALL` (영속성 전이): 부모 엔티티(`Post`)의 상태 변화(저장, 삭제 등)를 자식 엔티티(`Comment`)에 그대로 전파합니다.
* `orphanRemoval = true` (고아 객체 제거): 부모 엔티티와의 연관관계가 끊어진 자식 엔티티를 자동으로 데이터베이스에서 삭제합니다.

### 4. 도메인 캡슐화 및 객체 모델링
* `@NoArgsConstructor(access = AccessLevel.PROTECTED)`: 기본 생성자의 접근 권한을 `protected`로 제한하여 무분별한 객체 생성을 막고, JPA 프록시 객체가 정상 동작할 수 있는 최소 가시성을 확보합니다.
* `@Builder`: 복잡한 매개변수를 가진 엔티티 객체를 가독성 있고 안전하게 생성하기 위해 적용했습니다.
* **Self-referencing (자기참조 계층형 구조)**: `Comment`가 자기 자신을 부모(`parent`)로 참조하고 자식들(`children`)을 컬렉션으로 보유하여 N차 대댓글을 유연하게 표현합니다.
* **Soft Delete / Masking (소프트 삭제 및 마스킹)**: 실제 레코드를 DB에서 물리적으로 삭제하지 않고 `isDeleted = true` 플래그 및 `"삭제된 댓글입니다."` 텍스트로 치환하여 하위 대댓글의 맥락과 스레드를 온전히 보존하는 기법입니다.

---

## 💡 주요 아키텍처 및 설계 의사결정 (Key Design Decisions)

### 1. 양방향 연관관계를 선택적으로 적용한 이유 (Selective Bidirectional Mapping)
* **`User ➔ Post / Comment` (단방향 유지)**:
  * 테이블 관점에서는 외래키 하나로 양방향 조회가 가능하지만, 객체 세상에서 `User`가 수많은 `Post`와 `Comment` 컬렉션을 직접 들고 있게 되면 회원이 작성한 데이터가 많아질수록 엄청난 메모리 로딩 부하와 의도치 않은 N+1 쿼리 폭탄이 발생합니다.
  * 사용자의 게시글/댓글 목록 조회는 Repository 쿼리(`findByUserId`)와 페이징 처리를 통해 필요한 시점에만 조회하는 것이 훨씬 안전하고 효율적이므로, 도메인 간 결합도를 낮추기 위해 단방향을 유지했습니다.
* **`Post ➔ Comment` (양방향 적용)**:
  * 반면 게시글은 상세 페이지 조회 시 댓글 묶음을 함께 탐색하는 비즈니스 유스케이스가 매우 빈번합니다.
  * 또한 댓글은 게시글 없이는 존재할 수 없는 완전 종속 엔티티이므로, `cascade = CascadeType.ALL, orphanRemoval = true`를 통해 게시글의 생명주기에 댓글을 종속시켜 고아 데이터를 방지하기 위해 선택적으로 양방향을 구성했습니다.

### 2. 모든 `@ManyToOne` 관계에 `FetchType.LAZY`를 강제한 이유
* JPA 스펙상 `@ManyToOne`의 기본 로딩 전략은 즉시 로딩(`FetchType.EAGER`)입니다.
* EAGER는 하나의 엔티티를 조회할 때 연관된 엔티티까지 무조건 조인하여 가져오므로, JPQL 실행 시 예측하기 어려운 N+1 쿼리를 발생시켜 성능에 치명적인 영향을 줍니다.
* 따라서 실무 표준에 맞춰 모든 다대일 관계를 지연 로딩(`LAZY`)으로 강제하고, 실제 참조 객체가 필요할 때 프록시를 통해 로딩되도록 설계했습니다.

### 3. 대댓글 삭제 시 물리 삭제가 아닌 소프트 마스킹("삭제된 댓글입니다.")을 채택한 이유
* 부모 댓글이 물리적으로 하드 삭제(`DELETE` 쿼리)되면, 계층형 자기참조 외래키 제약조건에 의해 에러가 발생하거나 자식 대댓글까지 연쇄 삭제되어 전체 토론 맥락(스레드)이 끊기게 됩니다.
* 대댓글 작성자의 권리와 대화의 흐름을 보존하기 위해, `Comment.delete()` 호출 시 `isDeleted = true` 플래그와 함께 본문을 `"삭제된 댓글입니다."`로 마스킹 처리하여 하위 대댓글 트리를 온전히 유지하도록 설계했습니다.

### 4. 신고(`Report`) 도메인에서 다형성 ID 매핑 대신 개별 Nullable 외래키를 선택한 이유
* `targetType`(POST/COMMENT)과 `targetId` 숫자만 저장하는 다형성 매핑 방식은 테이블 구조는 단순해 보이지만, DB 레벨의 외래키(FK) 제약조건을 걸 수 없어 대상 글이 삭제되었을 때 고아 신고 데이터가 남는 등 데이터 무결성이 취약해집니다.
* 반면 `post`와 `comment`를 각각 Nullable 외래키로 두면, RDB 외래키 무결성을 완벽히 보장하면서 JPA에서 `report.getPost()`, `report.getComment()`처럼 객체 지향적인 그래프 탐색을 타입 안전하게 수행할 수 있어 이 방식을 채택했습니다.

### 5. 무분별한 Setter를 배제하고 도메인 비즈니스 메서드를 제공한 이유
* 무분별한 public `@Setter`는 객체의 상태 변경 의도(단순 수정인지, 상태 전이인지)를 파악하기 어렵게 만들며, 불완전한 상태의 객체가 생성되거나 수정될 위험이 큽니다.
* 기본 생성자 접근 권한을 `protected`로 제한하고, 상태 변경이 필요한 경우 `post.update(...)`, `comment.delete()`, `post.addComment(...)` 등 명확한 도메인 언어를 담은 메서드를 통해서만 변경되도록 캡슐화했습니다.
