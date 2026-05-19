package com.fitness.controller;

import com.fitness.common.Result;
import com.fitness.entity.SysDictData;
import com.fitness.service.DictService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 字典控制器
 * 提供数据字典查询接口，前端可根据字典编码获取下拉选项等数据
 */
@RestController
@RequestMapping("/api/v1/dict")
public class DictController {

    private final DictService dictService;

    public DictController(DictService dictService) {
        this.dictService = dictService;
    }

    /**
     * 根据字典编码查询字典数据
     * @param dictCode 字典编码（如 gender、meal_type）
     * @return 字典数据列表
     */
    @GetMapping("/{dictCode}")
    public Result<List<SysDictData>> getDict(@PathVariable String dictCode) {
        return Result.ok(dictService.getByCode(dictCode));
    }
}
