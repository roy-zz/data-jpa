package com.roy.datajpa.repository.data.specification;

import com.roy.datajpa.domain.SoccerPlayer;
import com.roy.datajpa.domain.Team;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;

public class SoccerPlayerSpecification {

    public static Specification<SoccerPlayer> teamName(final String name) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(name)) {
                return null;
            }
            Join<SoccerPlayer, Team> team = root.join("team", JoinType.INNER);
            return criteriaBuilder.equal(team.get("name"), name);
        };
    }

    public static Specification<SoccerPlayer> greaterHeight(final int height) {
        return ((root, query, criteriaBuilder) -> criteriaBuilder.greaterThan(root.get("height"), height));
    }

    public static Specification<SoccerPlayer> greaterWeight(final int weight) {
        return ((root, query, criteriaBuilder) -> criteriaBuilder.greaterThan(root.get("weight"), weight));
    }

}
