이번 장에서는 메소드 이름 쿼리에 대해서 알아본다.
글의 하단부에 참고한 강의와 공식문서의 경로를 첨부하였으므로 자세한 사항은 강의나 공식문서에서 확인한다.

---

SoccerPlayer가 가지는 속성은 아래와 같다.

```java
@Entity
@Getter @Setter
@ToString(of = {"id", "name", "height", "weight"})
@NoArgsConstructor(access = PROTECTED)
public class SoccerPlayer {

    @Id
    @GeneratedValue
    @Column(name = "soccer_player_id")
    private Long id;
    private String name;
    private int height;
    private int weight;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;
    
    // 이하 생략
}
```

이름과 몸무게 기준으로 축구 선수를 조회해본다.

**Pure JPA 리포지토리와 테스트**

```java
@Repository
public class SoccerPlayerPureRepository {
    @PersistenceContext
    private EntityManager entityManager;
    public List<SoccerPlayer> findByNameAndHeightGreaterThan(String name, int height) {
        return entityManager.createQuery(
                "SELECT SP " +
                        "FROM SoccerPlayer SP " +
                        "WHERE " +
                        "   SP.name = :name " +
                        "   AND SP.height >= :height", SoccerPlayer.class)
                .setParameter("name", name)
                .setParameter("height", height)
                .getResultList();
    }
}

@Transactional
@SpringBootTest
class SoccerPlayerPureRepositoryTest {

    @Autowired
    private SoccerPlayerPureRepository pureRepository;

    @Test
    @DisplayName("메소드 이름기반 쿼리 테스트")
    void methodNameQueryTest() {
        List<SoccerPlayer> players = List.of(
                new SoccerPlayer("Roy", 173),
                new SoccerPlayer("Roy", 183)
        );
        pureRepository.saveAll(players);

        List<SoccerPlayer> result = pureRepository.findByNameAndHeightGreaterThan("Roy", 180);
        assertEquals(1, result.size());
        assertEquals("Roy", result.get(0).getName());
        assertEquals(183, result.get(0).getHeight());;
    }
}
```

**Data JPA 리포지토리와 테스트**

```java
public interface SoccerPlayerDataRepository extends JpaRepository<SoccerPlayer, Long> {

    List<SoccerPlayer> findByNameAndHeightGreaterThan(String name, int height);

}

@Transactional
@SpringBootTest
class SoccerPlayerDataRepositoryTest {
    @Test
    @DisplayName("메소드 이름기반 쿼리 테스트")
    void methodNameQueryTest() {
        List<SoccerPlayer> players = List.of(
                new SoccerPlayer("Roy", 173),
                new SoccerPlayer("Roy", 183)
        );
        dataRepository.saveAll(players);

        List<SoccerPlayer> result = dataRepository.findByNameAndHeightGreaterThan("Roy", 180);
        assertEquals(1, result.size());
        assertEquals("Roy", result.get(0).getName());
        assertEquals(183, result.get(0).getHeight());;
    }

}
```

엔티티의 필드명만 맞춰준다면 메소드 명 만으로 우리가 원하는 결과를 가져올 수 있다.
전부 Spring Data JPA가 메소드의 이름을 보고 자동으로 생성해주기 때문이다.

만약 키가 180이 넘고 몸무게가 90kg 이하인 선수만 찾고 싶다면 메소드 명은 어떻게 변하게 되는지 알아본다.

```java
public interface SoccerPlayerDataRepository extends JpaRepository<SoccerPlayer, Long> {

    List<SoccerPlayer> findByNameAndHeightGreaterThan(String name, int height);

    List<SoccerPlayer> findByNameAndHeightGreaterThanAndWeightLessThan(String name, int height, int weight);

}

@Transactional
@SpringBootTest
class SoccerPlayerDataRepositoryTest {

    @Autowired
    private SoccerPlayerDataRepository dataRepository;
    @Test
    @DisplayName("메소드 이름기반 쿼리 테스트 (키와 몸무게를 기준으로 조회)")
    void methodNameQueryByHeightAndWeightTest() {
        List<SoccerPlayer> players = List.of(
                new SoccerPlayer("Roy", 173, 90),
                new SoccerPlayer("Roy", 183, 100),
                new SoccerPlayer("Roy", 183, 85)
        );
        dataRepository.saveAll(players);

        List<SoccerPlayer> result =
                dataRepository.findByNameAndHeightGreaterThanAndWeightLessThan("Roy", 180, 90);
        assertEquals(1, result.size());
        assertEquals("Roy", result.get(0).getName());
        assertEquals(183, result.get(0).getHeight());
        assertEquals(85, result.get(0).getWeight());
    }

}
```

역시 메소드 명에 원하는 필드를 추가하고 파라미터를 추가하는 것 만으로 우리가 원하는 결과를 얻어낼 수 있다.
또한 오타로 필드명과 다른 이름이 메소드에 추가되면 컴파일 시점에 오류가 발생한다는 장점을 가지고 있다.

하지만 필드가 많아지면 많아질수록 메소드 명이 길어진다는 단점이 있다.
충분히 편리한 기능이지만 파라미터가 3개 이상이라면 다른 방법으로 조회하는 것이 좋아보인다.

GraterThan, LessThen 이외에도 우리가 필요로 하는 대부분의 기능을 포함하고 있다.
자세한 사항은 [스프링 데이터 JPA 공식 문서 (링크)](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#jpa.query-methods.query-creation) 에 나와있다.

메소드 명만으로 페이징 처리도 가능한데 [Limiting Query Results(링크)](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repositories.limit-query-result)에서 확인 가능하다.

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