package com.roy.datajpa.repository.data;

import com.roy.datajpa.domain.SoccerPlayer;
import com.roy.datajpa.repository.data.custom.SoccerPlayerDataRepositoryCustom;
import com.roy.datajpa.repository.data.query.dto.SoccerPlayerResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import javax.persistence.LockModeType;
import javax.persistence.QueryHint;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface SoccerPlayerDataRepository extends
        JpaRepository<SoccerPlayer, Long>,
        SoccerPlayerDataRepositoryCustom,
        JpaSpecificationExecutor<SoccerPlayer> {

    List<SoccerPlayer> findByNameAndHeightGreaterThan(String name, int height);

    List<SoccerPlayer> findByNameAndHeightGreaterThanAndWeightLessThan(String name, int height, int weight);

    @Query(name = "SoccerPlayer.findByName")
    List<SoccerPlayer> findByNameUsingNamedQuery(@Param("name") String name);

    @Query(name = "SoccerPlayer.findByHeightGreaterThan")
    List<SoccerPlayer> findByHeightGreaterThanUsingNamedQuery(@Param("height") int height);

    @Query(value = "SELECT SP FROM SoccerPlayer SP")
    List<SoccerPlayer> findEntityAllUsingQueryAnnotation();

    @Query(value =
            "SELECT new com.roy.datajpa.repository.data.query.dto.SoccerPlayerResponseDTO " +
            "(SP.name, SP.height, SP.weight) " +
            "FROM SoccerPlayer SP")
    List<SoccerPlayerResponseDTO> findDTOAllUsingQueryAnnotation();

    @Query(value =
            "SELECT SP " +
            "FROM SoccerPlayer SP " +
            "WHERE " +
            "   SP.name = ?1 " +
            "   AND SP.height > ?2 ")
    List<SoccerPlayer> findByNameAndHeightWithPositionBaseBinding(String name, int height);

    @Query(value =
            "SELECT SP " +
            "FROM SoccerPlayer SP " +
            "WHERE " +
            "   SP.name = :name " +
            "   AND SP.height > :height ")
    List<SoccerPlayer> findByNameAndHeightWithNameBaseBinding(String name, int height);

    @Query(value =
            "SELECT SP " +
            "FROM SoccerPlayer SP " +
            "WHERE " +
            "   SP.id IN :ids ")
    List<SoccerPlayer> findByIdIn(Set<Long> ids);

    SoccerPlayer findOneByName(String name);

    Optional<SoccerPlayer> findOptionalOneByName(String name);

    Page<SoccerPlayer> findPageByNameIsNotNull(Pageable pageable);

    @Query(value = "SELECT SP " +
                   "FROM SoccerPlayer SP " +
                   "        LEFT JOIN SP.team T " +
                   "WHERE SP.name IS NOT NULL",
           countQuery = "SELECT SP FROM SoccerPlayer SP")
    Page<SoccerPlayer> findCustomPageByNameIsNotNull(Pageable pageable);

    Slice<SoccerPlayer> findSliceByNameIsNotNull(Pageable pageable);

    @Modifying(clearAutomatically = true)
    @Query(value =
            "UPDATE SoccerPlayer SP " +
            "SET SP.weight = SP.weight + 10 " +
            "WHERE SP.height > :height")
    int bulkUpdate(int height);

    @Override
    @EntityGraph(attributePaths = {"team"})
    List<SoccerPlayer> findAll();

    @EntityGraph(attributePaths = {"team"})
    @Query("SELECT SP FROM SoccerPlayer SP")
    List<SoccerPlayer> findAllUsingJpqlEntityGraph();

    @EntityGraph(attributePaths = {"team"})
    List<SoccerPlayer> findAllByName(String name);

    SoccerPlayer findUpdatableByName(String name);

    @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
    SoccerPlayer findReadOnlyByName(String name);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<SoccerPlayer> findUsingLockByName(String name);

}
