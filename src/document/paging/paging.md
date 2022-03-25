이번 장에서는 NamedQuery에 대해서 알아본다.
글의 하단부에 참고한 강의와 공식문서의 경로를 첨부하였으므로 자세한 사항은 강의나 공식문서에서 확인한다.
모든 코드는 [깃허브 (링크)](https://github.com/roy-zz/data-jpa)에 있다.

---

### 순수 JPA 기반의 Paging

**SoccerPlayerPureRepository**

```java
@Repository
public class SoccerPlayerPureRepository {
    @PersistenceContext
    private EntityManager entityManager;
    public Page<SoccerPlayer> findAllPage(int page, int size) {
        int offset = size * page;
        List<SoccerPlayer> content = entityManager.createQuery(
                        "SELECT SP " +
                                "FROM SoccerPlayer SP " +
                                "ORDER BY SP.height DESC ", SoccerPlayer.class)
                .setFirstResult(offset)
                .setMaxResults(size)
                .getResultList();

        long totalCount = entityManager.createQuery(
                        "SELECT COUNT(SP) " +
                                "FROM SoccerPlayer SP " +
                                "ORDER BY SP.height DESC ", Long.class)
                .getSingleResult();
        Sort sort = Sort.by(Sort.Direction.DESC, "height");
        Pageable pageable = PageRequest.of(page, size, sort);

        return new PageImpl<>(content, pageable, totalCount);
    }
}
```

클라이언트로 부터 page와 size 조건만 받아서 Paging 처리하는 방식이다.
생각보다 복잡해보이지 않지만 정렬 방식을 클라이언트로 부터 받는다면 코드는 상상하기 싫을 정도로 복잡해질 것이다.

페이징을 테스트하는 테스트 코드는 아래와 같다.

```java
@Transactional
@SpringBootTest
class SoccerPlayerPureRepositoryTest {
    @Autowired
    private SoccerPlayerPureRepository pureRepository;
    @Test
    @DisplayName("페이징 테스트")
    void pagingTest() {
        List<SoccerPlayer> players = List.of(
                new SoccerPlayer("Roy"),
                new SoccerPlayer("Perry"),
                new SoccerPlayer("Sally"),
                new SoccerPlayer("Dice"),
                new SoccerPlayer("Louis")
        );
        pureRepository.saveAll(players);

        Page<SoccerPlayer> pageOfPlayers = pureRepository.findAllPage(1, 2);
        List<SoccerPlayer> listOfPlayers = pageOfPlayers.getContent();
        assertEquals(2, listOfPlayers.size());
        assertEquals(5, pageOfPlayers.getTotalElements());
    }
}
```

---

### Data JPA 기반의 Paging

**SoccerPlayerDataRepository**
```java
public interface SoccerPlayerDataRepository extends JpaRepository<SoccerPlayer, Long> {
    Page<SoccerPlayer> findAllPage(Pageable pageable);
}
```

단 한 줄로 순수 JPA와 동일한 기능을 하는 메서드가 완료되었다.
테스트 코드 또한 정상적으로 통과한다.

```java
@Transactional
@SpringBootTest
class SoccerPlayerDataRepositoryTest {
    @Autowired
    private SoccerPlayerDataRepository dataRepository;
    @Test
    @DisplayName("페이징 테스트")
    void pagingTest() {
        List<SoccerPlayer> players = List.of(
                new SoccerPlayer("Roy", 173),
                new SoccerPlayer("Perry", 180),
                new SoccerPlayer("Sally", 160),
                new SoccerPlayer("Dice", 183),
                new SoccerPlayer("Louis", 178)
        );
        dataRepository.saveAll(players);

        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "height"));
        Page<SoccerPlayer> pageOfResult = dataRepository.findPageByNameIsNotNull(pageRequest);
        List<SoccerPlayer> listOfResult = pageOfResult.getContent();

        assertEquals(3, listOfResult.size());
        assertEquals(5, pageOfResult.getTotalElements());
        assertEquals(0, pageOfResult.getNumber());
        assertEquals(2, pageOfResult.getTotalPages());
        assertTrue(pageOfResult.isFirst());
        assertTrue(pageOfResult.hasNext());
    }
}
```

### Data JPA 기반의 Paging 최적화

Data JPA 기반의 페이징 처리를 최적화 하는 두 가지 방법을 알아본다.

**1. Page 대신 Slice를 사용하는 방법**

Page Interface를 확인해보면 Slice를 상속받고 있으며 전체 데이터를 확인하는 기능만 추가되어있다.
```java
public interface Page<T> extends Slice<T> {
	static <T> Page<T> empty() {
		return empty(Pageable.unpaged());
	}
	static <T> Page<T> empty(Pageable pageable) {
		return new PageImpl<>(Collections.emptyList(), pageable, 0);
	}
	int getTotalPages();
	long getTotalElements();
	<U> Page<U> map(Function<? super T, ? extends U> converter);
}
```

실제로 우리가 사용하는 대부분의 기능은 Slice Interface에 있다.
```java
public interface Slice<T> extends Streamable<T> {
	int getNumber();
	int getSize();
	int getNumberOfElements();
	List<T> getContent();
	boolean hasContent();
	Sort getSort();
	boolean isFirst();
	boolean isLast();
	boolean hasNext();
	boolean hasPrevious();
	default Pageable getPageable() {
		return PageRequest.of(getNumber(), getSize(), getSort());
	}
	Pageable nextPageable();
	Pageable previousPageable();
	<U> Slice<U> map(Function<? super T, ? extends U> converter);
	default Pageable nextOrLastPageable() {
		return hasNext() ? nextPageable() : getPageable();
	}
	default Pageable previousOrFirstPageable() {
		return hasPrevious() ? previousPageable() : getPageable();
	}
}
```

모바일 전용 어플리케이션들을 보면 전체 컨텐츠의 양은 중요하지 않은 경우들이 있다.
이러한 경우 Page 대신 Slice를 사용하여 무거운 count 쿼리를 사용하지 않도록 할 수 있다.

기존에 작동하던 테스트 코드를 수정하여 Slice로 변경하면 Total* 기능을 제외하고 동일하게 작용하는 것을 확인할 수 있다.

```java
@Transactional
@SpringBootTest
class SoccerPlayerDataRepositoryTest {
    @Autowired
    private SoccerPlayerDataRepository dataRepository;
    @Test
    @DisplayName("Slice 테스트")
    void sliceTest() {
        List<SoccerPlayer> players = List.of(
                new SoccerPlayer("Roy", 173),
                new SoccerPlayer("Perry", 180),
                new SoccerPlayer("Sally", 160),
                new SoccerPlayer("Dice", 183),
                new SoccerPlayer("Louis", 178)
        );
        dataRepository.saveAll(players);

        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "height"));
        Slice<SoccerPlayer> pageOfResult = dataRepository.findSliceByNameIsNotNull(pageRequest);
        List<SoccerPlayer> listOfResult = pageOfResult.getContent();

        assertEquals(3, listOfResult.size());
        // assertEquals(5, pageOfResult.getTotalElements()); // 컴파일 에러
        assertEquals(0, pageOfResult.getNumber());
        // assertEquals(2, pageOfResult.getTotalPages()); // 컴파일 에러
        assertTrue(pageOfResult.isFirst());
        assertTrue(pageOfResult.hasNext());
    }
}
```

**2. Count 쿼리의 분리**

Left Join이 사용되는 경우 Total Count의 결과는 Join되는 테이블과는 무관하다.
이러한 점을 이용하여 Count 쿼리를 커스텀하여 가볍게 만들 수 있다.

아래와 같이 Count 쿼리를 분리하면 Content를 가져올 때는 Join 쿼리가 발생하지만
Count 쿼리에는 Join이 발생하지 않아서 성능을 향상 시킬 수 있다.
```java
public interface SoccerPlayerDataRepository extends JpaRepository<SoccerPlayer, Long> {
    @Query(value = "SELECT SP " +
                   "FROM SoccerPlayer SP " +
                   "        LEFT JOIN SP.team T " +
                   "WHERE SP.name IS NOT NULL",
           countQuery = "SELECT SP FROM SoccerPlayer SP")
    Page<SoccerPlayer> findCustomPageByNameIsNotNull(Pageable pageable);

    Slice<SoccerPlayer> findSliceByNameIsNotNull(Pageable pageable);
}
```

---

Page의 map 기능을 사용하면 Content가 Entity로 이루어진 Page 객체를 손쉽게 DTO로 이루어진 객체로 변경시킬 수 있다.

```java
@Transactional
@SpringBootTest
class SoccerPlayerDataRepositoryTest {
    @Autowired
    private SoccerPlayerDataRepository dataRepository;
    @Test
    @DisplayName("Page map 테스트")
    void pagingMapTest() {
        List<SoccerPlayer> players = List.of(
                new SoccerPlayer("Roy", 173),
                new SoccerPlayer("Perry", 180),
                new SoccerPlayer("Sally", 160),
                new SoccerPlayer("Dice", 183),
                new SoccerPlayer("Louis", 178)
        );
        dataRepository.saveAll(players);

        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "height"));
        Page<SoccerPlayer> entityResult = dataRepository.findPageByNameIsNotNull(pageRequest);
        List<SoccerPlayer> listOfEntity = entityResult.getContent();

        Page<SoccerPlayerResponseDTO> dtoResult = entityResult.map(SoccerPlayerResponseDTO::of);
        List<SoccerPlayerResponseDTO> listOfDto = dtoResult.getContent();

        assertEquals(listOfEntity.get(0).getClass(), SoccerPlayer.class);
        assertEquals(listOfDto.get(0).getClass(), SoccerPlayerResponseDTO.class);
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