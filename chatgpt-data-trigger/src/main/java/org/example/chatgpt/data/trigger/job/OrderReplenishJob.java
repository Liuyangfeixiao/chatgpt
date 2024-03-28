package org.example.chatgpt.data.trigger.job;

import com.google.common.eventbus.EventBus;
import lombok.extern.slf4j.Slf4j;
import org.example.chatgpt.data.domain.order.service.IOrderService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Component
public class OrderReplenishJob {
    @Resource
    private IOrderService orderService;
    @Resource
    private EventBus eventBus;
    /**
     * 执行订单补货，超时3分钟并且[已支付，待发货]的订单进行补货
     */
    @Scheduled(cron = "0 0/10 * * * ?")
    public void exec() {
        try {
            List<String> orderIds = orderService.queryReplenishmentOrder();
            if (orderIds.isEmpty()) {
                log.info("定时任务，没有需要补货的订单，查询 orderId is null");
                return;
            }
            for (String orderId : orderIds) {
                log.info("定时任务，订单补货开始, orderId: {}", orderId);
                eventBus.post(orderId);
            }
        } catch (Exception e) {
            log.error("定时任务，订单补货失败", e);
        }
    }
}
