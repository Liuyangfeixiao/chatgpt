package org.example.chatgpt.data.domain.order.model.aggregates;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.chatgpt.data.domain.order.model.entity.OrderEntity;
import org.example.chatgpt.data.domain.order.model.entity.ProductEntity;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateOrderAggregate {
    /**
     * 用户ID，用户唯一标识
     */
    private String openid;
    /**
     * 商品信息
     */
    private ProductEntity product;
    /**
     * 订单信息
     */
    private OrderEntity order;
}
