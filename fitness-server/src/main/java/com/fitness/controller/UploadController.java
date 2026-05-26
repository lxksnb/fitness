package com.fitness.controller;

import com.fitness.common.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Date;
import java.util.UUID;

/**
 * 文件上传控制器
 * <p>
 * 提供通用文件上传接口，支持图片等文件上传。
 * 文件按日期分目录存储，使用UUID命名避免冲突。
 */
@RestController
@RequestMapping("/api/v1")
public class UploadController {

    /** 上传文件存储根目录，可通过配置 upload.path 自定义 */
    @Value("${upload.path:uploads}")
    private String uploadPath;

    /**
     * 上传文件
     * <p>
     * 接收multipart文件，生成唯一文件名后保存到日期子目录。
     *
     * @param file 上传的文件
     * @return 成功时返回文件的相对访问URL
     * @throws IOException 文件保存失败时抛出
     */
    @PostMapping("/upload")
    public Result<Map<String, String>> upload(@RequestParam("file") MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return Result.fail(com.fitness.common.ResultCode.BAD_REQUEST, "上传文件不能为空");
        }

        // 获取原始文件名和扩展名
        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf("."));
        }

        // 生成唯一文件名: 日期目录 + UUID + 扩展名
        String dateDir = new SimpleDateFormat("yyyy/MM/dd").format(new Date());
        String fileName = UUID.randomUUID().toString() + ext;

        // 创建目录并保存文件
        Path rootDir = Paths.get(uploadPath).toAbsolutePath().normalize();
        Path dir = rootDir.resolve(Paths.get(dateDir)).normalize();
        if (!dir.startsWith(rootDir)) {
            return Result.fail(com.fitness.common.ResultCode.BAD_REQUEST, "上传路径非法");
        }
        Files.createDirectories(dir);

        Path dest = dir.resolve(fileName).normalize();
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, dest, StandardCopyOption.REPLACE_EXISTING);
        }

        // 返回相对路径，前端可直接拼接域名访问
        String url = "/uploads/" + dateDir + "/" + fileName;
        Map<String, String> data = new HashMap<>();
        data.put("url", url);
        return Result.ok(data);
    }
}
