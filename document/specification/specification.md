이번 장에서는 명세(Specification)에 대해서 알아본다.
글의 하단부에 참고한 강의와 공식문서의 경로를 첨부하였으므로 자세한 사항은 강의나 공식문서에서 확인한다.
모든 코드는 [깃허브 (링크)](https://github.com/roy-zz/data-jpa)에 있다.

---

Domain Driven Design (DDD)에는 명세(Specification)라는 개념이 있다.
Spring Data JPA에는 JPA Criteria를 활용하여 명세라는 개념을 사용할 수 있도록 지원한다.

참고한 강의에서 실무에서는 사용되지 않는 기능이라고 소개하고 있다.
Specification을 사용하여 개발을 진행하지는 않겠지만 언제 이러한 코드를 리팩토링하게 될지 모르니 자세히 알아보도록 한다.

JpaRepository가 JpaSpecificationExecutor<T>를 상속받도록 한다.

**SoccerPlayerDataRepository**

```java
public interface SoccerPlayerDataRepository extends
        JpaRepository<SoccerPlayer, Long>,
        SoccerPlayerDataRepositoryCustom,
        JpaSpecificationExecutor<SoccerPlayer> {
        // 생략
        }
```

Specification을 위한 클래스를 생성한다.
필자의 경우 팀의 이름으로 검색하는 teamName(),
입력받은 값보다 큰 선수를 검색하는 greaterHeight(),
입력받은 값보다 무거은 선수를 검색하는 greaterWeight() 

세 개의 메서드를 구현하였다.

**SoccerPlayerSpecification**

```java
public class SoccerPlayerSpecification {

    public static Specification<SoccerPlayer> teamName(final String name) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(name)) {
                return null;
            }
            Join<SoccerPlayer, Team> team = root.join("team", JoinType.INNER);
            return criteriaBuilder.equal(team.get("name"), name);
        };
    }

    public static Specification<SoccerPlayer> greaterHeight(final int height) {
        return ((root, query, criteriaBuilder) -> criteriaBuilder.greaterThan(root.get("height"), height));
    }

    public static Specification<SoccerPlayer> greaterWeight(final int weight) {
        return ((root, query, criteriaBuilder) -> criteriaBuilder.greaterThan(root.get("weight"), weight));
    }

}
```

Specification을 사용하는 코드는 아래와 같다.
Specification을 위한 클래스에서 정의한 메서드를 조립(컴포지트 패턴)하여 원하는 검색 조건을 만든다.

```java
@Transactional
@SpringBootTest
class SoccerPlayerDataRepositoryTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private SoccerPlayerDataRepository dataRepository;

    @Test
    @DisplayName("Specification 테스트")
    void specificationTest() {
        Team team1 = new Team("TeamA");
        Team team2 = new Team("TeamB");
        List<SoccerPlayer> players = List.of(
                new SoccerPlayer("Roy", 173, 73, team1),
                new SoccerPlayer("Perry", 180, 80, team1),
                new SoccerPlayer("Sally", 160, 50, team1),
                new SoccerPlayer("Dice", 183, 90, team2),
                new SoccerPlayer("Louis", 178, 85, team2)
        );
        dataRepository.saveAll(players);
        entityManager.flush();
        entityManager.clear();

        Specification<SoccerPlayer> specification =
                SoccerPlayerSpecification.teamName("TeamA")
                        .and(SoccerPlayerSpecification.greaterHeight(170))
                        .and(SoccerPlayerSpecification.greaterWeight(70));

        List<SoccerPlayer> storedPlayers = dataRepository.findAll(specification);
        assertEquals(2, storedPlayers.size());
    }
}
```

테스트는 통과할 것이며 발생한 쿼리는 아래와 같다.

```sql
Hibernate: 
    select
        soccerplay0_.soccer_player_id as soccer_p1_1_,
        soccerplay0_.created_at as created_2_1_,
        soccerplay0_.updated_at as updated_3_1_,
        soccerplay0_.created_by as created_4_1_,
        soccerplay0_.updated_by as updated_5_1_,
        soccerplay0_.height as height6_1_,
        soccerplay0_.name as name7_1_,
        soccerplay0_.team_id as team_id9_1_,
        soccerplay0_.weight as weight8_1_ 
    from
        soccer_player soccerplay0_ 
    inner join
        team team1_ 
            on soccerplay0_.team_id=team1_.team_id 
    where
        team1_.name=? 
        and soccerplay0_.height>170 
        and soccerplay0_.weight>70
```

우리가 예상한 쿼리가 발생하였다.
또한 명세를 chaining할 때 and 이외에 where(), or(), not()도 사용이 가능하다.

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