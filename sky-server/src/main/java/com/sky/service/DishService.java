package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

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
    /*
    * 根据ID查询
    * */
    DishVO selectByIdWithFlavor(Long id);

    /*
    * 更改菜品
    * */
    void updateWithFlavor(DishDTO dishDTO);
    /*
    * 菜品起售停售
    * */
    void startOrStop(Integer status, Long id);
}
