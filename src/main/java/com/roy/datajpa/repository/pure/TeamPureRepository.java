package com.roy.datajpa.repository.pure;

import com.roy.datajpa.domain.SoccerPlayer;
import com.roy.datajpa.domain.Team;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class TeamPureRepository {
    @PersistenceContext
    private EntityManager entityManager;

    public Team save(Team team) {
        entityManager.persist(team);
        return team;
    }

    public List<Team> saveAll(List<Team> teams) {
        List<Team> answer = new ArrayList<>();
        teams.forEach(team -> answer.add(save(team)));
        return answer;
    }

    public void remove(Team team) {
        entityManager.remove(team);
    }

    public List<Team> findAll() {
        return entityManager.createQuery(
                "SELECT T FROM Team T", Team.class)
                .getResultList();
    }

    public Optional<Team> findOptionalById(Long id) {
        return Optional.ofNullable(findEntityById(id));
    }

    public Team findEntityById(Long id) {
        return entityManager.find(Team.class, id);
    }

    public long count() {
        return entityManager.createQuery("SELECT COUNT(T) FROM Team T", Long.class)
                .getSingleResult();
    }

}
