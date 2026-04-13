package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealMapper {

    /*
    * 根据分类ID查套餐数目
    * */
    @Select("select count(*) from setmeal where category_id = #{categoryId}")
    public Integer getCountByCategoryId(Long categoryId);
    /*
     *  批量停售
     * */
    @AutoFill(OperationType.UPDATE)
    void update(Setmeal setmeal);
    /*
    * 插入菜品
    * */
    @AutoFill(OperationType.INSERT)
    void insert(Setmeal setmeal);
}
