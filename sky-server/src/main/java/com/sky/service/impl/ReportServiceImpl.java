package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
                .dateList(StringUtils.join(dateList, ","))
                .turnoverList(StringUtils.join(turnoverList, ","))
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
                .dateList(StringUtils.join(dateList, ","))
                .newUserList(StringUtils.join(newUser, ","))
                .totalUserList(StringUtils.join(totalUser, ","))
                .build();

        return userReportVO;
    }

    /*
    * 订单统计
    * */
    @Override
    public OrderReportVO getOrdersStatistics(LocalDate begin, LocalDate end) {
        //创建时间列表
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        //查表，要查所有订单和已完成的订单数
        List<Integer> completeOrderList = new ArrayList<>();
        List<Integer> totalOrderList = new ArrayList<>();
        for (LocalDate localDate : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(localDate, LocalDateTime.MIN.toLocalTime());
            LocalDateTime endTime = LocalDateTime.of(localDate, LocalDateTime.MAX.toLocalTime());
            //获取订单数
            Integer totalCount = getCountWithStatusAndTime(beginTime, endTime, null);
            Integer completeCount = getCountWithStatusAndTime(beginTime, endTime, Orders.COMPLETED);

            completeOrderList.add(completeCount);
            totalOrderList.add(totalCount);
        }

        //计算从订单数、总完成数、完成率
        Integer complete = completeOrderList.stream().reduce(Integer::sum).get();
        Integer total = totalOrderList.stream().reduce(Integer::sum).get();
        Double rate = total == 0 ? 0.0 : complete.doubleValue() / total;

        OrderReportVO orderReportVO = OrderReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .orderCountList(StringUtils.join(totalOrderList, ","))
                .validOrderCountList(StringUtils.join(completeOrderList, ","))
                .totalOrderCount(total)
                .validOrderCount(complete)
                .orderCompletionRate(rate)
                .build();

        return orderReportVO;
    }

    private Integer getCountWithStatusAndTime (LocalDateTime begin, LocalDateTime end, Integer status) {
        Map<String, Object> map = new HashMap<>();
        map.put("begin", begin);
        map.put("end", end);
        map.put("status", status);
        Integer count = orderMapper.countByMap(map);
        return count;
    }

    /*
    * 热销产品统计
    * */
    @Override
    public SalesTop10ReportVO getTop10(LocalDate begin, LocalDate end) {
        //根据时间查询得到的订单列表
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalDateTime.MIN.toLocalTime());
        LocalDateTime endTime = LocalDateTime.of(end, LocalDateTime.MAX.toLocalTime());

        Map<String, Object> map = new HashMap<>();
        map.put("begin", beginTime);
        map.put("end", endTime);
        map.put("status", Orders.COMPLETED);
        //多表联查
        List<GoodsSalesDTO> list = orderMapper.getOrderDetailByMap(map);

        List<String> nameList = new ArrayList<>();
        List<Integer> numberList = new ArrayList<>();

        for (GoodsSalesDTO goodsSalesDTO : list) {
            nameList.add(goodsSalesDTO.getName());
            numberList.add(goodsSalesDTO.getNumber());
        }

        //Stream方法
//        List<String> list1 = list.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());

        SalesTop10ReportVO salesTop10ReportVO = SalesTop10ReportVO.builder()
                .nameList(StringUtils.join(nameList, ","))
                .numberList(StringUtils.join(numberList, ","))
                .build();

        return salesTop10ReportVO;
    }
}
