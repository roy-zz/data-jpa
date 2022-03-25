package com.roy.datajpa.repository.pure;

import com.roy.datajpa.domain.SoccerPlayer;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
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

    public List<SoccerPlayer> saveAll(List<SoccerPlayer> soccerPlayers) {
        List<SoccerPlayer> answer = new ArrayList<>();
        soccerPlayers.forEach(soccerPlayer -> answer.add(save(soccerPlayer)));
        return answer;
    }

    public void delete(SoccerPlayer soccerPlayer) {
        entityManager.remove(soccerPlayer);
    }

    public void deleteAll(List<SoccerPlayer> soccerPlayers) {
        soccerPlayers.forEach(this::delete);
    }

    public List<SoccerPlayer> findAll() {
        return entityManager.createQuery(
                "SELECT SC FROM SoccerPlayer SC", SoccerPlayer.class)
                .getResultList();
    }

    public Optional<SoccerPlayer> findOptionalById(Long id) {
        return Optional.ofNullable(findEntityById(id));
    }

    public SoccerPlayer findEntityById(Long id) {
        return  entityManager.find(SoccerPlayer.class, id);
    }

    public long count() {
        return entityManager.createQuery("SELECT COUNT(SC) FROM SoccerPlayer SC", Long.class)
                .getSingleResult();
    }

    public List<SoccerPlayer> findByNameAndHeightGreaterThan(String name, int height) {
        return entityManager.createQuery(
                "SELECT SP " +
                        "FROM SoccerPlayer SP " +
                        "WHERE " +
                        "   SP.name = :name " +
                        "   AND SP.height >= :height", SoccerPlayer.class)
                .setParameter("name", name)
                .setParameter("height", height)
                .getResultList();
    }

    public List<SoccerPlayer> findByNameUsingNamedQuery(String name) {
        return entityManager.createNamedQuery("SoccerPlayer.findByName", SoccerPlayer.class)
                .setParameter("name", name)
                .getResultList();
    }

    public List<SoccerPlayer> findByHeightGreaterThanUsingNamedQuery(int height) {
        return entityManager.createNamedQuery("SoccerPlayer.findByHeightGreaterThan", SoccerPlayer.class)
                .setParameter("height", height)
                .getResultList();
    }

    public Page<SoccerPlayer> findAllPage(int page, int size) {
        int offset = size * page;
        List<SoccerPlayer> content = entityManager.createQuery(
                "SELECT SP " +
                        "FROM SoccerPlayer SP " +
                        "ORDER BY SP.height DESC ", SoccerPlayer.class)
                .setFirstResult(offset)
                .setMaxResults(size)
                .getResultList();

        long totalCount = entityManager.createQuery(
                "SELECT COUNT(SP) " +
                        "FROM SoccerPlayer SP " +
                        "ORDER BY SP.height DESC ", Long.class)
                .getSingleResult();
        Sort sort = Sort.by(Sort.Direction.DESC, "height");
        Pageable pageable = PageRequest.of(page, size, sort);

        return new PageImpl<>(content, pageable, totalCount);
    }

}
