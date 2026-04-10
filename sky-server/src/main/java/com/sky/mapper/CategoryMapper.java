package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CategoryMapper {

    /*
     * 分页查询
     * */
    public Page<Category> pageQuery(CategoryPageQueryDTO categoryPageQueryDTO);
    /*
    * 根据类型查询
    * */
    @Select("select * from category where type = #{type}")
    Category selectByType(Integer type);
    /*
    * 插入分类
    * */
    @Insert("insert into category (type, name, sort, status, create_time, update_time, create_user, update_user) " +
            "values (#{type}, #{name}, #{sort}, #{status}, #{createTime}, #{updateTime}, #{createUser}, #{updateUser})")
    void insert(Category category);

    void update(Category category);

    @Delete("delete from category where id = #{id}")
    void delete(Long id);
}
