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

    @Test
    void foodSchemaSupportsCategoryAndEdibleWeightUnits() throws Exception {
        String sql = read("src/main/resources/db/init.sql");

        assertTrue(sql.matches("(?s).*category_type\\s+VARCHAR\\(50\\).*"), "food_library should include category_type");
        assertTrue(sql.matches("(?s).*edible_weight_g\\s+DECIMAL\\(6,1\\).*"), "food_nutrition should include edible_weight_g");
        assertTrue(sql.contains("('food_category', '食物分类')"), "food_category dict type should be initialized");
        assertTrue(sql.contains("'每根', 'PER_ROOT'"), "food_unit_type should include per-root units");
        assertTrue(sql.contains("'香蕉'") && sql.contains("'FRUIT'"), "initial foods should include fruit classification");
    }

    @Test
    void foodSchemaMigrationSupportsExistingDatabases() throws Exception {
        String sql = read("src/main/resources/db/migration_food_category_nutrition_weight.sql");

        assertTrue(sql.contains("ALTER TABLE food_library ADD COLUMN category_type"), "migration should add food category column");
        assertTrue(sql.contains("ALTER TABLE food_nutrition ADD COLUMN edible_weight_g"), "migration should add edible weight column");
        assertTrue(sql.contains("information_schema.COLUMNS"), "migration should be safe to rerun for existing columns");
        assertTrue(sql.contains("INSERT IGNORE INTO sys_dict_type"), "migration should not duplicate dict types");
        assertTrue(sql.contains("NOT EXISTS (SELECT 1 FROM food_nutrition"), "migration should not duplicate sample nutrition units");
    }

    private static String read(String path) throws Exception {
        return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
    }
}
