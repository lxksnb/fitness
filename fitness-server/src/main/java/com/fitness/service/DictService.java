package com.fitness.service;

import com.fitness.entity.SysDictData;
import com.fitness.mapper.SysDictDataMapper;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * 字典服务
 * 提供系统数据字典的查询功能
 */
@Service
public class DictService {

    private final SysDictDataMapper dictDataMapper;

    public DictService(SysDictDataMapper dictDataMapper) {
        this.dictDataMapper = dictDataMapper;
    }

    /**
     * 根据字典编码获取字典数据列表
     * @param dictCode 字典编码
     * @return 字典数据列表
     */
    public List<SysDictData> getByCode(String dictCode) {
        return dictDataMapper.selectByDictCode(dictCode);
    }
}
