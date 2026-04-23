package com.sky.controller.user;

import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RequestMapping("/user/order")
@RestController("userOrderController")
@Api("用户下单相关接口")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /*
    * 用户下单
    * */
    @PostMapping("/submit")
    @ApiOperation("用户下单")
    public Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO ordersSubmitDTO) {
        OrderSubmitVO orderSubmitVO = orderService.submit(ordersSubmitDTO);
        return Result.success(orderSubmitVO);
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    @PutMapping("/payment")
    @ApiOperation("订单支付")
    public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        log.info("订单支付：{}", ordersPaymentDTO);
        OrderPaymentVO orderPaymentVO = orderService.payment(ordersPaymentDTO);
        log.info("生成预支付交易单：{}", orderPaymentVO);
        return Result.success(orderPaymentVO);
    }
    /*
    * 查询订单历史
    * */
    @ApiOperation("查询历史订单（分页查询）")
    @GetMapping("/historyOrders")
    public Result<PageResult> page(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageResult pageResult = orderService.page(ordersPageQueryDTO);
        return Result.success(pageResult);
    }

    /*
    * 查询订单详情
    * */
    @ApiOperation("查询订单详情")
    @GetMapping("/orderDetail/{id}")
    public Result<OrderVO> list (@PathVariable Long id) {
        OrderVO orderVO = orderService.list(id);
        return Result.success(orderVO);
    }

    /*
    * 取消订单(修改订单的状态为已取消)
    * */
    @ApiOperation("取消订单")
    @PutMapping("/cancel/{id}")
    public Result cancel (@PathVariable Long id) {
        orderService.cancel(id);
        return Result.success();
    }

    /*
    * 再来一单（将订单再次加到购物车）
    * */
    @ApiOperation("再来一单")
    @PostMapping("/repetition/{id}")
    public Result repetition (@PathVariable Long id) {
        orderService.repetition(id);
        return Result.success();
    }
}
