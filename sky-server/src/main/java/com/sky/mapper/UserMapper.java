package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

@Mapper
public interface UserMapper {
    /*
    * 根据openid查寻用户
    * */
    @Select("select * from user where openid = #{openid}")
    User getByOpenId(String openid);

    /*
    * 新增用户
    * */
    void insert(User user);
    @Select("select * from user where id = #{userId}")
    User getById(Long userId);

    /*
    * 查询用户数量（通过时间查）
    * */
    Integer getUserCount(Map<String, Object> map);
}
