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
import java.util.Map;

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
    /*
    * 根据分类ID查
    * */
    List<Dish> list(Dish dish);
    /*
    * 根据套餐id查菜品
    * */
    @Select("select d.* from dish as d left join setmeal_dish as s on d.id = s.dish_id where s.setmeal_id = #{id}")
    List<Dish> selectBySetmealId(Long id);

    /**
     * 根据条件统计菜品数量
     * @param map
     * @return
     */
    Integer countByMap(Map map);
}
