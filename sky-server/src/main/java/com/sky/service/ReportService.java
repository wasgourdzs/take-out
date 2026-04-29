package com.sky.service;

import com.sky.vo.OrderReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;

import java.time.LocalDate;

public interface ReportService {

    /*
    * 获得营业额
    * */
    TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end);
    /*
    * 用户统计
    * */
    UserReportVO getUserStatistics(LocalDate begin, LocalDate end);
    /*
    * 订单统计
    * */
    OrderReportVO getOrdersStatistics(LocalDate begin, LocalDate end);
}
