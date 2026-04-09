package com.sky.service;

import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.result.PageResult;

public interface EmployeeService {

    /**
     * 员工登录
     * @param employeeLoginDTO
     * @return
     */
    Employee login(EmployeeLoginDTO employeeLoginDTO);
    /*
    * 新增员工
    * */
    public void save(EmployeeDTO employeeDTO);
    /*
    * 分页查询
    * */
    public PageResult page(EmployeePageQueryDTO employeePageQueryDTO);
    /*
    * 更新员工状态
    * */
    void startOrStop(Integer status, Long id);

    /*
    * 根据ID查员工信息
    * */
    Employee selectById(Long id);
    /*
    * 编辑员工信息
    * */
    void update(EmployeeDTO employeeDTO);
}
