package com.roy.datajpa.repository.data.projection;

import lombok.Getter;

@Getter
public class ExcludeIdProjectionDTO {
    private String name;
    private int height;
    private int weight;
    public ExcludeIdProjectionDTO(String name, int height, int weight) {
        this.name = name;
        this.height = height;
        this.weight = weight;
    }
}
