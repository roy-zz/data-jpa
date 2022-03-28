이번 장에서는 @EntityGraph 어노테이션에 대해서 알아본다.
글의 하단부에 참고한 강의와 공식문서의 경로를 첨부하였으므로 자세한 사항은 강의나 공식문서에서 확인한다.
모든 코드는 [깃허브 (링크)](https://github.com/roy-zz/data-jpa)에 있다.

---

우리는 지금까지 모든 연관관계를 Lazy로 설정하였다.
이번에는 이렇게 Lazy로 설정되어있는 Entity까지 한 번에 조회하기 위해 
순수 JPA의 경우 명시적으로 Fetch Join을 사용하고 Data JPA의 경우 @EntityGraph를 통해 Fetch Join을 사용해본다.

조회 대상 Entity인 SoccerPlayer 클래스는 아래와 같다.

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
@EqualsAndHashCode
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

    @ManyToOne(fetch = LAZY, cascade = ALL)
    @JoinColumn(name = "team_id")
    private Team team;
    // 이하 생략
}
```

정말로 1 + N 문제가 발생하는지 아래의 테스트 코드를 실행시켜서 확인해본다.

**SoccerPlayerPureRepository**

```java
@Transactional
@SpringBootTest
class SoccerPlayerPureRepositoryTest {
    @Autowired
    private SoccerPlayerPureRepository pureRepository;
    @Test
    @DisplayName("N + 1 발생 검증 테스트")
    void isOccurNPlusOneTest() {
        List<SoccerPlayer> soccerPlayers = List.of(
                new SoccerPlayer("Roy", 173, 75, new Team("TeamA")),
                new SoccerPlayer("Perry", 180, 80, new Team("TeamB"))
        );
        pureRepository.saveAll(soccerPlayers);
        pureRepository.flushAndClear();

        List<SoccerPlayer> storedPlayers = pureRepository.findAll();

        storedPlayers.forEach(player -> {
            assertFalse(Hibernate.isInitialized(player.getTeam()));
            System.out.println("player.getTeam().getName() = " + player.getTeam().getName());
            assertTrue(Hibernate.isInitialized(player.getTeam()));
        });
    }
}
```

선수와 함께 팀을 저장하고 영속성 컨텍스트를 flush, clear하여 캐시를 지워준다.
이후 DB에서 선수들을 다시 조회하여 영속성 컨텍스트에 캐싱한다.
선수들을 순회하면서 Hibernate.isInitialized 메서드를 사용하여 Team이 초기화 되지 않았음을 검증하고
이름을 조회하여 Lazy Loading을 유도한 다음 Team이 초기화 되었는지를 검증한다.

테스트 결과는 아래와 같다.

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
Hibernate: 
    select
        team0_.team_id as team_id1_1_0_,
        team0_.name as name2_1_0_ 
    from
        team team0_ 
    where
        team0_.team_id=?
player.getTeam().getName() = TeamA
Hibernate: 
    select
        team0_.team_id as team_id1_1_0_,
        team0_.name as name2_1_0_ 
    from
        team team0_ 
    where
        team0_.team_id=?
player.getTeam().getName() = TeamB
```

테스트 결과는 성공이며 getName을 호출하기 이전에 DB에서 Team을 조회하였다.
N + 1이 발생한 것을 확인할 수 있다.

### 순수 JPA의 Fetch Join

순수 JPA에서 N + 1을 해결하는 방법은 여러가지 방법이 있지만 이번 장에서는 Fetch Join을 사용해서 해결해본다.
이유는 Data JPA에서 사용할 @EntityGraph 어노테이션이 Lazy Entity의 데이터를 가져오기 위해 Fetch Join을 사용하기 때문이다.

SoccerPlayer를 조회할 때 Fetch Join을 사용하여 Team까지 한 번에 조회하는 방법은 아래와 같다.

```java
@Repository
public class SoccerPlayerPureRepository {
    @PersistenceContext
    private EntityManager entityManager;
    public List<SoccerPlayer> findAllUsingFetchJoin() {
        return entityManager.createQuery(
                        "SELECT SP " +
                                "FROM " +
                                "   SoccerPlayer SP " +
                                "       JOIN FETCH SP.team T ", SoccerPlayer.class)
                .getResultList();
    }
}
```

