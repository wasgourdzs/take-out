package com.sky.controller.user;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController("userShopController")
@Slf4j
@RequestMapping("/user/shop")
@Api(tags = "店铺状态")
public class ShopController {

    private static final String key = "SHOP_STATUS";

    @Autowired
    private RedisTemplate redisTemplate;

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
