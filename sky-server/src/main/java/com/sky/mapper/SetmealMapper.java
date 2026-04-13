package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.enumeration.OperationType;
import com.sky.vo.SetmealVO;
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
    /*
    * 分页查询
    * */
    Page<SetmealVO> pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

    /*
    * 根据ID查询套餐
    * */
    @Select("select * from setmeal where id = #{id}")
    public Setmeal selectById(Long id);
    /*
    * 批量删除套餐
    * */
    void deleteBatch(List<Long> ids);
}
