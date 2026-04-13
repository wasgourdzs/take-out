package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;

    /*
    * 新增套餐
    * */
    @Transactional
    @Override
    public void save(SetmealDTO setmealDTO) {
        //套餐表新增套餐
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.insert(setmeal);
        //套餐菜品关系表新增对应关系
        //获取套餐ID,并赋值
        Long setmealId = setmeal.getId();
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        if (setmealDishes != null && !setmealDishes.isEmpty()) {
            setmealDishes.forEach(s -> s.setSetmealId(setmealId));
            setmealDishMapper.insertBatch(setmealDishes);
        }
    }
    /*
    * 分页查询
    * */
    @Override
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());
        Page<SetmealVO> page = setmealMapper.pageQuery(setmealPageQueryDTO);
        long total = page.getTotal();
        List<SetmealVO> result = page.getResult();
        return new PageResult(total, result);
    }
    /*
    * 根据ID查询
    * */
    @Override
    public SetmealVO selectById(Long id) {
        SetmealVO setmealVO = new SetmealVO();
        //查套餐表
        Setmeal setmeal = setmealMapper.selectById(id);
        BeanUtils.copyProperties(setmeal, setmealVO);
        //查套餐菜品对应表
        List<SetmealDish> setmealDishes = setmealDishMapper.selectBySetmealId(id);
        setmealVO.setSetmealDishes(setmealDishes);
        return setmealVO;
    }
    /*
    * 修改套餐
    * */
    @Override
    public void update(SetmealDTO setmealDTO) {
        //更新套餐表
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.update(setmeal);
        //更新套餐菜品对应关系表(先删除、再添加)
        List<Long> ids = new ArrayList<>();
        ids.add(setmeal.getId());
        setmealDishMapper.deleteBatch(ids);

        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        for (SetmealDish setmealDish : setmealDishes) {
            setmealDish.setSetmealId(setmeal.getId());
        }
        setmealDishMapper.insertBatch(setmealDishes);

    }

    /*
    * 删除套餐
    * */
    @Transactional
    @Override
    public void delete(List<Long> ids) {
        //删除套餐表中套餐
        //查询套餐售卖状态
        for (Long id : ids) {
            Setmeal setmeal = setmealMapper.selectById(id);
            if (setmeal.getStatus() == StatusConstant.ENABLE) {
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        }
        setmealMapper.deleteBatch(ids);
        //删除套餐菜品对应表中的关系
        setmealDishMapper.deleteBatch(ids);
    }
}
