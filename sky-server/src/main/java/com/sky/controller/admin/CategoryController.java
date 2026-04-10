package com.sky.controller.admin;

import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.ResultExtractor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/category")
@Slf4j
@Api("菜品分类相关接口")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /*
    * 分页查询
    * */
    @ApiOperation("分页查询")
    @GetMapping("/page")
    public Result<PageResult> page(CategoryPageQueryDTO categoryPageQueryDTO) {
        PageResult pageResult = categoryService.page(categoryPageQueryDTO);
        return Result.success(pageResult);
    }

    /*
    * 根据类型查询
    * */
    @ApiOperation("类型查询")
    @GetMapping("/list")
    public Result<Category> list(Integer type) {
        Category category = categoryService.list(type);
        return Result.success(category);
    }

    /*
    * 新增分类
    * */
    @PostMapping()
    @ApiOperation("新增分类")
    public Result insert(@RequestBody CategoryDTO categoryDTO){
        categoryService.insert(categoryDTO);
        return Result.success();
    }

    /*
    * 分类状态
    * */
    @ApiOperation("启用或停用")
    @PostMapping("/status/{status}")
    public Result startOrStop(@PathVariable Integer status, Long id) {
        categoryService.startOrStop(status, id);
        return Result.success();
    }

    /*
    * 修改分类
    * */
    @ApiOperation("修改分类")
    @PutMapping()
    public Result update(@RequestBody CategoryDTO categoryDTO) {
        categoryService.update(categoryDTO);
        return Result.success();
    }

    /*
    * 删除分类
    * */
    @DeleteMapping()
    public Result delete(Long id) {
        categoryService.delete(id);
        return Result.success();
    }
}
