이번 장에서는 사용자 정의 리포지토리에 대해서 알아본다.
글의 하단부에 참고한 강의와 공식문서의 경로를 첨부하였으므로 자세한 사항은 강의나 공식문서에서 확인한다.
모든 코드는 [깃허브 (링크)](https://github.com/roy-zz/data-jpa)에 있다.

---

Spring Data JPA 리포지토리는 인터페이스만 정의하고 구현체는 Spring이 생성해준다.
만약 우리가 인터페이스를 구현하려 한다면 구현해야 하는 기능이 많다.
예를 들어 findAll()과 같이 Data JPA에서 기본으로 제공하는 메소드 또한 직접 구현을 해주어야한다.

만약 여러가지 이유 때문에 인터페이스의 일부분만 직접 구현하고 싶은 경우에 어떻게 해야하는지 알아보도록 한다.
다양한 이유가 있겠지만 필자가 참고한 강의에서 예를 들은 것과 필자의 경험에 의하면 아래 정도의 예시가 있을 듯하다.

- EntityManager를 직접 사용하고 싶은 경우
- 스프링 JDBC Template를 직접 사용하는 경우
- MyBatis로 구축되어 있는 서비스를 점진적으로 JPA로 변경해나가는 경우
- 작업 시간이 오래 걸리는 쿼리를 위해 기본적으로 사용되는 Connection Pool이 아니라 특정 Connection Pool에서 꺼내와서 사용하는 경우
- Querydsl을 사용하는 경우

---

사용법은 아래와 같다.

사용자 정의 리포지토리를 위한 사용자 정의 인터페이스를 정의한다.

**SoccerPlayerDataRepositoryCustom**

```java
public interface SoccerPlayerDataRepositoryCustom {
    List<SoccerPlayer> findCustomByName(String name);
}
```

사용자 정의 인터페이스의 구현체를 생성한다.

**SoccerPlayerDataRepositoryCustomImpl**

```java
@RequiredArgsConstructor
public class SoccerPlayerDataRepositoryCustomImpl implements SoccerPlayerDataRepositoryCustom {

    private final EntityManager entityManager;

    @Override
    public List<SoccerPlayer> findCustomByName(String name) {
        return entityManager.createQuery(
                "SELECT SP FROM SoccerPlayer SP WHERE SP.name = :name", SoccerPlayer.class)
                .setParameter("name", name)
                .getResultList();
    }
}
```

Data JPA를 위한 인터페이스가 사용자 정의 인터페이스를 상속받도록 한다.

**SoccerPlayerDataRepository**

```java
public interface SoccerPlayerDataRepository extends
        JpaRepository<SoccerPlayer, Long>,
        SoccerPlayerDataRepositoryCustom {
    // 이하 생략
}
```

테스트 코드를 확인해보면 SoccerPlayerDataRepository를 통해서 사용자 정의 리포지토리의 메소드가 호출되는 것을 확인할 수 있다.

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
    @DisplayName("사용자 정의 리포지토리 정상 작동 테스트")
    void customRepositoryTest() {
        dataRepository.findCustomByName("Roy");
    }
}
```

---


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