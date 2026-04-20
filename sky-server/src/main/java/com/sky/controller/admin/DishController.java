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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.print.attribute.standard.ReferenceUriSchemesSupported;
import java.util.List;
import java.util.Set;

@RestController
@Slf4j
@RequestMapping("/admin/dish")
@Api(tags = "菜品相关接口")
public class DishController {

    @Autowired
    private DishService dishService;
    @Autowired
    private RedisTemplate redisTemplate;

    /*
    *   新增菜品
    * */
    @ApiOperation("新增菜品")
    @PostMapping()
    public Result save(@RequestBody DishDTO dishDTO) {
        dishService.saveWithFlavors(dishDTO);
        //清理redis缓存，保证数据的一致性
        //新增菜品删除对应分类的redis数据    （可以不清理，因为新增菜品是停售状态，不会显示，在起售时会清理缓存）
        String key = "dish_" + dishDTO.getCategoryId();
        redisTemplate.delete(key);

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
        //清理redis缓存，保证数据的一致性
        //批量删除菜品，涉及多个分类，全部删除缓存  (可以不清理，因为删除菜品需要先停售，停售时候会清理缓存)
        Set keys = redisTemplate.keys("dish_*");
        redisTemplate.delete(keys);

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
        //清理redis缓存，保持数据一致性
        //更改菜涉及更改菜品的分类，删除全部redis缓存
        Set keys = redisTemplate.keys("dish_*");
        redisTemplate.delete(keys);

        return Result.success();
    }

    /*
    * 菜品起售停售
    * */
    @ApiOperation("起售停售")
    @PostMapping("/status/{status}")
    public Result startOrStop (@PathVariable Integer status, Long id) {
        dishService.startOrStop(status, id);
        //清理redis缓存，保持数据一致性
        //更改对应分类的缓存数据（但是需要查询一次数据库，所以全部删除缓存）
        Set keys = redisTemplate.keys("dish_*");
        redisTemplate.delete(keys);

        return Result.success();
    }
}
