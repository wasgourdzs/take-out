package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.print.attribute.standard.ReferenceUriSchemesSupported;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/admin/dish")
@Api("菜品相关接口")
public class DishController {

    @Autowired
    private DishService dishService;

    /*
    *   新增菜品
    * */
    @ApiOperation("新增菜品")
    @PostMapping()
    public Result save(@RequestBody DishDTO dishDTO) {
        dishService.saveWithFlavors(dishDTO);
        return Result.success();
    }

    /*
    * 菜品分页查询
    * */
    @GetMapping("/page")
    @ApiOperation("分页查询")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO) {
        PageResult pageResult = dishService.pageQuery(dishPageQueryDTO);
        return Result.success(pageResult);
    }

    /*
    * 批量删除菜品
    * */
    @ApiOperation("批量删除")
    @DeleteMapping()
    public Result delete(@RequestParam List<Long> ids) {
        log.info("批量删除");
        dishService.delete(ids);
        return Result.success();
    }

    /*
    * 根据ID查询菜品
    * */
    @ApiOperation("根据id查菜品")
    @GetMapping("/{id}")
    public Result<DishVO> selectById(@PathVariable Long id){
        DishVO dishVO = dishService.selectByIdWithFlavor(id);
        return Result.success(dishVO);
    }

    /*
    * 根据分类ID查菜品
    * */
    @GetMapping("/list")
    public Result<List<Dish>> selectByCategoryId(Long categoryId) {
        List<Dish> dishes = dishService.selectByCategoryId(categoryId);
        return Result.success(dishes);
    }

    /*
    * 更改菜品
    * */
    @ApiOperation("更改菜品")
    @PutMapping()
    public Result updateWithFlavor(@RequestBody DishDTO dishDTO) {
        dishService.updateWithFlavor(dishDTO);
        return Result.success();
    }

    /*
    * 菜品起售停售
    * */
    @ApiOperation("起售停售")
    @PostMapping("/status/{status}")
    public Result startOrStop (@PathVariable Integer status, Long id) {
        dishService.startOrStop(status, id);
        return Result.success();
    }
}
