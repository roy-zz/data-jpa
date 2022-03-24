package com.roy.datajpa.repository.data;

import com.roy.datajpa.domain.SoccerPlayer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SoccerPlayerDataRepository extends JpaRepository<SoccerPlayer, Long> {
}
