package com.sky.service;

import com.sky.vo.TurnoverReportVO;

import java.time.LocalDate;

public interface ReportService {

    /*
    * 获得营业额
    * */
    TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end);

}
