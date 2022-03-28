이번 장에서는 Parameter 바인딩과 반환 타입에 대해서 알아본다.
글의 하단부에 참고한 강의와 공식문서의 경로를 첨부하였으므로 자세한 사항은 강의나 공식문서에서 확인한다.
모든 코드는 [깃허브 (링크)](https://github.com/roy-zz/data-jpa)에 있다.

---

### 파라미터 바인딩 

SoccerPlayerDataRepository 코드를 확인해보면 위치 기반 파라미터 바인딩과 이름 기반 바인딩이 존재한다.

```java
public interface SoccerPlayerDataRepository extends JpaRepository<SoccerPlayer, Long> {
    @Query(value =
            "SELECT SP " +
                    "FROM SoccerPlayer SP " +
                    "WHERE " +
                    "   SP.name = ?1 " +
                    "   AND SP.height > ?2 ")
    List<SoccerPlayer> findByNameAndHeightWithPositionBaseBinding(String name, int height);

    @Query(value =
            "SELECT SP " +
                    "FROM SoccerPlayer SP " +
                    "WHERE " +
                    "   SP.name = :name " +
                    "   AND SP.height > :height ")
    List<SoccerPlayer> findByNameAndHeightWithNameBaseBinding(String name, int height);
}
```

Test를 위하여 SoccerPlayer 클래스에 @EqualsAndHashCode 어노테이션을 추가하였다.

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
    // 생략
}
```

테스트 코드를 돌려보면 두 방식의 결과가 동일한 것을 확인할 수 있다.

```java
@Transactional
@SpringBootTest
class SoccerPlayerDataRepositoryTest {
    @Autowired
    private SoccerPlayerDataRepository dataRepository;
    @Test
    @DisplayName("파라미터 바인딩 테스트")
    void parameterBindingTest() {
        List<SoccerPlayer> players = List.of(
                new SoccerPlayer("Roy", 173),
                new SoccerPlayer("Perry", 183)
        );
        dataRepository.saveAll(players);

        List<SoccerPlayer> positionBaseResult =
                dataRepository.findByNameAndHeightWithPositionBaseBinding("Perry", 180);
        List<SoccerPlayer> nameBaseResult =
                dataRepository.findByNameAndHeightWithNameBaseBinding("Perry", 180);
        
        assertEquals(nameBaseResult.size(), positionBaseResult.size());
        for (int i = 0; i < positionBaseResult.size(); i++) {
            assertEquals(positionBaseResult.get(i), nameBaseResult.get(i));
        }
    }
}
```

또한 아래와 같이 컬렉션 타입을 파라미터로 받아서 IN 쿼리에 사용할 수 있다.

```java
public interface SoccerPlayerDataRepository extends JpaRepository<SoccerPlayer, Long> {
    @Query(value =
            "SELECT SP " +
            "FROM SoccerPlayer SP " +
            "WHERE " +
            "   SP.id IN :ids")
    List<SoccerPlayer> findByIdIn(Set<Long> ids);
}
```

```java
@Transactional
@SpringBootTest
class SoccerPlayerDataRepositoryTest {

    @Autowired
    private SoccerPlayerDataRepository dataRepository;
    @Test
    @DisplayName("파라미터 바인딩 Collection 타입 테스트")
    void collectionBindingTest() {
        List<SoccerPlayer> players = List.of(
                new SoccerPlayer("Roy", 173),
                new SoccerPlayer("Perry", 183)
        );
        dataRepository.saveAll(players);

        Set<Long> targetIds = players.stream()
                .map(SoccerPlayer::getId)
                .collect(Collectors.toSet());

        List<SoccerPlayer> result = dataRepository.findByIdIn(targetIds);
        assertEquals(2, result.size());
    }
}
```

우리가 원하는 테스트 결과가 나오는 것을 확인할 수 있다.

---

### 반환 타입

Repository에서 Return 타입으로 원하는 타입을 지정하면 우리가 원하는 타입으로 가져올 수 있다.

```java
public interface SoccerPlayerDataRepository extends JpaRepository<SoccerPlayer, Long> {
    List<SoccerPlayer> findAllByName(String name);

    SoccerPlayer findOneByName(String name);

    Optional<SoccerPlayer> findOptionalOneByName(String name);
}
```

단 컬렉션의 경우 결과가 없으면 빈 컬렉션을 반환한다.
단건 조회의 경우 결과가 없으면 null을 반환하고 결과가 2개 이상이라면 NonUniqueResultException이 발생한다.

단건으로 조회하는 경우 JPQL의 getSingleResult 메서드를 호출하여 데이터를 조회한다.
javax의 NoResultException이 발생하면 Spring Data JPA에서 예외를 무시하고 null을 반환한다.

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