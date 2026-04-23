package com.sky.service;

import com.sky.dto.*;
import com.sky.entity.Orders;
import com.sky.result.PageResult;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;

import java.util.List;

public interface OrderService {
    /*
    * 用户下单
    * */
    OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO);

    /**
     * 订单支付
     * @param ordersPaymentDTO
     * @return
     */
    OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception;

    /**
     * 支付成功，修改订单状态
     * @param outTradeNo
     */
    void paySuccess(String outTradeNo);
    /*
    * 查询历史订单（分页查询）
    * */
    PageResult page(OrdersPageQueryDTO ordersPageQueryDTO);
    /*
    * 查订单详情（根据订单ID查）
    * */
    OrderVO list(Long id);
    /*
    * 用户取消订单（修改订单状态为已取消）
    * */
    void cancel(Long id);
    /*
    * 再来一单（将订单再次加到购物车）
    * */
    void repetition(Long id);
    /*
    * 管理端条件查询
    * */
    PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO);
    /*
    * 统计各类订单总数
    * */
    OrderStatisticsVO statistics();
    /*
    * 商家接单
    * */
    void confirm(OrdersConfirmDTO ordersConfirmDTO);
    /*
    * 商家拒单
    * */
    void rejection(OrdersRejectionDTO ordersRejectionDTO);
    /*
    * 商家取消订单
    * */
    void cancelByAdmin(OrdersCancelDTO ordersCancelDTO);
    /*
    * 派送订单
    * */
    void delivery(Long id);
    /*
    * 完成订单
    * */
    void complete(Long id);
}
