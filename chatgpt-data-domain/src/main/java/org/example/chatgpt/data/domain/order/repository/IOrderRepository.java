package org.example.chatgpt.data.domain.order.repository;

import org.example.chatgpt.data.domain.order.model.aggregates.CreateOrderAggregate;
import org.example.chatgpt.data.domain.order.model.entity.PayOrderEntity;
import org.example.chatgpt.data.domain.order.model.entity.ProductEntity;
import org.example.chatgpt.data.domain.order.model.entity.ShopCarEntity;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @description 订单仓储接口
 */
public interface IOrderRepository {
    PayOrderEntity queryUnpaidOrder(ShopCarEntity shopCar);
    ProductEntity queryProduct(Integer productId);
    void saveOrder(CreateOrderAggregate aggregate);
    void updateOrderPayInfo(PayOrderEntity payOrderEntity);
    boolean changeOrderPaySuccess(String orderId, String transactionId, BigDecimal totalAmount, Date payTime);
    CreateOrderAggregate queryOrder(String orderId);
    
    void deliverGoods(String orderId);
    
    List<String> queryReplenishmentOrder();
    
    List<String> queryNoPayNotifyOrder();
    
    List<String> queryTimeoutCloseOrderList();
    
    boolean changeOrderClose(String orderId);
    
    List<ProductEntity> queryProductList();
    
    
}
