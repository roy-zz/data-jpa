package com.roy.datajpa.repository.data.projection;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class NativeProjectionDTO {
    private Long id;
    private String name;
    private int height;
    private int weight;

    public NativeProjectionDTO(Long id, String name, int height, int weight) {
        this.id = id;
        this.name = name;
        this.height = height;
        this.weight = weight;
    }

}
