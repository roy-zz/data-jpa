package com.roy.datajpa.repository.data;

import com.roy.datajpa.domain.SoccerPlayer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SoccerPlayerDataRepository extends JpaRepository<SoccerPlayer, Long> {
}
