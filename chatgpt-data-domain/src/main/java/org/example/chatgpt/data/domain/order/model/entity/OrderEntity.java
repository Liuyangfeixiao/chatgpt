package org.example.chatgpt.data.domain.order.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.chatgpt.data.domain.order.model.vo.OrderStatusVO;
import org.example.chatgpt.data.domain.order.model.vo.PayTypeVO;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 订单信息
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderEntity {
    /**订单编号**/
    private String orderId;
    /**下单时间**/
    private Date orderTime;
    /**订单状态：0-创建完成，1-等待发货, 2-发货完成, 3-系统关闭订单**/
    private OrderStatusVO orderStatus;
    /**订单金额**/
    private BigDecimal totalAmount;
    /** 支付类型: 0-微信支付, 1-支付宝支付**/
    private PayTypeVO payType;
}
