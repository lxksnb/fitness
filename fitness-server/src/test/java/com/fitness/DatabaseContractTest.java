package com.fitness;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseContractTest {

    @Test
    void trainingTimesUseTimeColumnsInInitialSchema() throws Exception {
        String sql = read("src/main/resources/db/init.sql");

        assertTrue(sql.matches("(?s).*start_time\\s+TIME\\b.*"), "start_time should be TIME");
        assertTrue(sql.matches("(?s).*end_time\\s+TIME\\b.*"), "end_time should be TIME");
    }

    @Test
    void dietInsertPersistsRecordedAtFromService() throws Exception {
        String mapper = read("src/main/resources/mapper/DietRecordMapper.xml");

        assertTrue(mapper.contains("recorded_at"), "diet insert should include recorded_at");
        assertTrue(mapper.contains("#{recordedAt}"), "diet insert should bind recordedAt");
    }

    private static String read(String path) throws Exception {
        return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
    }
}
