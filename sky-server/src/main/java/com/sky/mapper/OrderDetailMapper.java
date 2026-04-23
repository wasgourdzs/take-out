package com.sky.mapper;

import com.sky.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OrderDetailMapper {
    /*
    * 向订单详细表批量插入数据
    * */
    void insert(List<OrderDetail> orderDetails);
    /*
    * 根据订单ID查询订单详情
    * */
    @Select("select * from order_detail where order_id = #{id}")
    List<OrderDetail> selectByOrdersId(Long id);
}
