package com.roy.datajpa.repository.data;

import com.roy.datajpa.domain.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeamDataRepository extends JpaRepository<Team, Long> {
}
