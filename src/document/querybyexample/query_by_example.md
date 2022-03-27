이번 장에서는 Query By Example에 대해서 알아본다.
글의 하단부에 참고한 강의와 공식문서의 경로를 첨부하였으므로 자세한 사항은 강의나 공식문서에서 확인한다.
모든 코드는 [깃허브 (링크)](https://github.com/roy-zz/data-jpa)에 있다.

---

원하는 검색 조건을 가지고 있는 Probe라고 하는 도메인 객체를 생성하여 Example 객체를 생성하고 검색하는 기술이다. 

참고한 강의에서 실무에서는 사용되지 않는 기능이라고 소개하고 있다.
Query By Example 사용하여 개발을 진행하지는 않겠지만 언제 이러한 코드를 리팩토링하게 될지 모르고
아직 탄생한지 얼마 안된 기술이기 때문에 성장 가능성이 있으므로 간략하게 알아보도록 한다.

**사용 방법**

검색을 위한 examplePlayer와 exampleTeam을 생성하였다.
생성된 객체(Probe)를 Example.of()메서드를 사용하여 Example 객체로 변환한다.

변환된 Example 객체로 동일한 조건의 Entity를 찾는 쿼리가 생성된다.

```java
@Transactional
@SpringBootTest
class SoccerPlayerDataRepositoryTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private SoccerPlayerDataRepository dataRepository;

    @Test
    @DisplayName("Query By Example 테스트")
    void queryByExampleTest() {
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

        SoccerPlayer examplePlayer = new SoccerPlayer("Roy");
        Team exampleTeam = new Team("TeamA");
        examplePlayer.setTeam(exampleTeam);

        Example<SoccerPlayer> example = Example.of(examplePlayer);
        List<SoccerPlayer> storedPlayers = dataRepository.findAll(example);
        assertEquals(1, storedPlayers.size());
    }

}
```

팀의 이름이 "TeamA"이며 선수의 이름이 "Roy"인 한 명의 선수가 검색될 것이라고 예상하고 테스트 코드를 작성하였다.
하지만 예상과 다르게 storedPlayers의 사이즈는 0이었고 테스트는 실패하였다.

왜 실패하였는지 발생한 쿼리를 살펴본다.

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
        soccerplay0_.name=? 
        and soccerplay0_.height=0 
        and soccerplay0_.weight=0 
        and team1_.name=?
```

선수의 height와 weight는 Primitive 타입인 long이다.
선수를 생성할 때 값을 입력하지 않았으므로 해당 값은 null이 아닌 0이 된다.
이러한 이유로 쿼리가 생성될 때 height = 0, weight = 0인 조건이 같이 포함된 것이다.

그렇다면 height, weight는 검색조건에 포함되지 않도록 코드를 수정해본다.
아래와 같이 ExampleMatcher를 추가하여 원하는 필드를 제거할 수 있다.

```java
@Transactional
@SpringBootTest
class SoccerPlayerDataRepositoryTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private SoccerPlayerDataRepository dataRepository;
    @Test
    @DisplayName("Query By Example 테스트")
    void queryByExampleTest() {
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

        SoccerPlayer examplePlayer = new SoccerPlayer("Roy");
        Team exampleTeam = new Team("TeamA");
        examplePlayer.setTeam(exampleTeam);

        ExampleMatcher exampleMatcher = ExampleMatcher.matching()
                .withIgnorePaths("height", "weight");

        Example<SoccerPlayer> example = Example.of(examplePlayer, exampleMatcher);
        List<SoccerPlayer> storedPlayers = dataRepository.findAll(example);
        assertEquals(1, storedPlayers.size());
    }
}
```

테스트는 성공하며 발생한 쿼리는 아래와 같다.

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
        soccerplay0_.name=? 
        and team1_.name=?
```

---

**주의 사항**

1. 갑자기 검색조건이 변경되어 선수(SoccerPlayer)를 조회할 때 팀(Team)이 없는 선수도 조회가 가능하도록 해달라는 요청이 들어왔다.

요청 사항을 듣고 inner join을 outer join으로 변경해야 하는 상황이다.
하지만 Query By Example을 사용한 방식에서는 **Outer Join이 불가능**하다.

결국 기존에 작성되어 있던 모든 코드를 변경해서 문제를 해결해야한다.

2. 갑자기 검색조건에 키가 180이 넘는 선수만 조회하게 해달라는 요청이 들어왔다.

요청 사항을 듣고 검색조건에 height > 180을 추가하려 하였으나 불가능 하였다.
Query By Example을 사용하는 방식에서 **문자를 제외한 속성은 Equal(=)**만 지원한다.

3. 중첩 제약조건 불가능

---

참고한 강의:

- https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81-%EB%8D%B0%EC%9D%B4%ED%84%B0-JPA-%EC%8B%A4%EC%A0%84
- https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81%EB%B6%80%ED%8A%B8-JPA-API%EA%B0%9C%EB%B0%9C-%EC%84%B1%EB%8A%A5%EC%B5%9C%EC%A0%81%ED%99%94
- https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81%EB%B6%80%ED%8A%B8-JPA-%ED%99%9C%EC%9A%A9-1
- https://www.inflearn.com/course/ORM-JPA-Basic

JPA 공식 문서:

- https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#reference
- https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#query-by-example

위키백과:

- https://ko.wikipedia.org/wiki/%EC%9E%90%EB%B0%94_%ED%8D%BC%EC%8B%9C%EC%8A%A4%ED%84%B4%EC%8A%A4_API