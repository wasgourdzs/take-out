package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;

import java.util.List;

public interface DishService {

    /*
    * 新增菜品
    * */
    public void saveWithFlavors(DishDTO dishDTO);
    /*
    * 分页查询
    * */
    PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO);
    /*
    * 批量删除
    * */
    void delete(List<Long> ids);
}
