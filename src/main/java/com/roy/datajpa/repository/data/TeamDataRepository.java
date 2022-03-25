package com.roy.datajpa.repository.data;

import com.roy.datajpa.domain.Team;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamDataRepository extends JpaRepository<Team, Long> {
}
