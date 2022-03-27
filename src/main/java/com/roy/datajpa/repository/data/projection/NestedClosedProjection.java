package com.roy.datajpa.repository.data.projection;

public interface NestedClosedProjection {
    String getName();
    int getHeight();
    int getWeight();
    TeamInfo getTeam();

    interface TeamInfo {
        String getName();
    }
}
