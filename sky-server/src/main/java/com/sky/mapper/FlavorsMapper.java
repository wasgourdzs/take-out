package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface FlavorsMapper {
    /*
    * 插入口味
    * */
    void insertBatch(List<DishFlavor> flavors);
    /*
    * 通过菜品ID删除口味(批量删)
    * */
    void deleteByDishIds(List<Long> ids);
    /*
    * 通过菜品ID查找口味数据
    * */
    @Select("select * from dish_flavor where dish_id = #{dishId}")
    List<DishFlavor> selectByDishId(Long dishId);

    @Delete("delete from dish_flavor where dish_id = #{dishId};")
    void deleteByDishId(Long dishId);
}
