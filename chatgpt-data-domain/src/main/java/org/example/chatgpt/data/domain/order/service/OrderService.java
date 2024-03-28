package org.example.chatgpt.data.domain.order.service;

import cn.hutool.db.sql.Order;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.example.chatgpt.data.domain.order.model.aggregates.CreateOrderAggregate;
import org.example.chatgpt.data.domain.order.model.entity.OrderEntity;
import org.example.chatgpt.data.domain.order.model.entity.PayOrderEntity;
import org.example.chatgpt.data.domain.order.model.entity.ProductEntity;
import org.example.chatgpt.data.domain.order.model.vo.OrderStatusVO;
import org.example.chatgpt.data.domain.order.model.vo.PayStatusVO;
import org.example.chatgpt.data.domain.order.model.vo.PayTypeVO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class OrderService extends AbstractOrderService {
    @Resource
    private AlipayClient alipayClient;
    @Value("${alipay.notify_url}")
    private String notifyUrl;
    @Value("${alipay.return_url}")
    private String returnUrl;
    @Override
    protected OrderEntity doSaveOrder(String openid, ProductEntity productEntity) {
        OrderEntity orderEntity = new OrderEntity();
        // 数据库有幂等拦截，有重复的订单ID会报错主键冲突
        // 公司里会有专门的雪花算法做 UUID 服务
        orderEntity.setOrderId(RandomStringUtils.randomNumeric(12));
        orderEntity.setOrderTime(new Date());
        orderEntity.setOrderStatus(OrderStatusVO.CREATE);
        orderEntity.setTotalAmount(productEntity.getPrice());
        orderEntity.setPayType(PayTypeVO.ALIPAY);
        // 生成聚合信息
        CreateOrderAggregate aggregate = CreateOrderAggregate.builder()
                .openid(openid)
                .product(productEntity)
                .order(orderEntity)
                .build();
        // 保存订单；订单和支付，是2个操作。
        // 一个是数据库操作，一个是HTTP操作。所以不能一个事务处理，只能先保存订单再操作创建支付单，如果失败则需要任务补偿
        orderRepository.saveOrder(aggregate);
        return orderEntity;
    }
    
    @Override
    protected PayOrderEntity doPrepayOrder(String openid, String orderId, String productName, BigDecimal amountTotal) {
        // 创建支付订单
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        // 异步回调地址，公网可访问
        request.setNotifyUrl(notifyUrl);
        // 同步跳转地址
        request.setReturnUrl(returnUrl);
        /**必传参数**/
        JSONObject bizContent = new JSONObject();
        //商户订单号，商家自定义，保持唯一性
        bizContent.put("out_trade_no", orderId);
        //支付金额，最小值0.01元
        bizContent.put("total_amount", amountTotal.toString());
        // 订单标题，不可使用特殊符号
        bizContent.put("subject", productName);
        //电脑网站支付场景固定传值FAST_INSTANT_TRADE_PAY
        bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");
        
        request.setBizContent(bizContent.toString());
        String codeUrl = "";
        try {
            AlipayTradePagePayResponse response = alipayClient.pageExecute(request,"POST");
            codeUrl = response.getBody();
            log.info(codeUrl);
        } catch (Exception e) {
            codeUrl = "支付宝SDK调用失败，无法生成跳转页面";
            log.error("doPrepayOrder 调用支付宝SDK失败", e);
        }
        // 更新支付订单信息
        PayOrderEntity payOrderEntity = PayOrderEntity.builder()
                .openid(openid)
                .orderId(orderId)
                .payStatus(PayStatusVO.WAIT)
                .payUrl(codeUrl)
                .build();
        
        // 更新订单支付信息
        orderRepository.updateOrderPayInfo(payOrderEntity);
        return payOrderEntity;
    }
    
    @Override
    public boolean changeOrderPaySuccess(String orderId, String transactionId, BigDecimal totalAmount, Date payTime) {
        return orderRepository.changeOrderPaySuccess(orderId, transactionId, totalAmount, payTime);
    }
    
    @Override
    public CreateOrderAggregate queryOrder(String orderId) {
        return orderRepository.queryOrder(orderId);
    }
    
    @Override
    public void deliverGoods(String orderId) {
        orderRepository.deliverGoods(orderId);
    }
    
    @Override
    public List<String> queryReplenishmentOrder() {
        return orderRepository.queryReplenishmentOrder();
    }
    
    @Override
    public List<String> queryNoPayNotifyOrder() {
        return orderRepository.queryNoPayNotifyOrder();
    }
    
    @Override
    public List<String> queryTimeoutCloseOrder() {
        return orderRepository.queryTimeoutCloseOrderList();
    }
    
    @Override
    public boolean changeOrderClose(String orderId) {
        return orderRepository.changeOrderClose(orderId);
    }
    
    @Override
    public List<ProductEntity> queryProductList() {
        return orderRepository.queryProductList();
    }
}
