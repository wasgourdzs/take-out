package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/admin/setmeal")
@Api(tags = "套餐相关借口")
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    /*
    * 新增套餐
    * */
    @CachePut(cacheNames = "setmealCache", key = "#setmealDTO.categoryId")
    @ApiOperation("新增套餐")
    @PostMapping()
    public Result save(@RequestBody SetmealDTO setmealDTO) {
        setmealService.save(setmealDTO);
        return Result.success();
    }

    /*
    * 分页查询
    * */
    @ApiOperation("分页查询")
    @GetMapping("/page")
    public Result<PageResult> pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageResult pageResult = setmealService.pageQuery(setmealPageQueryDTO);
        return Result.success(pageResult);
    }
    /*
    *  根据ID查套餐
    * */
    @ApiOperation("根据ID查询")
    @GetMapping("/{id}")
    public Result<SetmealVO> selectById(@PathVariable Long id) {
        SetmealVO setmealVO = setmealService.selectById(id);
        return Result.success(setmealVO);
    }
    /*
    * 修改套餐
    * */
    @CacheEvict(cacheNames = "setmealCache", allEntries = true)
    @ApiOperation("修改套餐")
    @PutMapping()
    public Result update(@RequestBody SetmealDTO setmealDTO) {
        setmealService.update(setmealDTO);
        return Result.success();
    }
    /*
    * 删除套餐
    * */
    @CacheEvict(cacheNames = "setmealCache", allEntries = true)
    @ApiOperation("删除套餐")
    @DeleteMapping()
    public Result delete(@RequestParam List<Long> ids) {
        setmealService.delete(ids);
        return Result.success();
    }
    /*
    * 套餐起售停售
    * */
    @CacheEvict(cacheNames = "setmealCache", allEntries = true)
    @ApiOperation("起售停售")
    @PostMapping("/status/{status}")
    public Result startOrStop(@PathVariable Integer status, Long id) {
        setmealService.startOrStop(status, id);
        return Result.success();
    }
}
