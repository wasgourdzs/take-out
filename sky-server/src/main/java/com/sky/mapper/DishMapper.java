package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DishMapper {
    /*
     * 根据分类ID查菜品数目
     */
    @Select("select count(*) from dish where category_id = #{categoryId}")
    public Integer getCountByCategory(Long categoryId);

    /*
     * 新增菜品
     * */
    @AutoFill(OperationType.INSERT)
    void insert(Dish dish);
}
