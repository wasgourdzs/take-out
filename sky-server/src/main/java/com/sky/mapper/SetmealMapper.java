package com.sky.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SetmealMapper {

    /*
    * 根据分类ID查套餐数目
    * */
    @Select("select count(*) from setmeal where category_id = #{categoryId}")
    public Integer getCountByCategoryId(Long categoryId);
}
