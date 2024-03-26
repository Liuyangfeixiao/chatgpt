package org.example.chatgpt.data.domain.order.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.chatgpt.data.domain.order.model.vo.PayStatusVO;

import java.math.BigDecimal;

/**
 * @description 支付单信息
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PayOrderEntity {
    /**用户ID**/
    private String openid;
    /**订单ID**/
    private String orderId;
    /**
     * 订单金额
     */
    private BigDecimal totalAmount;
    /**
     * 商品名称
     */
    private String productName;
    /**
     * 支付地址，创建支付后，获得的URL或者HTML
     */
    private String payUrl;
    /**
     * 支付状态：0-等待支付, 1-支付完成, 2-支付失败, 3-放弃支付
     */
    PayStatusVO payStatus;
    
    @Override
    public String toString() {
        return "PayOrderEntity{" +
                "openid='" + openid + '\'' +
                ", orderId='" + orderId + '\'' +
                ", totalAmount='" + totalAmount + '\''+
                ", productName='" + productName + '\''+
                ", payUrl='" + payUrl + '\'' +
                ", payStatus=" + payStatus.getCode() + ": " + payStatus.getDesc() +
                '}';
    }
}
