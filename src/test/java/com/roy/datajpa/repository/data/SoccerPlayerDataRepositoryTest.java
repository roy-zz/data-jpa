package com.roy.datajpa.repository.data;

import com.roy.datajpa.domain.SoccerPlayer;
import com.roy.datajpa.domain.Team;
import com.roy.datajpa.repository.data.query.dto.SoccerPlayerResponseDTO;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest
class SoccerPlayerDataRepositoryTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private SoccerPlayerDataRepository dataRepository;

    @Test
    @DisplayName("축구선수 저장 및 조회 테스트")
    void saveAndFindTest() {
        SoccerPlayer soccerPlayer = new SoccerPlayer("Roy");
        SoccerPlayer storedSoccerPlayer = dataRepository.save(soccerPlayer);
        SoccerPlayer foundSoccerPlayer = dataRepository.findById(storedSoccerPlayer.getId()).orElseThrow();
        assertEquals(foundSoccerPlayer.getId(), storedSoccerPlayer.getId());
        assertEquals(foundSoccerPlayer.getName(), storedSoccerPlayer.getName());
        // JPA Entity 동일성 보장
        assertEquals(foundSoccerPlayer, storedSoccerPlayer);
    }

    @Test
    @DisplayName("축구선수 CRUD 테스트")
    void crudTest() {
        List<SoccerPlayer> soccerPlayers = List.of(
                new SoccerPlayer("Roy"),
                new SoccerPlayer("Perry")
        );
        dataRepository.saveAll(soccerPlayers);
        SoccerPlayer foundSoccerPlayer1 =
                dataRepository.findById(soccerPlayers.get(0).getId()).orElseThrow();
        SoccerPlayer foundSoccerPlayer2 =
                dataRepository.findById(soccerPlayers.get(1).getId()).orElseThrow();

        assertEquals(foundSoccerPlayer1, soccerPlayers.get(0));
        assertEquals(foundSoccerPlayer2, soccerPlayers.get(1));

        List<SoccerPlayer> storedSoccerPlayers = dataRepository.findAll();
        assertEquals(soccerPlayers.size(), storedSoccerPlayers.size());

        long storedCount = dataRepository.count();
        assertEquals(soccerPlayers.size(), storedCount);

        dataRepository.deleteAll(storedSoccerPlayers);

        long deletedCount = dataRepository.count();
        assertEquals(0, deletedCount);

    }

    @Test
    @DisplayName("메소드 이름기반 쿼리 테스트 (키를 기준으로 조회)")
    void methodNameQueryByHeightTest() {
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

    @Test
    @DisplayName("@Query Entity 조회 테스트")
    void queryAnnotationFindEntityTest() {
        List<SoccerPlayer> players = List.of(
                new SoccerPlayer("Roy", 173),
                new SoccerPlayer("Perry", 183)
        );
        dataRepository.saveAll(players);

        List<SoccerPlayer> result =
                dataRepository.findEntityAllUsingQueryAnnotation();
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("@Query DTO 조회 테스트")
    void queryAnnotationFindDTOTest() {
        List<SoccerPlayer> players = List.of(
                new SoccerPlayer("Roy", 173),
                new SoccerPlayer("Perry", 183)
        );
        dataRepository.saveAll(players);

        List<SoccerPlayerResponseDTO> result =
                dataRepository.findDTOAllUsingQueryAnnotation();
        assertEquals(2, result.size());
        assertEquals(players.get(0).getName(), result.get(0).getName());
        assertEquals(players.get(0).getHeight(), result.get(0).getHeight());

        result.forEach(r -> System.out.println("r.getClass() = " + r.getClass()));
    }

    @Test
    @DisplayName("파라미터 바인딩 위치기반 vs 이름기반 테스트")
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

    @Test
    @DisplayName("벌크 업데이트 테스트")
    void bulkUpdateTest() {
        List<SoccerPlayer> players = List.of(
                new SoccerPlayer("Roy", 173, 73),
                new SoccerPlayer("Perry", 180, 80),
                new SoccerPlayer("Sally", 160, 60),
                new SoccerPlayer("Dice", 183, 83),
                new SoccerPlayer("Louis", 178, 78)
        );
        dataRepository.saveAll(players);
        int updatedCount = dataRepository.bulkUpdate(170);
        assertEquals(4, updatedCount);
        SoccerPlayer storedPlayer = dataRepository.findOneByName("Roy");
        assertEquals(83, storedPlayer.getWeight());
    }

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

    @Test
    @DisplayName("Lock 테스트")
    void lockTest() {
        dataRepository.findUsingLockByName("Roy");
    }

    @Test
    @DisplayName("사용자 정의 리포지토리 정상 작동 테스트")
    void customRepositoryTest() {
        dataRepository.findCustomByName("Roy");
    }
    
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
