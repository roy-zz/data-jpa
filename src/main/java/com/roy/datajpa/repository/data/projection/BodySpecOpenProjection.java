package com.roy.datajpa.repository.data.projection;

import org.springframework.beans.factory.annotation.Value;

public interface BodySpecOpenProjection {

    @Value("#{'키:' + target.height + ', 몸무게: ' + target.weight}")
    String getBodySpec();

}
