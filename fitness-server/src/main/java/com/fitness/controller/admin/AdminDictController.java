package com.fitness.controller.admin;

import com.fitness.common.Result;
import com.fitness.entity.SysDictType;
import com.fitness.entity.SysDictData;
import com.fitness.mapper.SysDictTypeMapper;
import com.fitness.mapper.SysDictDataMapper;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员字典管理控制器
 * 管理系统字典类型和字典数据
 */
@RestController
@RequestMapping("/api/v1/admin/dict")
public class AdminDictController {

    private final SysDictTypeMapper dictTypeMapper;
    private final SysDictDataMapper dictDataMapper;

    public AdminDictController(SysDictTypeMapper dictTypeMapper, SysDictDataMapper dictDataMapper) {
        this.dictTypeMapper = dictTypeMapper;
        this.dictDataMapper = dictDataMapper;
    }

    // ===== 字典类型管理 =====

    /** 获取所有字典类型 */
    @GetMapping("/types")
    public Result<List<SysDictType>> listTypes() {
        return Result.ok(dictTypeMapper.selectAll());
    }

    /** 新增字典类型 */
    @PostMapping("/types")
    public Result<?> createType(@RequestBody SysDictType dictType) {
        dictTypeMapper.insert(dictType);
        return Result.ok();
    }

    /** 更新字典类型 */
    @PutMapping("/types/{id}")
    public Result<?> updateType(@PathVariable Long id, @RequestBody SysDictType dictType) {
        dictType.setId(id);
        dictTypeMapper.updateById(dictType);
        return Result.ok();
    }

    /** 删除字典类型 */
    @DeleteMapping("/types/{id}")
    public Result<?> deleteType(@PathVariable Long id) {
        dictTypeMapper.deleteById(id);
        return Result.ok();
    }

    // ===== 字典数据管理 =====

    /** 获取某字典类型下的所有数据 */
    @GetMapping("/types/{typeId}/data")
    public Result<List<SysDictData>> listData(@PathVariable Long typeId) {
        return Result.ok(dictDataMapper.selectByTypeId(typeId));
    }

    /** 新增字典数据 */
    @PostMapping("/types/{typeId}/data")
    public Result<?> createData(@PathVariable Long typeId, @RequestBody SysDictData dictData) {
        dictData.setDictTypeId(typeId);
        dictDataMapper.insert(dictData);
        return Result.ok();
    }

    /** 更新字典数据 */
    @PutMapping("/data/{id}")
    public Result<?> updateData(@PathVariable Long id, @RequestBody SysDictData dictData) {
        dictData.setId(id);
        dictDataMapper.updateById(dictData);
        return Result.ok();
    }

    /** 删除字典数据 */
    @DeleteMapping("/data/{id}")
    public Result<?> deleteData(@PathVariable Long id) {
        dictDataMapper.deleteById(id);
        return Result.ok();
    }
}
