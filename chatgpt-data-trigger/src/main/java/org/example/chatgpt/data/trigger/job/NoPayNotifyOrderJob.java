package org.example.chatgpt.data.trigger.job;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.domain.AlipayTradeQueryModel;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.google.common.eventbus.EventBus;
import lombok.extern.slf4j.Slf4j;
import org.example.chatgpt.data.domain.order.service.IOrderService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @description 检测没有接收或者没有正确处理的支付回调通知
 */
@Slf4j
@Component
public class NoPayNotifyOrderJob {
    @Resource
    private IOrderService orderService;
    @Resource
    private AlipayClient alipayClient;
    @Resource
    private EventBus eventBus;
    
    @Scheduled(cron = "0 0/1 * * * ?")
    public void exec() {
        // 首先查询没有收到支付回调的订单
        // 具体为查询已创建但在等待支付，并超过1分钟的订单
        List<String> orderIds = orderService.queryNoPayNotifyOrder();
        if (orderIds.isEmpty()) {
            log.info("定时任务，订单支付状态更新，暂无需要更新订单 orderId is null");
            return;
        }
        for (String orderId : orderIds) {
            // 构造请求参数，调用支付宝接口
            AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
            AlipayTradeQueryModel model = new AlipayTradeQueryModel();
            // 设置订单支付时传入的商户订单号
            model.setOutTradeNo(orderId);
            // 设置查询选项
            List<String> queryOptions = new ArrayList<String>();
            queryOptions.add("trade_settle_info");
            model.setQueryOptions(queryOptions);
            
            request.setBizModel(model);
            try {
                AlipayTradeQueryResponse response = alipayClient.execute(request);
                if (!response.isSuccess()) {
                    log.error("定时任务,订单支付状态更新，调用支付宝SDK查询订单失败 orderId: {}", orderId);
                    continue;
                } else {
                    log.info("定时任务,订单支付状态更新，调用支付宝SDK查询订单成功");
                }
                if ("WAIT_BUYER_PAY".equals(response.getTradeStatus())) {
                    // 交易创建，等待买家付款
                    log.info("定时任务，订单支付状态更新，当前订单未支付 orderId is {}", orderId);
                    continue;
                }
                if ("TRADE_SUCCESS".equals(response.getTradeStatus()) ||
                        "TRADE_FINISHED".equals(response.getTradeStatus())) {
                    // 此时才认为用户支付成功, 更改订单信息
                    String transactionId = response.getTradeNo(); // 支付宝交易单号
                    BigDecimal totalAmount = new BigDecimal(response.getTotalAmount()); // 交易的订单金额，单位为元，两位小数
                    // 打款给卖家时间
                    Date payTime = response.getSendPayDate();
                    // 更新订单信息
                    boolean isSuccess = orderService.changeOrderPaySuccess(orderId, transactionId, totalAmount, payTime);
                    // 发布支付成功消息，准备发货
                    if (isSuccess) {
                        eventBus.post(orderId);
                    }
                }
            } catch (Exception e) {
                log.error("定时任务，订单支付状态更新，查询支付宝订单信息失败", e);
            }
        }
    }
}
