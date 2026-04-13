package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SetmealDishMapper {
    /*
    * 根据菜品ID查寻
    * */
    List<SetmealDish> selectByDishId(List<Long> ids);
    /*
    * 新增套餐，增加套餐菜品对应关系
    * */
    void insertBatch(List<SetmealDish> setmealDishes);
}
