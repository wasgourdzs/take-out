package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class OrderTask {

    @Autowired
    private OrderMapper orderMapper;

    /*
    * 处理支付超时的订单（每分钟检查一次）
    * */
    @Scheduled(cron = "0 0/1 * * * *")
    public void processOutOfTime () {
        log.info("处理超时订单时间：{}", LocalDateTime.now());
        //查订单表，查找状态为待支付，创建时间超过15分钟的
        LocalDateTime outTime = LocalDateTime.now().plusMinutes(-15);
        List<Orders> orders = orderMapper.getByStatusAndOrderTime(Orders.UN_PAID, outTime);
        if (orders != null && !orders.isEmpty()) {
            for (Orders order : orders) {
                order.setStatus(Orders.CANCELLED);
                order.setCancelReason("支付超时");
                order.setCancelTime(LocalDateTime.now());
                orderMapper.update(order);
            }
        }
    }

    /*
    * 处理未点完成的订单（每天凌晨一点清理前一天没有完成的订单）
    * */
    @Scheduled(cron = "0 0 1 * * ? ")
    public void processDeliveryOrder () {
        log.info("处理未完成订单时间：{}", LocalDateTime.now());
        //查订单表，查看有没有在配送中，超过一个小时的
        LocalDateTime overTime = LocalDateTime.now().plusHours(-1);
        List<Orders> orders = orderMapper.getByStatusAndOrderTime(Orders.DELIVERY_IN_PROGRESS, overTime);
        if (orders != null && !orders.isEmpty()) {
            for (Orders order : orders) {
                order.setStatus(Orders.COMPLETED);
                orderMapper.update(order);
            }
        }
    }
}
