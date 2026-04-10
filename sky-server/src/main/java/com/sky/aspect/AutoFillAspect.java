package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Aspect
@Slf4j
@Component
public class AutoFillAspect {
    //切入点
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void pointCut(){}
    //前置通知
    @Before("pointCut()")
    public void autoFill(JoinPoint joinPoint) {
        log.info("开始自动填充..");
        //获取注解(类型)
        MethodSignature signature = (MethodSignature)joinPoint.getSignature(); //获得方法，并转为method类型，来获取注解
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);
        OperationType value = autoFill.value();
        //获得参数
        Object[] args = joinPoint.getArgs();
        Object arg = args[0];
        //准备数据
        LocalDateTime now = LocalDateTime.now();
        Long id = BaseContext.getCurrentId();
        //利用反射来赋值
        if (value == OperationType.INSERT) {
            try {
                //获取方法
                Method setCreateTime = arg.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                Method setCreateUser = arg.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                Method setUpdateTime = arg.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = arg.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
                //赋值
                setCreateUser.invoke(arg, id);
                setCreateTime.invoke(arg, now);
                setUpdateUser.invoke(arg, id);
                setUpdateTime.invoke(arg, now);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else if (value == OperationType.UPDATE) {
            try {
                //获取方法
                Method setUpdateTime = arg.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = arg.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
                //赋值
                setUpdateUser.invoke(arg, id);
                setUpdateTime.invoke(arg, now);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
