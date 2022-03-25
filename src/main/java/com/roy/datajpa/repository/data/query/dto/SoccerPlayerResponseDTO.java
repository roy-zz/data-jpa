package com.roy.datajpa.repository.data.query.dto;

import com.roy.datajpa.domain.SoccerPlayer;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SoccerPlayerResponseDTO {
    private String name;
    private int height;
    private int weight;
    public static SoccerPlayerResponseDTO of(SoccerPlayer entity) {
        return new SoccerPlayerResponseDTO(entity.getName(), entity.getHeight(), entity.getWeight());
    }
}
