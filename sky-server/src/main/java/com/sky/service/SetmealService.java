package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;

import java.util.List;

public interface SetmealService {
    /*
    * 新增套餐
    * */
    void save(SetmealDTO setmealDTO);
    /*
    * 分页查询
    * */
    PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);
    /*
    * 删除套餐
    * */
    void delete(List<Long> ids);
}
