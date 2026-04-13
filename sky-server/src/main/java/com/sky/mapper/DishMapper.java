package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

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
    /*
    * 分页查询
    * */
    Page<DishVO> pageQuery(DishPageQueryDTO dishPageQueryDTO);
    /*
    * 通过ID查询菜品(批量查)
    * */
    List<Dish> selectByIds(List<Long> ids);
    /*
     *  通过ID查菜品（单独查）
     * */
    @Select("select * from dish where id = #{id}")
    Dish selectById(Long id);
    /*
    * 通过ID删除菜品
    * */
    void deleteById(List<Long> ids);
    /*
    * 更改菜品信息
    * */
    @AutoFill(OperationType.UPDATE)
    void update(Dish dish);
}
