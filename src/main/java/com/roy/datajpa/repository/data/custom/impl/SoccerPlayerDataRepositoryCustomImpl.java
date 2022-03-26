package com.roy.datajpa.repository.data.custom.impl;

import com.roy.datajpa.domain.SoccerPlayer;
import com.roy.datajpa.repository.data.custom.SoccerPlayerDataRepositoryCustom;
import lombok.RequiredArgsConstructor;

import javax.persistence.EntityManager;
import java.util.List;

@RequiredArgsConstructor
public class SoccerPlayerDataRepositoryCustomImpl implements SoccerPlayerDataRepositoryCustom {

    private final EntityManager entityManager;

    @Override
    public List<SoccerPlayer> findCustomByName(String name) {
        return entityManager.createQuery(
                "SELECT SP FROM SoccerPlayer SP WHERE SP.name = :name", SoccerPlayer.class)
                .setParameter("name", name)
                .getResultList();
    }

}
