package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.HttpClientUtil;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WeChatPayUtil weChatPayUtil;

    private Orders orders;

    //百度地图相关信息注入
    @Value("${sky.shop.address}")
    private String shopAddress;
    @Value("${sky.baidu.AK}")
    private String ak;

    /*
    * 用户下单
    * */
    @Transactional
    @Override
    public OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO) {
        //处理异常清空（地址为空或不存在、购物车为空或不存在）（先查询）
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
            //获取用户的ID
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(userId);
        List<ShoppingCart> shoppingCarts = shoppingCartMapper.list(shoppingCart);
        if (shoppingCarts == null || shoppingCarts.isEmpty()) {
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        //判断距离是否合理
        checkOutOfRange(addressBook.getProvinceName() + addressBook.getCityName() + addressBook.getDetail());

        //向订单表插入一条数据(还要获取订单ID)
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
            //配置订单的额外信息
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setUserId(userId);
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setConsignee(addressBook.getConsignee());
        orders.setPhone(addressBook.getPhone());
        orders.setAddress(addressBook.getDetail());

        this.orders = orders; //方便后续更改订单状态，跳过微信支付
        orderMapper.insert(orders);
        //向订单详情表插入多条数据(批量插入)
        List<OrderDetail> orderDetails = new ArrayList<>();
        for (ShoppingCart cart : shoppingCarts) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart, orderDetail);
            orderDetail.setOrderId(orders.getId()); //设置订单详情的订单ID
            orderDetails.add(orderDetail);
        }

        orderDetailMapper.insert(orderDetails);
        //清空购物车信息（已提交订单）
        shoppingCartMapper.delete(userId);
        //返回数据
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(orders.getId())
                .orderAmount(orders.getAmount())
                .orderNumber(orders.getNumber())
                .orderTime(orders.getOrderTime())
                .build();
        return orderSubmitVO;
    }


    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

