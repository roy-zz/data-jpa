package com.roy.datajpa.repository.data;

import com.roy.datajpa.domain.Director;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;

@SpringBootTest
class DirectorDataRepositoryTest {

    @Autowired
    private DirectorDataRepository dataRepository;

    @Test
    @Rollback(value = false)
    @DisplayName("감독 저장 테스트")
    void saveDirectorTest() {
        Director director = new Director();
        dataRepository.save(director);
    }

}