package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
    @Autowired
    private WorkspaceService workspaceService;

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

    /*
    * 导出Excel数据
    * */
    @Override
    public void getExcelDate(HttpServletResponse response) {
        //1.查数据库统计数据
        LocalDate begin = LocalDate.now().minusDays(30);
        LocalDate end = LocalDate.now().minusDays(1);
            //查数据
        BusinessDataVO businessData = workspaceService.getBusinessData(LocalDateTime.of(begin, LocalTime.MIN), LocalDateTime.of(end, LocalTime.MAX));

        //2.写入excel表格
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");
        try {
            XSSFWorkbook excel = new XSSFWorkbook(inputStream);
            //概览数据
            XSSFSheet sheet = excel.getSheetAt(0);
            sheet.getRow(1).getCell(1).setCellValue("时间：" + begin + "至" + end);
            XSSFRow row = sheet.getRow(3);
            row.getCell(2).setCellValue(businessData.getTurnover());
            row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
            row.getCell(6).setCellValue(businessData.getNewUsers());
            row  = sheet.getRow(4);
            row.getCell(2).setCellValue(businessData.getValidOrderCount());
            row.getCell(4).setCellValue(businessData.getUnitPrice());

            //明细数据
            for (int i = 0; i < 30; i++) {
                LocalDate date = begin.plusDays(i);
                BusinessDataVO data = workspaceService.getBusinessData(LocalDateTime.of(date, LocalTime.MIN), LocalDateTime.of(date, LocalTime.MAX));
                row = sheet.getRow(7 + i);
                row.getCell(1).setCellValue(date.toString());
                row.getCell(2).setCellValue(data.getTurnover());
                row.getCell(3).setCellValue(data.getValidOrderCount());
                row.getCell(4).setCellValue(data.getOrderCompletionRate());
                row.getCell(5).setCellValue(data.getUnitPrice());
                row.getCell(6).setCellValue(data.getNewUsers());
            }

            //3.使用HttpServletRespond获取输出流，页面可以下载
            ServletOutputStream outputStream = response.getOutputStream();
            excel.write(outputStream);

            excel.close();
            outputStream.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
