package com.roy.datajpa.repository.pure;

import com.roy.datajpa.domain.SoccerPlayer;
import com.roy.datajpa.domain.Team;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest
class SoccerPlayerPureRepositoryTest {

    @Autowired
    private SoccerPlayerPureRepository pureRepository;

    @Test
    @DisplayName("축구선수 저장 및 조회 테스트")
    void saveAndFindTest() {
        SoccerPlayer soccerPlayer = new SoccerPlayer("Roy");
        SoccerPlayer storedSoccerPlayer = pureRepository.save(soccerPlayer);
        SoccerPlayer foundSoccerPlayer = pureRepository.findOptionalById(storedSoccerPlayer.getId()).orElseThrow();
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
        pureRepository.saveAll(soccerPlayers);
        SoccerPlayer foundSoccerPlayer1 =
                pureRepository.findOptionalById(soccerPlayers.get(0).getId()).orElseThrow();
        SoccerPlayer foundSoccerPlayer2 =
                pureRepository.findOptionalById(soccerPlayers.get(1).getId()).orElseThrow();

        assertEquals(foundSoccerPlayer1, soccerPlayers.get(0));
        assertEquals(foundSoccerPlayer2, soccerPlayers.get(1));

        List<SoccerPlayer> storedSoccerPlayers = pureRepository.findAll();
        assertEquals(soccerPlayers.size(), storedSoccerPlayers.size());

        long storedCount = pureRepository.count();
        assertEquals(soccerPlayers.size(), storedCount);

        pureRepository.deleteAll(storedSoccerPlayers);

        long deletedCount = pureRepository.count();
        assertEquals(0, deletedCount);
    }

    @Test
    @DisplayName("메소드 이름기반 쿼리 테스트 키 기준 조회")
    void methodNameQueryByHeightTest() {
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
        pureRepository.saveAll(players);
        int updatedCount = pureRepository.bulkUpdate(170);
        assertEquals(4, updatedCount);
        pureRepository.clear();
        SoccerPlayer storedPlayer = pureRepository.findByName("Roy");
        assertEquals(83, storedPlayer.getWeight());
    }

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

    @Test
    @DisplayName("Auditing 테스트")
    void auditingTest() {
        SoccerPlayer soccerPlayer = new SoccerPlayer("Roy", 173);
        pureRepository.save(soccerPlayer);
        soccerPlayer.setHeight(183);
        pureRepository.flushAndClear();
        
        SoccerPlayer storedPlayer = pureRepository.findByName("Roy");
//        System.out.println("storedPlayer.getCreatedAt() = " + storedPlayer.getCreatedAt());
//        System.out.println("storedPlayer.getUpdatedAt() = " + storedPlayer.getUpdatedAt());
    }

}
