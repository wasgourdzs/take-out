package com.sky.service;

import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;

public interface CategoryService {

    /*
    * 分页查询
    * */
    PageResult page(CategoryPageQueryDTO categoryPageQueryDTO);
    /*
    * 根据类型查询
    * */
    Category list(Integer type);
    /*
    * 新增分类
    * */
    void insert(CategoryDTO categoryDTO);
    /*
    * 更改分类状态
    * */
    void startOrStop(Integer status, Long id);
    /*
    * 更新数据
    * */
    void update(CategoryDTO categoryDTO);
    /*
    * 删除数据
    * */
    void delete(Long id);
}