Team까지 조회된 것이 맞는지 아래의 테스트 코드를 실행시켜 본다.

```java
@Transactional
@SpringBootTest(properties = "test")
class SoccerPlayerPureRepositoryTest {
    @Autowired
    private SoccerPlayerPureRepository pureRepository;
    @Test
    @DisplayName("Fetch Join을 사용하고 N + 1이 발생하지 않음을 검증")
    void isNotOccurNPlusOneTest() {
        List<SoccerPlayer> soccerPlayers = List.of(
                new SoccerPlayer("Roy", 173, 75, new Team("TeamA")),
                new SoccerPlayer("Perry", 180, 80, new Team("TeamB"))
        );

        pureRepository.saveAll(soccerPlayers);
        pureRepository.flushAndClear();

        List<SoccerPlayer> storedPlayers = pureRepository.findAllUsingFetchJoin();

        storedPlayers.forEach(player -> {
            assertTrue(Hibernate.isInitialized(player.getTeam()));
            System.out.println("player.getTeam().getName() = " + player.getTeam().getName());
        });
    }
}
```

발생한 쿼리는 아래와 같다.
SoccerPlayer를 조회할 때 Team을 같이 조회하여 추가 쿼리없이 Team Entity까지 영속성 컨텍스트에 존재하였다.

```bash
Hibernate: 
    select
        soccerplay0_.soccer_player_id as soccer_p1_0_0_,
        team1_.team_id as team_id1_1_1_,
        soccerplay0_.height as height2_0_0_,
        soccerplay0_.name as name3_0_0_,
        soccerplay0_.team_id as team_id5_0_0_,
        soccerplay0_.weight as weight4_0_0_,
        team1_.name as name2_1_1_ 
    from
        soccer_player soccerplay0_ 
    inner join
        team team1_ 
            on soccerplay0_.team_id=team1_.team_id
player.getTeam().getName() = TeamA
player.getTeam().getName() = TeamB
```

---

### Data JPA의 @EntityGraph

여러 상황에서 사용이 가능한데 아래의 예제 기준으로
1번은 JpaRepository의 findAll() 메서드를 오버라이드 하여 team까지 한 번에 조회되도록 하였다.
2번의 경우 JPQL 쿼리에 Fetch Join은 없지만 EntityGraph 어노테이션을 사용하여 Fetch Join 되도록 하였다.
3번의 경우 메서드 이름으로 SoccerPlayer를 조회할 때 Fetch Join을 사용하여 Team까지 조회되도록 하였다.

```java
public interface SoccerPlayerDataRepository extends JpaRepository<SoccerPlayer, Long> {
    // 1번
    @Override
    @EntityGraph(attributePaths = {"team"})
    List<SoccerPlayer> findAll();
    // 2번
    @EntityGraph(attributePaths = {"team"})
    @Query("SELECT SP FROM SoccerPlayer SP")
    List<SoccerPlayer> findAllUsingJpqlEntityGraph();
    // 3번
    @EntityGraph(attributePaths = {"team"})
    List<SoccerPlayer> findAllByName(String name);
}
```

1번 케이스를 테스트 코드로 검증한다.

```java
@Transactional
@SpringBootTest
class SoccerPlayerDataRepositoryTest {
    @Autowired
    private SoccerPlayerDataRepository dataRepository;
    @Test
    @DisplayName("순수 @EntityGraph 테스트")
    void onlyEntityGraphTest() {
        List<SoccerPlayer> soccerPlayers = List.of(
                new SoccerPlayer("Roy", 173, 75, new Team("TeamA")),
                new SoccerPlayer("Perry", 180, 80, new Team("TeamB"))
        );
        dataRepository.saveAll(soccerPlayers);

        List<SoccerPlayer> storedPlayers = dataRepository.findAll();
        storedPlayers.forEach(player -> {
            assertTrue(Hibernate.isInitialized(player));
        });
    }
}
```

