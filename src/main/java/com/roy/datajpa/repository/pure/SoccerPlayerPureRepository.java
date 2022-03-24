package com.roy.datajpa.repository.pure;

import com.roy.datajpa.domain.SoccerPlayer;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;

@Repository
public class SoccerPlayerPureRepository {
    @PersistenceContext
    private EntityManager entityManager;

    public SoccerPlayer save(SoccerPlayer soccerPlayer) {
        entityManager.persist(soccerPlayer);
        return soccerPlayer;
    }

    public void delete(SoccerPlayer soccerPlayer) {
        entityManager.remove(soccerPlayer);
    }

    public List<SoccerPlayer> findAll() {
        return entityManager.createQuery(
                "SELECT SC FROM SoccerPlayer SC", SoccerPlayer.class)
                .getResultList();
    }

    public Optional<SoccerPlayer> findOptionalById(Long id) {
        return Optional.of(entityManager.find(SoccerPlayer.class, id));
    }

    public SoccerPlayer findEntityById(Long id) {
        return  entityManager.find(SoccerPlayer.class, id);
    }

    public long count() {
        return entityManager.createQuery("SELECT COUNT(SC) FROM SoccerPlayer SC", Long.class)
                .getSingleResult();
    }

}
