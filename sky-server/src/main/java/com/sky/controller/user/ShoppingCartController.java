package com.sky.controller.user;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.result.Result;
import com.sky.service.ShoppingCartService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequestMapping("/user/shoppingCart")
@RestController
@Api(tags = "C-端购物车接口")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    /*
    * 添加购物车
    * */
    @ApiOperation("添加购物车")
    @PostMapping("/add")
    public Result add(@RequestBody ShoppingCartDTO shoppingCartDTO) {
        shoppingCartService.add(shoppingCartDTO);
        return Result.success();
    }

    /*
    * 查看购物车
    * */
    @ApiOperation("查看购物车")
    @GetMapping("list")
    public Result<List<ShoppingCart>> list() {
        List<ShoppingCart> list =  shoppingCartService.list();
        return Result.success(list);
    }

    /*
    * 清空购物车
    * */
    @ApiOperation("清空购物车")
    @DeleteMapping("/clean")
    public Result clean() {
        shoppingCartService.clean();
        return Result.success();
    }

    /*
    * 删除购物车内容
    * */
    @ApiOperation("删除购物车一个内容")
    @PostMapping("/sub")
    public Result delete (@RequestBody ShoppingCartDTO shoppingCartDTO) {
        shoppingCartService.delete(shoppingCartDTO);
        return Result.success();
    }
}