테스트는 성공하였으며 SoccerPlayer를 조회할 때 Team까지 조회한다.

```bash 
Hibernate: 
    select
        soccerplay0_.soccer_player_id as soccer_p1_0_0_,
        team1_.team_id as team_id1_1_1_,
        soccerplay0_.height as height2_0_0_,
        soccerplay0_.name as name3_0_0_,
        soccerplay0_.team_id as team_id5_0_0_,
        soccerplay0_.weight as weight4_0_0_,
        team1_.name as name2_1_1_ 
    from
        soccer_player soccerplay0_ 
    left outer join
        team team1_ 
            on soccerplay0_.team_id=team1_.team_id
```

2번 케이스를 테스트 코드로 검증한다.

```java
@Transactional
@SpringBootTest
class SoccerPlayerDataRepositoryTest {
    @Autowired
    private SoccerPlayerDataRepository dataRepository;
    @Test
    @DisplayName("JPQL + @EntityGraph 테스트")
    void jqplAndEntityGraphTest() {
            List<SoccerPlayer> soccerPlayers = List.of(
        new SoccerPlayer("Roy", 173, 75, new Team("TeamA")),
        new SoccerPlayer("Perry", 180, 80, new Team("TeamB"))
        );
        dataRepository.saveAll(soccerPlayers);

        List<SoccerPlayer> storedPlayers = dataRepository.findAllUsingJpqlEntityGraph();
        storedPlayers.forEach(player -> {
        assertTrue(Hibernate.isInitialized(player));
        });
    }
}
```

테스트는 성공하였으며 SoccerPlayer를 조회할 때 Team까지 조회한다.

```bash
Hibernate: 
    select
        soccerplay0_.soccer_player_id as soccer_p1_0_0_,
        team1_.team_id as team_id1_1_1_,
        soccerplay0_.height as height2_0_0_,
        soccerplay0_.name as name3_0_0_,
        soccerplay0_.team_id as team_id5_0_0_,
        soccerplay0_.weight as weight4_0_0_,
        team1_.name as name2_1_1_ 
    from
        soccer_player soccerplay0_ 
    left outer join
        team team1_ 
            on soccerplay0_.team_id=team1_.team_id
```

3번 케이스를 테스트 코드로 검증한다.

```java
@Transactional
@SpringBootTest
class SoccerPlayerDataRepositoryTest {
    @Autowired
    private SoccerPlayerDataRepository dataRepository;
    @Test
    @DisplayName("메서드 명 쿼리 + @EntityGraph 테스트")
    void methodNameAndEntityGraphTest() {
        List<SoccerPlayer> soccerPlayers = List.of(
                new SoccerPlayer("Roy", 173, 75, new Team("TeamA")),
                new SoccerPlayer("Perry", 180, 80, new Team("TeamB"))
        );
        dataRepository.saveAll(soccerPlayers);

        List<SoccerPlayer> storedPlayers = dataRepository.findAllByName("Roy");
        storedPlayers.forEach(player -> {
            assertTrue(Hibernate.isInitialized(player));
        });
    }
}
```

테스트는 성공하였으며 SoccerPlayer를 조회할 때 Team까지 조회한다.

```bash
Hibernate: 
    select
        soccerplay0_.soccer_player_id as soccer_p1_0_0_,
        team1_.team_id as team_id1_1_1_,
        soccerplay0_.height as height2_0_0_,
        soccerplay0_.name as name3_0_0_,
        soccerplay0_.team_id as team_id5_0_0_,
        soccerplay0_.weight as weight4_0_0_,
        team1_.name as name2_1_1_ 
    from
        soccer_player soccerplay0_ 
    left outer join
        team team1_ 
            on soccerplay0_.team_id=team1_.team_id 
    where
        soccerplay0_.name=?
```

### Summary

메소드 명 쿼리, 단순한 쿼리에는 @EntityGraph를 사용하여 해결한다.
쿼리가 복잡하거나 이미 JPQL을 작성해야한다면 Fetch Join으로 해결한다.

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