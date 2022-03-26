package com.roy.datajpa.repository.data.custom;

import com.roy.datajpa.domain.SoccerPlayer;

import java.util.List;

public interface SoccerPlayerDataRepositoryCustom {
    List<SoccerPlayer> findCustomByName(String name);
}
