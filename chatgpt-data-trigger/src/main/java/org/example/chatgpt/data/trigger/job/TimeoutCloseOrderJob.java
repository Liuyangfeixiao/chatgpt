package org.example.chatgpt.data.trigger.job;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradeCloseRequest;
import com.alipay.api.response.AlipayTradeCloseResponse;
import lombok.extern.slf4j.Slf4j;
import org.example.chatgpt.data.domain.order.service.IOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Component
public class TimeoutCloseOrderJob {
    @Resource
    private IOrderService orderService;
    @Resource
    AlipayClient alipayClient;
    
    /**
     * @description 将超时订单关闭 [已创建, 未支付, 超时30分钟]
     */
    @Scheduled(cron = "0 0/10 * * * ?")
    public void exec() {
        List<String> orderIds = orderService.queryTimeoutCloseOrder();
        if (orderIds.isEmpty()) {
            log.info("定时任务，超过30分钟订单关闭，暂无超时未支付订单");
            return;
        }
        for (String orderId : orderIds) {
            // 构建参数，传入支付宝
            AlipayTradeCloseRequest request = new AlipayTradeCloseRequest();
            JSONObject bizContent = new JSONObject();
            bizContent.put("out_trade_no", orderId);
            request.setBizContent(bizContent.toString());
            try {
                AlipayTradeCloseResponse response = alipayClient.execute(request);
                if (!response.isSuccess()) {
                    log.error("定时任务, 超过30分钟关闭订单，调用支付宝SDK关闭订单失败, orderId: {}", orderId);
                    continue;
                }
                boolean status = orderService.changeOrderClose(orderId);
                if (!status) {
                    log.error("定时任务，超过30分钟关闭订单, 更新订单状态失败 orderId: {}", orderId);
                }
            } catch (Exception e) {
                log.error("定时任务, 超过30分钟关闭订单，支付宝SDK调用失败, orderId: {}", orderId, e);
                return;
            }
            
        }
    }
}
