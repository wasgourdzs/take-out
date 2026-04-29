package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;

    /*
     * 获得营业额
     * */
    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        //封装日期列表
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        //查表，封装营业额列表
        List<Double> turnoverList = new ArrayList<>();
        for (LocalDate localDate : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(localDate, LocalDateTime.MIN.toLocalTime());
            LocalDateTime endTime = LocalDateTime.of(localDate, LocalDateTime.MAX.toLocalTime());
            //封装为map集合查询
            Map<String, Object> map = new HashMap<>();
            map.put("begin", beginTime);
            map.put("end", endTime);
            map.put("status", Orders.COMPLETED);

            Double turnover = orderMapper.sumByMap(map);
            //判断当天有没有数据
            turnover = turnover == null ? 0 : turnover;

            turnoverList.add(turnover);
        }

        TurnoverReportVO turnoverReportVO = TurnoverReportVO.builder()
                .dateList(StringUtil.join(",", dateList))
                .turnoverList(StringUtil.join(",", turnoverList))
                .build();

        return turnoverReportVO;
    }

    /*
    * 用户统计
    * */
    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        //处理时间表
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        //查询用户表，找创建时间符合要求的
        List<Integer> newUser = new ArrayList<>();
        List<Integer> totalUser = new ArrayList<>();

        for (LocalDate localDate : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(localDate, LocalDateTime.MIN.toLocalTime());
            LocalDateTime endTime = LocalDateTime.of(localDate, LocalDateTime.MAX.toLocalTime());

            //先查总的用户数，因为只需要endTime(查用户表)
            Map<String, Object> map = new HashMap<>();
            map.put("end", endTime);
            Integer newUserCount = userMapper.getUserCount(map);
            newUserCount = newUserCount == null ? 0 : newUserCount;

            map.put("begin", beginTime);
            Integer totalUserCount = userMapper.getUserCount(map);
            totalUserCount = totalUserCount == null ? 0 : totalUserCount;

            newUser.add(newUserCount);
            totalUser.add(totalUserCount);
        }

        UserReportVO userReportVO = UserReportVO.builder()
                .dateList(StringUtil.join(",", dateList))
                .newUserList(StringUtil.join(",", newUser))
                .totalUserList(StringUtil.join(",", totalUser))
                .build();

        return userReportVO;
    }
}
