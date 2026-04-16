package com.sky.controller.admin;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController("adminShopController")
@Slf4j
@RequestMapping("/admin/shop")
@Api(tags = "店铺状态")
public class ShopController {

    private static final String key = "SHOP_STATUS";

    @Autowired
    private RedisTemplate redisTemplate;
    /*
    * 更改店铺状态
    * */
    @ApiOperation("更改店铺状态")
    @PutMapping("/{status}")
    public Result setStatus(@PathVariable Integer status) {
        redisTemplate.opsForValue().set(key, status);
        log.info("设置店铺营业状态为：{}", status == 1 ? "营业中" : "打烊了");
        return Result.success();
    }
    /*
    * 获取店铺状态
    * */
    @ApiOperation("获取店铺状态")
    @GetMapping("/status")
    public Result getStatus () {
        Integer status = (Integer) redisTemplate.opsForValue().get(key);
        log.info("店铺营业状态为：{}", status == 1 ? "营业中" : "打烊了");
        return Result.success(status);
    }

}
