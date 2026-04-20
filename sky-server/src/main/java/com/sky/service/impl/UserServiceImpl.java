package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    public static final String WX_LOGIN = "https://api.weixin.qq.com/sns/jscode2session";

    @Autowired
    private WeChatProperties weChatProperties;
    @Autowired
    private UserMapper userMapper;

    /*
    * 用户登录
    * */
    @Override
    public User wxLogin(UserLoginDTO userLoginDTO){
        //调用方法获取openid
        String openid = getOpenid(userLoginDTO);
        //判断openid是否为空（每个code只能用一次）
        if (openid == null) {
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }
        //判断是否为新用户（查user表）
        User user = userMapper.getByOpenId(openid);
        //新用户则完成注册（向user表插数据）
        if (user == null) {
            user = User.builder()
                    .openid(openid)
                    .createTime(LocalDateTime.now())
                    .build();
            userMapper.insert(user);
        }
        //返回
        return user;
    }

    //获取用户登录的OpenId
    public String getOpenid (UserLoginDTO userLoginDTO) {
        //向微信发送请求，获得用户表示openid
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("appid", weChatProperties.getAppid());
        paramMap.put("secret", weChatProperties.getSecret());
        paramMap.put("js_code", userLoginDTO.getCode());
        paramMap.put("grant_type", "authorization_code");
        String result = HttpClientUtil.doGet(WX_LOGIN, paramMap); //json字符串

        //json字符串转为json对象
        JSONObject jsonObject = JSONObject.parseObject(result);
        String openid = jsonObject.getString("openid");

        return openid;
    }
}
