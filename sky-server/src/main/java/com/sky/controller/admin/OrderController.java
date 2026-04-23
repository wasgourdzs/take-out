package com.sky.controller.admin;

import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/admin/order")
@Slf4j
@RestController
@Api("订单相关接口")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /*
    * 条件查询
    * */
    @ApiOperation("条件查询")
    @GetMapping("/conditionSearch")
    public Result<PageResult> conditionSearch (OrdersPageQueryDTO ordersPageQueryDTO) {
        PageResult page = orderService.conditionSearch(ordersPageQueryDTO);
        return Result.success(page);
    }

    /*
    * 统计各种订单状态
    * */
    @ApiOperation("统计各订单总数")
    @GetMapping("/statistics")
    public Result<OrderStatisticsVO> statistics () {
        OrderStatisticsVO orderStatisticsVO = orderService.statistics();
        return Result.success(orderStatisticsVO);
    }

    /*
    * 查询订单全部信息
    * */
    @ApiOperation("获取订单详情")
    @GetMapping("/details/{id}")
    public Result<OrderVO> details (@PathVariable Long id) {
        OrderVO orderVO = orderService.list(id);
        return Result.success(orderVO);
    }

    /*
    * 接单（修改订单状态为已接单）
    * */
    @ApiOperation("接单")
    @PutMapping("/confirm")
    public Result confirm (@RequestBody OrdersConfirmDTO ordersConfirmDTO) {
        orderService.confirm(ordersConfirmDTO);
        return Result.success();
    }

    /*
    * 拒单(修改订单状态为拒单)
    * */
    @ApiOperation("拒单")
    @PutMapping("/rejection")
    public Result rejection (@RequestBody OrdersRejectionDTO ordersRejectionDTO) {
        orderService.rejection(ordersRejectionDTO);
        return Result.success();
    }

    /*
    * 取消订单（修改订单状态为取消）
    * */
    @PutMapping("/cancel")
    @ApiOperation("取消订单")
    public Result cancel (@RequestBody OrdersCancelDTO ordersCancelDTO) {
        orderService.cancelByAdmin(ordersCancelDTO);
        return Result.success();
    }

    /*
    * 派送订单（修改订单状态为派送）
    * */
    @ApiOperation("派送订单")
    @PutMapping("/delivery/{id}")
    public Result delivery (@PathVariable Long id) {
        orderService.delivery(id);
        return Result.success();
    }

    /*
    * 完成订单(修改订单状态为已完成)
    * */
    @ApiOperation("完成订单")
    @PutMapping("/complete/{id}")
    public Result complete (@PathVariable Long id) {
        orderService.complete(id);
        return Result.success();
    }
}
