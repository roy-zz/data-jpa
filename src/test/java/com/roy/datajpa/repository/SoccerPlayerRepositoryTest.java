package com.roy.datajpa.repository;

import com.roy.datajpa.domain.SoccerPlayer;
import com.roy.datajpa.repository.data.SoccerPlayerDataRepository;
import com.roy.datajpa.repository.pure.SoccerPlayerPureRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest
public class SoccerPlayerRepositoryTest {

    @Autowired
    private SoccerPlayerPureRepository pureRepository;
    @Autowired
    private SoccerPlayerDataRepository dataRepository;

    @Test
    @DisplayName("축구선수 저장 및 조회 테스트")
    void saveAndFindTest() {
        SoccerPlayer soccerPlayer = new SoccerPlayer("Roy");
        SoccerPlayer storedSoccerPlayer = pureRepository.save(soccerPlayer);
        SoccerPlayer foundSoccerPlayer = pureRepository.findEntityById(storedSoccerPlayer.getId());
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
                pureRepository.findEntityById(soccerPlayers.get(0).getId());
        SoccerPlayer foundSoccerPlayer2 =
                pureRepository.findEntityById(soccerPlayers.get(1).getId());

        assertEquals(foundSoccerPlayer1, soccerPlayers.get(0));
        assertEquals(foundSoccerPlayer2, soccerPlayers.get(1));
    }

}
