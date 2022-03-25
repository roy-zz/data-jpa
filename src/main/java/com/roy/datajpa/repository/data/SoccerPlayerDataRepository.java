package com.roy.datajpa.repository.data;

import com.roy.datajpa.domain.SoccerPlayer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SoccerPlayerDataRepository extends JpaRepository<SoccerPlayer, Long> {

    List<SoccerPlayer> findByNameAndHeightGreaterThan(String name, int height);

    List<SoccerPlayer> findByNameAndHeightGreaterThanAndWeightLessThan(String name, int height, int weight);

    @Query(name = "SoccerPlayer.findByName")
    List<SoccerPlayer> findByNameUsingNamedQuery(@Param("name") String name);

    @Query(name = "SoccerPlayer.findByHeightGreaterThan")
    List<SoccerPlayer> findByHeightGreaterThanUsingNamedQuery(@Param("height") int height);

}