//        //调用微信支付接口，生成预支付交易单
//        JSONObject jsonObject = weChatPayUtil.pay(
//                ordersPaymentDTO.getOrderNumber(), //商户订单号
//                new BigDecimal(0.01), //支付金额，单位 元
//                "苍穹外卖订单", //商品描述
//                user.getOpenid() //微信用户的openid
//        );
//
//        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
//            throw new OrderBusinessException("该订单已支付");
//        }
//
//        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
//        vo.setPackageStr(jsonObject.getString("package"));
//        return vo;

        //跳过微信支付
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code","ORDERPAID");
        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));
        Integer OrderPaidStatus = Orders.PAID;//支付状态，已支付
        Integer OrderStatus = Orders.TO_BE_CONFIRMED;  //订单状态，待接单
        LocalDateTime check_out_time = LocalDateTime.now();//更新支付时间
        orderMapper.updateStatus(OrderStatus, OrderPaidStatus, check_out_time, this.orders.getId());
        return vo;

    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);
    }

    /*
    * 查历史订单
    * */
    @Override
    public PageResult page(OrdersPageQueryDTO ordersPageQueryDTO) {
        //使用pagehelper
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        //查询订单表
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());
        Page<Orders> page = orderMapper.pageQuery(ordersPageQueryDTO);

        List<OrderVO> list = new ArrayList<>();

            //判断有无订单
        if (page != null && page.getTotal() > 0) {
            //查询依次订单详细表
            List<Orders> result = page.getResult();
            for (Orders orders1 : result) {
                List<OrderDetail> orderDetails = orderDetailMapper.selectByOrdersId(orders1.getId());
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders1, orderVO);
                orderVO.setOrderDetailList(orderDetails);
                list.add(orderVO);
            }
        }

        return new PageResult(page.getTotal(), list);
    }

    /*
    * 查订单详情
    * */
    @Override
    public OrderVO list(Long id) {
        //查订单
        Orders orders1 = orderMapper.getById(id);
        //查订单详情
        List<OrderDetail> orderDetails = orderDetailMapper.selectByOrdersId(id);
        //返回
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders1, orderVO);
        orderVO.setOrderDetailList(orderDetails);
        return orderVO;
    }

    /*
    * 取消订单（修改订单状态为已取消）
    * */
    @Override
    public void cancel(Long id) {
        //先查订单表，获取当前订单的订单状态，再判断如何修改
        Orders orders1 = orderMapper.getById(id);
            //判断是否存在
        if (orders1 == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        Integer status = orders1.getStatus();
        if (status > 2) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        if (status == 2) {
            //跳过了微信退款的功能
            orders1.setPayStatus(Orders.REFUND);
        }
        orders1.setStatus(Orders.CANCELLED);
        //取消原因和时间
        orders1.setCancelReason("用户取消");
        orders1.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders1);
    }

    /*
    * 再来一单（将订单加到购物车）
    * */
    @Override
    public void repetition(Long id) {
        //根据当前订单ID查订单详细表，加到购物车内
        List<OrderDetail> orderDetails = orderDetailMapper.selectByOrdersId(id);
        List<ShoppingCart> shoppingCarts = new ArrayList<>();
        for (OrderDetail orderDetail : orderDetails) {
            ShoppingCart shoppingCart = new ShoppingCart();
            BeanUtils.copyProperties(orderDetail, shoppingCart);
            shoppingCart.setUserId(BaseContext.getCurrentId());
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCarts.add(shoppingCart);
        }
        shoppingCartMapper.insertBatch(shoppingCarts);
    }

    /*
    * 管理端条件查询
    * */
    @Override
    public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        //使用pagehelper
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        //查全部订单表
        Page<Orders> page = orderMapper.pageQuery(ordersPageQueryDTO);

        List<OrderVO> list = new ArrayList<>();

        if (page != null || page.getTotal() > 0) {
            //根据订单ID查订单详细表
            List<Orders> result = page.getResult();
            for (Orders orders1 : result) {
                List<OrderDetail> orderDetails = orderDetailMapper.selectByOrdersId(orders1.getId());
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders1, orderVO);
                orderVO.setOrderDetailList(orderDetails);
                StringBuilder sb = new StringBuilder();
                for (OrderDetail orderDetail : orderDetails) {
                    sb.append(orderDetail.getName());
                    sb.append("*");
                    sb.append(orderDetail.getNumber());
                    sb.append(";");
                }
                orderVO.setOrderDishes(sb.toString());
                list.add(orderVO);
            }
        }

        return new PageResult(page.getTotal(), list);
    }

    /*
    * 统计各类订单总数
    * */
    @Override
    public OrderStatisticsVO statistics() {
        //查订单表全部内容，统计总数
        List<Orders> ordersList = orderMapper.list();
        int toBeConfirmed = 0;
        int confirmed = 0;
        int deliveryInProgress = 0;
        for (Orders orders1 : ordersList) {
            Integer status = orders1.getStatus();
            if (Objects.equals(status, Orders.TO_BE_CONFIRMED)) {
                toBeConfirmed++;
            } else if (Objects.equals(status, Orders.CONFIRMED)) {
                confirmed++;
            } else if (Objects.equals(status, Orders.DELIVERY_IN_PROGRESS)) {
                deliveryInProgress++;
            }
        }

        OrderStatisticsVO orderStatisticsVO = OrderStatisticsVO.builder()
                .toBeConfirmed(toBeConfirmed)
                .confirmed(confirmed)
                .deliveryInProgress(deliveryInProgress)
                .build();
        return orderStatisticsVO;
    }

    /*
    * 商家接单
    * */
    @Override
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        Orders orders1 = new Orders();
        orders1.setId(ordersConfirmDTO.getId());
        orders1.setStatus(Orders.CONFIRMED);
        orderMapper.update(orders1);
    }

    /*
    * 商家拒单
    * */
    @Override
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) {
        //修改订单状态为拒单（只有待接单的可以）
        Orders orders1 = orderMapper.getById(ordersRejectionDTO.getId());
            //判断订单状态(订单存在，状态为2)
        if (orders1 == null || !orders1.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        orders1.setStatus(Orders.CANCELLED);
        orders1.setCancelTime(LocalDateTime.now());
        orders1.setRejectionReason(ordersRejectionDTO.getRejectionReason());
        //退款（跳过微信退款，修改订单支付状态为退款）
        if (orders1.getPayStatus().equals(Orders.PAID)) {
            log.info("退款");
        }
        orders1.setPayStatus(Orders.REFUND);
        orderMapper.update(orders1);
    }

    /*
    *   商家取消订单
    * */
    @Override
    public void cancelByAdmin(OrdersCancelDTO ordersCancelDTO) {
        //查询订单，修改订单状态为已取消，已付款则退款
        Orders orders1 = orderMapper.getById(ordersCancelDTO.getId());
        if (orders1 == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        Integer payStatus = orders1.getPayStatus();
        if (payStatus.equals(Orders.PAID)) {
            log.info("退款");
            orders1.setPayStatus(Orders.REFUND);
        }
        orders1.setStatus(Orders.CANCELLED);
        orders1.setCancelReason(ordersCancelDTO.getCancelReason());
        orders1.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders1);
    }

    /*
    * 派送订单
    * */
    @Override
    public void delivery(Long id) {
        //查订单，订单存在并且状态为带派送才修改
        Orders orders1 = orderMapper.getById(id);
        if (orders1 == null || !orders1.getStatus().equals(Orders.CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        orders1.setStatus(Orders.DELIVERY_IN_PROGRESS);
        orderMapper.update(orders1);
    }

    /*
    * 完成订单
    * */
    @Override
    public void complete(Long id) {
        //获取订单数据，判断是否存在，订单状态是否是派送中
        Orders orders1 = orderMapper.getById(id);
        if (orders1 == null || !orders1.getStatus().equals(Orders.DELIVERY_IN_PROGRESS)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        orders1.setStatus(Orders.COMPLETED);
        //送达时间
        orders1.setDeliveryTime(LocalDateTime.now());
        orderMapper.update(orders1);
    }
    /*
    * 检查距离
    * */
    //调用百度api，检查配送距离是否超出限制
    private void checkOutOfRange (String address) {
        //编写参数
        Map<String, String> map = new HashMap<>();
        map.put("address", shopAddress);
        map.put("ak", ak);
        map.put("output", "json");
        //发送请求（地理编码）
        String shop = HttpClientUtil.doGet("https://api.map.baidu.com/geocoding/v3", map);
        //解析经纬度值
        JSONObject jsonObject = JSON.parseObject(shop);
        if (!jsonObject.getString("status").equals("0")) {
            throw new OrderBusinessException("店铺地址解析失败");
        }

        JSONObject location = jsonObject.getJSONObject("result").getJSONObject("location");
        String shopLngLat = location.getString("lat") + "," + location.getString("lng");
        
        map.put("address", address);
        String user = HttpClientUtil.doGet("https://api.map.baidu.com/geocoding/v3", map);
        JSONObject object = JSON.parseObject(user);
        if (!object.getString("status").equals("0")) {
            throw new OrderBusinessException("用户位置解析失败");
        }
        JSONObject userLocation = object.getJSONObject("result").getJSONObject("location");
        String userLngLat = userLocation.getString("lat") + "," + userLocation.getString("lng");

        //调用百度计算路径api（路径规划）
        map.put("origin", shopLngLat);
        map.put("destination", userLngLat);
        map.put("steps_info","0");
            //发送请求
        String json = HttpClientUtil.doGet("https://api.map.baidu.com/direction/v2/driving", map);
            //解析
        JSONObject jo = JSONObject.parseObject(json);
        if (!jo.getString("status").equals("0")) {
            throw new OrderBusinessException("获得距离解析失败");
        }
        JSONArray jsonArray = jo.getJSONObject("result").getJSONArray("routes");
        Integer distance = (Integer) ((JSONObject) jsonArray.get(0)).get("distance");

        //判断距离是否合理
        if (distance > 5000) {
            throw new OrderBusinessException("超出距离限制");
        }
    }
}
