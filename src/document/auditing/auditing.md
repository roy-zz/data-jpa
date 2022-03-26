이번 장에서는 JPA의 Auditing에 대해서 알아본다.
글의 하단부에 참고한 강의와 공식문서의 경로를 첨부하였으므로 자세한 사항은 강의나 공식문서에서 확인한다.
모든 코드는 [깃허브 (링크)](https://github.com/roy-zz/data-jpa)에 있다.

---

대부분의 테이블에는 생성일시, 수정일시, 생성자, 수정자의 정보가 들어간다.
JPA를 사용하면 이러한 데이터를 개발자가 직접 수동으로 입력하지 않아도 입력되도록 할 수 있다.

---

### 순수 JPA의 Auditing

테이블과 엔티티의 공통되는 데이터인 생성일시와 수정일시를 필드로 가지는 클래스를 생성한다.
필자가 생성한 PureBaseEntity의 경우 추상 클래스로 만들지 않아도 상관없지만 추상 클래스로 만드는 경우
직관적으로 추상화된 클래스임을 알 수 있고 독립적으로 생성되는 것을 방지하기 위해 추상 클래스로 생성하였다.

**PureBaseEntity**

```java
@Getter
@MappedSuperclass
public abstract class PureBaseEntity {

    @Column(updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

}
```

@PrePersist: entity가 persist되기 직전에 호출된다.
@PostPersist: entity가 persist된 직후 호출된다.
@PreUpdate: entity가 update되기 직전에 호출된다.
@PostUpdate: entity가 update된 직후 호출된다.

실제로 생성일시와 수정일시가 적용되었는지 테스트 코드로 확인해 본다.

**SoccerPlayerPureRepositoryTest**

```java
@Transactional
@SpringBootTest
class SoccerPlayerPureRepositoryTest {
    @Autowired
    private SoccerPlayerPureRepository pureRepository;
    @Test
    @DisplayName("Auditing 테스트")
    void auditingTest() {
        SoccerPlayer soccerPlayer = new SoccerPlayer("Roy", 173);
        pureRepository.save(soccerPlayer);
        soccerPlayer.setHeight(183);
        pureRepository.flushAndClear();
        
        SoccerPlayer storedPlayer = pureRepository.findByName("Roy");
        System.out.println("storedPlayer.getCreatedAt() = " + storedPlayer.getCreatedAt());
        System.out.println("storedPlayer.getUpdatedAt() = " + storedPlayer.getUpdatedAt());
    }
}
```

결과는 아래와 같으며 생성일시와 수정일시가 적용된 것을 확인 할 수 있다.

```bash
storedPlayer.getCreatedAt() = 2022-03-26T19:51:43.019463
storedPlayer.getUpdatedAt() = 2022-03-26T19:51:43.055024
```

---

### Data JPA의 Auditing

**메인 메서드가 있는 클래스에 @EnableJpaAuditing 어노테이션 사용**

```java
@EnableJpaAuditing
@SpringBootApplication
public class DataJpaApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataJpaApplication.class, args);
    }

}
```

**시간 관련 값을 가지고 있는 공통 관심 클래스 생성**

```java
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class DataDateBaseEntity {

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

}
```

**작성자 관련 값을 가지고 있는 공통 관심 클래스 생성**

어떤 테이블에는 시간 및 작성자 정보가 모두 필요하고 어떤 테이블에는 시간 관련 정보만 필요할 수 있다.
필요에 따라 선택적으로 상속받을 수 있도록 추상 클래스 두 개로 나누어 작성하였다.

```java
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class DataBaseEntity extends DataDateBaseEntity {

    @CreatedBy
    @Column(updatable = false)
    private String createdBy;

    @LastModifiedBy
    private String updatedBy;

}
```

**Entity 클래스 상속**

```java
@Entity
@NamedQueries(value = {
        @NamedQuery(
                name = "SoccerPlayer.findByName",
                query = "SELECT SP FROM SoccerPlayer SP WHERE SP.name = :name"),
        @NamedQuery(
                name = "SoccerPlayer.findByHeightGreaterThan",
                query = "SELECT SP FROM SoccerPlayer SP WHERE SP.height > :height")
})
@Getter @Setter
@EqualsAndHashCode(callSuper = true)
@ToString(of = {"id", "name", "height", "weight"})
@NoArgsConstructor(access = PROTECTED)
public class SoccerPlayer extends DataBaseEntity {

    @Id
    @GeneratedValue
    @Column(name = "soccer_player_id")
    private Long id;
    private String name;
    // 이하 생략
}
```

**AuditorProvider 설정 클래스 생성**

@Bean으로 등록되어 생성자와 수정자의 정보를 입력하는 역할을 한다.
일반적으로 Spring Security에서 API를 호출한 사용자의 정보를 return 하도록 구현해야한다.

이번 프로젝트에는 Spring Security가 적용되어 있지 않으므로 추후 적용되어 있는 프로젝트에서 테스트를 진행하고 내용을 보충하도록 하겠다.

```java
@Configuration
public class AuditorProvider implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        return Optional.of("생성 및 수정자");
    }

}
```

**테스트 코드로 정상작동 확인**

```java
@Transactional
@SpringBootTest
class SoccerPlayerDataRepositoryTest {

    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private SoccerPlayerDataRepository dataRepository;
    
    @Test
    @DisplayName("Auditing 테스트")
    void auditingTest() {
        SoccerPlayer soccerPlayer = new SoccerPlayer("Roy", 173);
        dataRepository.save(soccerPlayer);
        soccerPlayer.setHeight(183);
        entityManager.flush();
        entityManager.clear();

        SoccerPlayer storedPlayer = dataRepository.findOneByName("Roy");
        System.out.println("storedPlayer.getCreatedAt() = " + storedPlayer.getCreatedAt());
        System.out.println("storedPlayer.getUpdatedAt() = " + storedPlayer.getUpdatedAt());
        System.out.println("storedPlayer.getCreatedBy() = " + storedPlayer.getCreatedBy());
        System.out.println("storedPlayer.getUpdatedBy() = " + storedPlayer.getUpdatedBy());
    }
}
```

결과를 확인해보면 정상적으로 데이터가 출력되는 것을 알 수 있다.

```bash
storedPlayer.getCreatedAt() = 2022-03-26T20:04:42.811693
storedPlayer.getUpdatedAt() = 2022-03-26T20:04:42.846116
storedPlayer.getCreatedBy() = 생성 및 수정자
storedPlayer.getUpdatedBy() = 생성 및 수정자
```

---

**번외**

우리가 구현한 코드를 확인해보면 생성되는 시점에 updatedAt 필드에 생성 일시가 적용되는 것을 확인할 수 있다.
하지만 DBA에 요청에 의해 불필요하다고 판단이 되어 넣지 않고 싶은 경우가 있을 수 있다.

그러한 경우 @EnableJpaAuditing(modifyOnCreate = false)로 설정해두면 생성 시점에 updatedAt의 값은 null이 되고
추후 update가 되는 시점에 updatedAt에 수정 일시가 추가된다.

modifyOnCreate = false 적용 후에 아래의 테스트 코드처럼 생성 시에는 updatedAt은 null 임을 검증하고
업데이트 후에는 updatedAt이 null이 아님을 검증하는 테스트 코드를 실행하면 정상적으로 통과하는 것을 알 수 있다.

```java
@Transactional
@SpringBootTest
class SoccerPlayerDataRepositoryTest {

    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private SoccerPlayerDataRepository dataRepository;
    
    @Test
    @DisplayName("modifyOnCreate = false 정상 작동 테스트")
    void modifyOnCreateFalseTest() {
        SoccerPlayer soccerPlayer = new SoccerPlayer("Roy", 173);
        dataRepository.save(soccerPlayer);
        entityManager.flush();
        entityManager.clear();

        SoccerPlayer storedPlayer = dataRepository.findOneByName("Roy");
        assertNull(storedPlayer.getUpdatedAt());

        storedPlayer.setHeight(183);
        entityManager.flush();
        entityManager.clear();

        SoccerPlayer updatedPlayer = dataRepository.findOneByName("Roy");
        assertNotNull(updatedPlayer.getUpdatedAt());
    }

}
```

---

참고한 강의:

- https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81-%EB%8D%B0%EC%9D%B4%ED%84%B0-JPA-%EC%8B%A4%EC%A0%84
- https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81%EB%B6%80%ED%8A%B8-JPA-API%EA%B0%9C%EB%B0%9C-%EC%84%B1%EB%8A%A5%EC%B5%9C%EC%A0%81%ED%99%94
- https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81%EB%B6%80%ED%8A%B8-JPA-%ED%99%9C%EC%9A%A9-1
- https://www.inflearn.com/course/ORM-JPA-Basic

JPA 공식 문서:

- https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#reference

위키백과:

- https://ko.wikipedia.org/wiki/%EC%9E%90%EB%B0%94_%ED%8D%BC%EC%8B%9C%EC%8A%A4%ED%84%B4%EC%8A%A4_API