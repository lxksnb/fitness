package com.fitness.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fitness.dto.TrainingRecordDTO;
import com.fitness.entity.TrainingRecord;
import com.fitness.entity.WeightRecord;
import org.junit.jupiter.api.Test;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JacksonContractTest {

    private final ObjectMapper mapper = createMapper();

    @Test
    void serializesDateOnlyFieldsAsYyyyMmDd() throws Exception {
        WeightRecord record = new WeightRecord();
        record.setRecordDate(new SimpleDateFormat("yyyy-MM-dd").parse("2026-05-22"));

        String json = mapper.writeValueAsString(record);

        assertTrue(json.contains("\"recordDate\":\"2026-05-22\""), json);
    }

    @Test
    void serializesTrainingTimesAsHhMmSs() throws Exception {
        TrainingRecord record = new TrainingRecord();
        record.setStartTime(Time.valueOf("09:30:00"));
        record.setEndTime(Time.valueOf("10:45:00"));

        String json = mapper.writeValueAsString(record);

        assertTrue(json.contains("\"startTime\":\"09:30:00\""), json);
        assertTrue(json.contains("\"endTime\":\"10:45:00\""), json);
    }

    @Test
    void parsesFrontendTrainingDateAndTimePayload() throws Exception {
        String json = "{"
                + "\"recordDate\":\"2026-05-22\","
                + "\"trainingType\":\"CHEST\","
                + "\"startTime\":\"09:30:00\","
                + "\"endTime\":\"10:45:00\""
                + "}";

        TrainingRecordDTO dto = mapper.readValue(json, TrainingRecordDTO.class);

        assertEquals("2026-05-22", new SimpleDateFormat("yyyy-MM-dd").format(dto.getRecordDate()));
        assertEquals(Time.valueOf("09:30:00"), dto.getStartTime());
        assertEquals(Time.valueOf("10:45:00"), dto.getEndTime());
    }

    private static ObjectMapper createMapper() {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Date.class, new MultiDateFormatDeserializer());
        module.addSerializer(Long.class, ToStringSerializer.instance);
        module.addSerializer(Long.TYPE, ToStringSerializer.instance);

        return JsonMapper.builder()
                .defaultDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"))
                .defaultTimeZone(TimeZone.getTimeZone("Asia/Shanghai"))
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .addModule(module)
                .build();
    }
}
