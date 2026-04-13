package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.FlavorsMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private FlavorsMapper flavorsMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private SetmealMapper setmealMapper;

    /*
    * 新增菜品
    * */
    @Override
    public void saveWithFlavors(DishDTO dishDTO) {
        //菜品插入1条数据
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.insert(dish);
        Long dishId = dish.getId();
        //口味表插入N条数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && !flavors.isEmpty()) {
            //便利口味对象，赋给菜品ID值
            flavors.forEach(s -> s.setDishId(dishId));
            //批量插入
            flavorsMapper.insertBatch(flavors);
        }
    }

    /*
    * 分页查询
    * */
    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
        Page<DishVO> page= dishMapper.pageQuery(dishPageQueryDTO);
        long total = page.getTotal();
        List<DishVO> result = page.getResult();
        return new PageResult(total, result);
    }

    /*
    * 批量删除
    * */
    @Transactional
    @Override
    public void delete(List<Long> ids) {
        //查菜品状态(批量查)
        List<Dish> dishes = dishMapper.selectByIds(ids);
        for (Dish dish : dishes) {
            if (dish.getStatus() == StatusConstant.ENABLE) {
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }
        //查菜品关联套餐
        List<SetmealDish> setmealDishes = setmealDishMapper.selectByDishId(ids);
        if (setmealDishes != null && !setmealDishes.isEmpty()) {
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }
        //删除菜品
        dishMapper.deleteById(ids);
        //删除口味
        flavorsMapper.deleteByDishIds(ids);
    }

    /*
    * 根据ID查询
    * */
    @ApiOperation("根据ID查询")
    @Override
    public DishVO selectByIdWithFlavor(Long id) {
        //查询菜品表
        Dish dish = dishMapper.selectById(id);
        //查询口味表
        List<DishFlavor> flavors = flavorsMapper.selectByDishId(id);
        //封装为VO对象返回
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish, dishVO);
        dishVO.setFlavors(flavors);
        return dishVO;
    }

    /*
    * 更改菜品
    * */
    @ApiOperation("修改菜品")
    @Override
    public void updateWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        //更改菜品表信息
        dishMapper.update(dish);
        //更改口味表信息
        //删除原有口味
        flavorsMapper.deleteByDishId(dish.getId());
        //新增口味信息(插入需要获得菜品ID)
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && !flavors.isEmpty()) {
            flavors.forEach(s -> s.setDishId(dish.getId()));
            flavorsMapper.insertBatch(flavors);
        }
    }
    /*
    * 菜品起售停售
    * */
    @ApiOperation("更改菜品状态")
    @Override
    public void startOrStop(Integer status, Long id) {
        Dish dish = Dish.builder()
                        .id(id)
                        .status(status)
                        .build();
        dishMapper.update(dish);
        //如果菜品停售，包含菜品的套餐也停售
        List<Long> dishId = new ArrayList<>();
        dishId.add(id);
        List<SetmealDish> setmealDishes = setmealDishMapper.selectByDishId(dishId);
        if (setmealDishes != null && !setmealDishes.isEmpty()) {
            for (SetmealDish setmealDish : setmealDishes) {
                Setmeal setmeal = Setmeal.builder()
                        .id(setmealDish.getSetmealId())
                        .status(StatusConstant.DISABLE)
                        .build();
                setmealMapper.update(setmeal);
            }
        }
    }

    /*
    * 根据分类ID查
    * */
    @Override
    public List<Dish> selectByCategoryId(Long categoryId) {
        Dish dish = Dish.builder()
                    .categoryId(categoryId)
                    .status(StatusConstant.ENABLE)
                    .build();
        List<Dish> dishes = dishMapper.list(dish);
        return dishes;
    }
}
