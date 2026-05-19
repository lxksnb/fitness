package com.fitness.service;

import com.fitness.entity.SysDictData;
import com.fitness.mapper.SysDictDataMapper;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DictService {

    private final SysDictDataMapper dictDataMapper;

    public DictService(SysDictDataMapper dictDataMapper) {
        this.dictDataMapper = dictDataMapper;
    }

    public List<SysDictData> getByCode(String dictCode) {
        return dictDataMapper.selectByDictCode(dictCode);
    }
}
