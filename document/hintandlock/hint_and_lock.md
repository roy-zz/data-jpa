이번 장에서는 JPA의 Hint와 Lock에 대해서 알아본다.
글의 하단부에 참고한 강의와 공식문서의 경로를 첨부하였으므로 자세한 사항은 강의나 공식문서에서 확인한다.
모든 코드는 [깃허브 (링크)](https://github.com/roy-zz/data-jpa)에 있다.

---

### Hint

DB에 적용되는 Hint가 아닌 JPA 구현체에게 제공하는 힌트이다.
ReadOnly 옵션을 true로 주어 JPA에게 읽기 전용으로 조회할 것이라는 힌트를 주는 방법에 대해서 알아본다.

아래는 일반적으로 이름으로 SoccerPlayer를 조회하는 메서드와 이를 검증하는 테스트 코드이다.

**SoccerPlayerDataRepository**

```java
public interface SoccerPlayerDataRepository extends JpaRepository<SoccerPlayer, Long> {
    SoccerPlayer findUpdatableByName(String name);
}
```

**SoccerPlayerDataRepositoryTest**

```java
@Transactional
@SpringBootTest
class SoccerPlayerDataRepositoryTest {
    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private SoccerPlayerDataRepository dataRepository;
    @Test
    @DisplayName("Hint Updatable 테스트")
    void hintUpdatableTest() {
        SoccerPlayer newPlayer = new SoccerPlayer("Roy");
        dataRepository.save(newPlayer);
        entityManager.flush();
        entityManager.clear();
        SoccerPlayer storedPlayer = dataRepository.findUpdatableByName("Roy");
        storedPlayer.setHeight(183);
        entityManager.flush();
    }
}
```

이렇게 SoccerPlayer를 조회하면 JPA Dirty Checking등을 위해 객체의 버전 관리를 내부적으로 진행해야한다.
JPA의 관리를 받기 때문에 entityManager.flush() 시점에 DB에 업데이트 쿼리가 전달된다.

```bash
Hibernate: 
    select
        soccerplay0_.soccer_player_id as soccer_p1_0_,
        soccerplay0_.height as height2_0_,
        soccerplay0_.name as name3_0_,
        soccerplay0_.team_id as team_id5_0_,
        soccerplay0_.weight as weight4_0_ 
    from
        soccer_player soccerplay0_ 
    where
        soccerplay0_.name=?
Hibernate: 
    update
        soccer_player 
    set
        height=?,
        name=?,
        team_id=?,
        weight=? 
    where
        soccer_player_id=?
```

만약 해당 메서드에서 조회되는 결과는 수정이 필요없는 데이터라면 JPA에게 Dirth Checking을 하지 않도록 유도할 수 있다.
물론 JPA 내부적으로 최적화가 잘 되어있어서 리소스 사용량에 큰 차이가 나지 않더라도 불필요한 작업을 진행하게 되는 것이다.

이번에는 JPA에게 읽기 전용 Entity이므로 수정되더라도 Update 쿼리를 발생시키지 않도록 힌트를 주도록 한다.

**SoccerPlayerDataRepository**

```java
public interface SoccerPlayerDataRepository extends JpaRepository<SoccerPlayer, Long> {
    @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
    SoccerPlayer findReadOnlyByName(String name);
}
```

테스트 코드에서 데이터를 수정하고 flush하더라도 Update 쿼리는 발생하지 않는다.

**SoccerPlayerDataRepositoryTest**

```java
@Transactional
@SpringBootTest
class SoccerPlayerDataRepositoryTest {
    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private SoccerPlayerDataRepository dataRepository;
    @Test
    @DisplayName("Hint ReadOnly 테스트")
    void hintReadOnlyTest() {
        SoccerPlayer newPlayer = new SoccerPlayer("Roy");
        dataRepository.save(newPlayer);
        entityManager.flush();
        entityManager.clear();
        SoccerPlayer storedPlayer = dataRepository.findReadOnlyByName("Roy");
        storedPlayer.setHeight(183);
        entityManager.flush();
    }
}
```

로그를 확인해보면 Update 쿼리가 발생하지 않은 것을 알 수 있다.

```bash
Hibernate: 
    select
        soccerplay0_.soccer_player_id as soccer_p1_0_,
        soccerplay0_.height as height2_0_,
        soccerplay0_.name as name3_0_,
        soccerplay0_.team_id as team_id5_0_,
        soccerplay0_.weight as weight4_0_ 
    from
        soccer_player soccerplay0_ 
    where
        soccerplay0_.name=?
```

---

### Lock

@Lock 어노테이션을 사용하여 Select를 할 때 Lock을 사용할 수 있다. 사용법은 아래와 같다.

```java
public interface SoccerPlayerDataRepository extends JpaRepository<SoccerPlayer, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<SoccerPlayer> findUsingLockByName(String name);
}
```

간단히 조회하는 테스트 코드를 작성하였다.

```java
@Transactional
@SpringBootTest
class SoccerPlayerDataRepositoryTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private SoccerPlayerDataRepository dataRepository;
    @Test
    @DisplayName("Lock 테스트")
    void lockTest() {
        dataRepository.findUsingLockByName("Roy");
    }
}
```

테스트 코드의 결과를 확인해보면 SELECT 쿼리 뒤에 for update가 붙은 것을 확인할 수 있다.

```bash 
Hibernate: 
    select
        soccerplay0_.soccer_player_id as soccer_p1_0_,
        soccerplay0_.height as height2_0_,
        soccerplay0_.name as name3_0_,
        soccerplay0_.team_id as team_id5_0_,
        soccerplay0_.weight as weight4_0_ 
    from
        soccer_player soccerplay0_ 
    where
        soccerplay0_.name=? for update
```

이번 장에서는 @Lock의 사용법까지만 알아보았다.
Lock의 경우 종류가 많기 때문에 추후에 종류별로 자세하게 알아보도록 한다.

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