이번 장에서는 NamedQuery에 대해서 알아본다.
글의 하단부에 참고한 강의와 공식문서의 경로를 첨부하였으므로 자세한 사항은 강의나 공식문서에서 확인한다.
모든 코드는 [깃허브 (링크)](https://github.com/roy-zz/data-jpa)에 있다.

---

SoccerPlayer가 가지는 속성은 아래와 같으며 @NamedQuery 어노테이션으로 Named Query를 적용할 수 있다.

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

**Pure JPA 기반의 리포지토리 및 테스트**

```java
@Repository
public class SoccerPlayerPureRepository {
    @PersistenceContext
    private EntityManager entityManager;
    public List<SoccerPlayer> findByNameUsingNamedQuery(String name) {
        return entityManager.createNamedQuery("SoccerPlayer.findByName", SoccerPlayer.class)
                .setParameter("name", name)
                .getResultList();
    }

    public List<SoccerPlayer> findByHeightGreaterThanUsingNamedQuery(int height) {
        return entityManager.createNamedQuery("SoccerPlayer.findByHeightGreaterThan", SoccerPlayer.class)
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
    @DisplayName("네임드 쿼리 테스트(이름 조회)")
    void namedQueryByNameTest() {
        List<SoccerPlayer> players = List.of(
                new SoccerPlayer("Roy"),
                new SoccerPlayer("Perry")
        );
        pureRepository.saveAll(players);

        List<SoccerPlayer> result =
                pureRepository.findByNameUsingNamedQuery("Roy");
        assertEquals(1, result.size());
        assertEquals("Roy", result.get(0).getName());
    }

    @Test
    @DisplayName("네임드 쿼리 테스트(키 조회)")
    void namedQueryByHeightTest() {
        List<SoccerPlayer> players = List.of(
                new SoccerPlayer("Roy", 173),
                new SoccerPlayer("Perry", 183)
        );
        pureRepository.saveAll(players);

        List<SoccerPlayer> result =
                pureRepository.findByHeightGreaterThanUsingNamedQuery(180);
        assertEquals(1, result.size());
        assertEquals("Perry", result.get(0).getName());
        assertEquals(183, result.get(0).getHeight());
    }

}
```

**Data JPA 기반의 리포지토리 및 테스트**

```java
public interface SoccerPlayerDataRepository extends JpaRepository<SoccerPlayer, Long> {
    
    // 변수명과 필드명이 일치한다면 @Param 생략 가능
    @Query(name = "SoccerPlayer.findByName")
    List<SoccerPlayer> findByNameUsingNamedQuery(@Param("name") String name);

    @Query(name = "SoccerPlayer.findByHeightGreaterThan")
    List<SoccerPlayer> findByHeightGreaterThanUsingNamedQuery(@Param("height") int height);

}

@Transactional
@SpringBootTest
class SoccerPlayerDataRepositoryTest {

    @Autowired
    private SoccerPlayerDataRepository dataRepository;
    @Test
    @DisplayName("네임드 쿼리 테스트(이름 조회)")
    void namedQueryByNameTest() {
        List<SoccerPlayer> players = List.of(
                new SoccerPlayer("Roy"),
                new SoccerPlayer("Perry")
        );
        dataRepository.saveAll(players);

        List<SoccerPlayer> result =
                dataRepository.findByNameUsingNamedQuery("Roy");
        assertEquals(1, result.size());
        assertEquals("Roy", result.get(0).getName());
    }

    @Test
    @DisplayName("네임드 쿼리 테스트(키 조회)")
    void namedQueryByHeightTest() {
        List<SoccerPlayer> players = List.of(
                new SoccerPlayer("Roy", 173),
                new SoccerPlayer("Perry", 183)
        );
        dataRepository.saveAll(players);

        List<SoccerPlayer> result =
                dataRepository.findByHeightGreaterThanUsingNamedQuery(180);
        assertEquals(1, result.size());
        assertEquals("Perry", result.get(0).getName());
        assertEquals(183, result.get(0).getHeight());
    }
}
```

테스트를 진행해보면 우리가 원하는 결과가 나오는 것을 알 수 있다.

Spring Data JPA는 선언한 "클래스명.메서드이름" 규칙으로 Named Query를 찾아서 실행한다.
만약 실행하려는 Named Query가 없다면 [메서드 이름으로 쿼리를 생성(링크)](https://imprint.tistory.com/134)하여 데이터를 조회한다.

우선권은 우리가 작성한 Named Query가 더 높다.
물론 [공식 문서(링크)](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repositories.query-methods.query-lookup-strategies)를 확인해보면 전략을 변경할 수 있다.

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