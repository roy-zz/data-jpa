package com.roy.datajpa.repository.data;

import com.roy.datajpa.domain.SoccerPlayer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SoccerPlayerDataRepository extends JpaRepository<SoccerPlayer, Long> {

    List<SoccerPlayer> findByNameAndHeightGreaterThan(String name, int height);

    List<SoccerPlayer> findByNameAndHeightGreaterThanAndWeightLessThan(String name, int height, int weight);

}
