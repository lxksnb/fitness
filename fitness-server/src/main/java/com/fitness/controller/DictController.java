package com.fitness.controller;

import com.fitness.common.Result;
import com.fitness.entity.SysDictData;
import com.fitness.service.DictService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/dict")
public class DictController {

    private final DictService dictService;

    public DictController(DictService dictService) {
        this.dictService = dictService;
    }

    @GetMapping("/{dictCode}")
    public Result<List<SysDictData>> getDict(@PathVariable String dictCode) {
        return Result.ok(dictService.getByCode(dictCode));
    }
}
