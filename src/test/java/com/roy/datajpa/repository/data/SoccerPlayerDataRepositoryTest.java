package com.roy.datajpa.repository.data;

import com.roy.datajpa.domain.SoccerPlayer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

}