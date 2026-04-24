package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OrderMapper {
    /*
    * 向订单表插入数据
    * */
    void insert(Orders orders);

    /**
     * 根据订单号查询订单
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     * @param orders
     */
    void update(Orders orders);

    @Update("update orders set status = #{orderStatus},pay_status = #{orderPaidStatus} ,checkout_time = #{check_out_time} where id = #{id}")
    void updateStatus(Integer orderStatus, Integer orderPaidStatus, LocalDateTime check_out_time, Long id);
    /*
    * 分页查询订单
    * */
    Page<Orders> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);
    /*
    * 根据订单ID查订单
    * */
    @Select("select * from orders where id = #{id}")
    Orders getById(Long id);
    /*
    * 查订单表全部内容
    * */
    @Select("select * from orders")
    List<Orders> list();

    /*
    * 查订单表有没有超时订单(未支付、未完成)
    * */
    @Select("select * from orders where status = #{status} and order_time < #{localDateTime}")
    List<Orders> getByStatusAndOrderTime (Integer status, LocalDateTime localDateTime);
}
