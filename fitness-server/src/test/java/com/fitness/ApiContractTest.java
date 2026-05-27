package com.fitness;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.common.Result;
import com.fitness.controller.UploadController;
import com.fitness.dto.ActionDTO;
import com.fitness.dto.FoodCreateDTO;
import com.fitness.dto.PlanCreateDTO;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ApiContractTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void uploadReturnsUrlObjectWithPublicUploadsPath() throws Exception {
        Path uploadDir = Files.createTempDirectory("fitness-upload-test");
        UploadController controller = new UploadController();
        ReflectionTestUtils.setField(controller, "uploadPath", uploadDir.toString());

        MockMultipartFile file = new MockMultipartFile(
            "file",
            "meal.png",
            "image/png",
            new byte[] {1, 2, 3}
        );

        Result<?> result = controller.upload(file);

        assertEquals(200, result.getCode());
        assertTrue(result.getData() instanceof Map);
        Object url = ((Map<?, ?>) result.getData()).get("url");
        assertTrue(url instanceof String);
        assertTrue(((String) url).startsWith("/uploads/"));
        assertTrue(((String) url).endsWith(".png"));
    }

    @Test
    void actionDtoAcceptsFrontendImageUrlArrays() throws Exception {
        ActionDTO dto = mapper.readValue(
            "{\"actionName\":\"卧推\",\"imageUrls\":[\"/uploads/a.png\",\"/uploads/b.png\"]}",
            ActionDTO.class
        );

        assertEquals("[\"/uploads/a.png\",\"/uploads/b.png\"]", dto.getImageUrls());
    }

    @Test
    void planTrainingDayAcceptsMultipleTrainingTypes() throws Exception {
        PlanCreateDTO dto = mapper.readValue(
            "{\"trainingDays\":[{\"dayType\":\"TRAINING\",\"trainingType\":[\"CHEST\",\"BACK\"]}]}",
            PlanCreateDTO.class
        );

        assertEquals("CHEST,BACK", dto.getTrainingDays().get(0).getTrainingType());
    }

    @Test
    void foodCreateDtoAcceptsCategoryAndEdibleWeight() throws Exception {
        FoodCreateDTO dto = mapper.readValue(
            "{\"foodName\":\"香蕉\",\"categoryType\":\"FRUIT\",\"nutritions\":[{\"unitType\":\"PER_ROOT\",\"servingWeightG\":150,\"edibleWeightG\":100,\"carbGrams\":22.8,\"proteinGrams\":1.1,\"fatGrams\":0.3}]}",
            FoodCreateDTO.class
        );

        assertEquals("FRUIT", dto.getCategoryType());
        assertEquals(100.0, dto.getNutritions().get(0).getEdibleWeightG());
    }
}
