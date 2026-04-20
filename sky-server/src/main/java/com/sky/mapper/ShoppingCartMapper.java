package com.sky.mapper;

import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {

    /*
    * 查看购物车
    * */
    List<ShoppingCart> list (ShoppingCart shoppingCart);
    /*
    * 更新购物车中数据的份数（添加 或 删除）
    * */
    @Update("update shopping_cart set number = #{number} where id = #{id}")
    void updateNumberById(ShoppingCart shoppingCart);
    /*
     * 添加购物车
     * */
    @Insert("insert into shopping_cart (name, image, user_id, dish_id, setmeal_id, dish_flavor, number, amount, create_time) " +
            "values (#{name}, #{image}, #{userId}, #{dishId}, #{setmealId}, #{dishFlavor}, #{number}, #{amount}, #{createTime})")
    void insert(ShoppingCart shoppingCart);

    /*
    * 根据用户ID删除数据
    * */
    @Delete("delete from shopping_cart where user_id = #{userId}")
    void delete(Long userId);
    /*
    * 删除购物车内容
    * */
    @Delete("delete  from shopping_cart where id = #{id}")
    void deleteById(Long id);
}
