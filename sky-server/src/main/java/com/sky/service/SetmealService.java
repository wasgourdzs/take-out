package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.vo.SetmealVO;

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
    /*
    * 根据ID查询
    * */
    SetmealVO selectById(Long id);
    /*
    * 修改套餐
    * */
    void update(SetmealDTO setmealDTO);
}
