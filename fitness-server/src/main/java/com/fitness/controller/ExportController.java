package com.fitness.controller;

import com.fitness.entity.*;
import com.fitness.mapper.*;
import com.fitness.utils.SecurityUtils;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 数据导出控制器
 * <p>
 * 支持将用户数据(体重、饮食、训练)导出为CSV格式文件，
 * 可按日期范围筛选，导出文件兼容Excel UTF-8编码。
 */
@RestController
@RequestMapping("/api/v1/export")
public class ExportController {

    private final WeightRecordMapper weightMapper;
    private final DietRecordMapper dietMapper;
    private final TrainingRecordMapper trainingMapper;

    public ExportController(WeightRecordMapper weightMapper,
                            DietRecordMapper dietMapper,
                            TrainingRecordMapper trainingMapper) {
        this.weightMapper = weightMapper;
        this.dietMapper = dietMapper;
        this.trainingMapper = trainingMapper;
    }

    /**
     * 导出CSV数据
     * <p>
     * 根据导出类型和日期范围，将用户数据以CSV格式写入HTTP响应流。
     * 响应头包含BOM标记以确保Excel正确识别UTF-8编码。
     *
     * @param type      导出类型: weight(体重) / diet(饮食) / training(训练)
     * @param startDate 开始日期 (yyyy-MM-dd格式)
     * @param endDate   结束日期 (yyyy-MM-dd格式)
     * @param response  HTTP响应对象，用于直接写入流
     * @throws Exception IO异常时抛出
     */
    @GetMapping("/{type}")
    public void export(@PathVariable("type") String type,
                       @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
                       @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
                       HttpServletResponse response) throws Exception {
        Long userId = SecurityUtils.getCurrentUserId();

        // 设置响应头，声明为CSV格式附件下载
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition",
                "attachment; filename=" + type + "_export.csv");

        // BOM(字节顺序标记)，确保Excel正确识别UTF-8编码
        response.getOutputStream().write(
                new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});

        PrintWriter writer = new PrintWriter(
                new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8));

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        switch (type) {
            case "weight":
                exportWeight(userId, startDate, endDate, writer, sdf);
                break;
            case "diet":
                exportDiet(userId, startDate, endDate, writer, sdf);
                break;
            case "training":
                exportTraining(userId, startDate, endDate, writer, sdf);
                break;
            default:
                writer.println("不支持的导出类型: " + type);
                break;
        }
        writer.flush();
    }

    /**
     * 导出体重数据CSV
     *
     * @param userId    用户ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param writer    输出Writer
     * @param sdf       日期格式化器
     */
    private void exportWeight(Long userId, Date startDate, Date endDate,
                               PrintWriter writer, SimpleDateFormat sdf) {
        // CSV表头
        writer.println("日期,体重(kg),BMI,备注");

        List<WeightRecord> weights = weightMapper.selectByUserAndDateRange(
                userId, startDate, endDate);
        for (WeightRecord w : weights) {
            String bmi = w.getBmi() != null ? String.format("%.1f", w.getBmi()) : "";
            String note = w.getNote() != null ? escapeCsv(w.getNote()) : "";
            writer.printf("%s,%.1f,%s,%s\n",
                    sdf.format(w.getRecordDate()), w.getWeightKg(), bmi, note);
        }
    }

    /**
     * 导出饮食数据CSV
     *
     * @param userId    用户ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param writer    输出Writer
     * @param sdf       日期格式化器
     */
    private void exportDiet(Long userId, Date startDate, Date endDate,
                             PrintWriter writer, SimpleDateFormat sdf) {
        // CSV表头
        writer.println("日期,餐次,食物,碳水(g),蛋白质(g),脂肪(g),热量(kcal)");

        List<DietRecord> diets = dietMapper.selectByUserAndDateRange(
                userId, startDate, endDate);
        for (DietRecord d : diets) {
            String foodName = d.getFoodName() != null ? escapeCsv(d.getFoodName()) : "";
            String mealType = d.getMealType() != null ? d.getMealType() : "";
            writer.printf("%s,%s,%s,%.1f,%.1f,%.1f,%.1f\n",
                    sdf.format(d.getRecordDate()), mealType, foodName,
                    d.getCarbGrams(), d.getProteinGrams(),
                    d.getFatGrams(), d.getCalories());
        }
    }

    /**
     * 导出训练数据CSV
     *
     * @param userId    用户ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param writer    输出Writer
     * @param sdf       日期格式化器
     */
    private void exportTraining(Long userId, Date startDate, Date endDate,
                                 PrintWriter writer, SimpleDateFormat sdf) {
        // CSV表头
        writer.println("日期,训练类型,时长(分钟),备注");

        List<TrainingRecord> trainings = trainingMapper.selectByUserAndDateRange(
                userId, startDate, endDate);
        for (TrainingRecord t : trainings) {
            String trainingType = t.getTrainingType() != null ? t.getTrainingType() : "";
            String duration = t.getDurationMinutes() != null
                    ? String.valueOf(t.getDurationMinutes()) : "";
            String note = t.getNote() != null ? escapeCsv(t.getNote()) : "";
            writer.printf("%s,%s,%s,%s\n",
                    sdf.format(t.getRecordDate()), trainingType, duration, note);
        }
    }

    /**
     * CSV字段转义
     * <p>
     * 如果字段值包含逗号、双引号或换行符，需要用双引号包裹并转义内部双引号。
     *
     * @param value 原始字段值
     * @return 转义后的CSV安全值
     */
    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
