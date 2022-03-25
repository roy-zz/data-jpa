package com.roy.datajpa.repository.data;

import com.roy.datajpa.domain.SoccerPlayer;
import com.roy.datajpa.repository.data.query.dto.SoccerPlayerResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Transactional
@SpringBootTest
class SoccerPlayerDataRepositoryTest {

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

}
