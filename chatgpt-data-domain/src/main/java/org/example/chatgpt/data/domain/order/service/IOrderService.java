package org.example.chatgpt.data.domain.order.service;

import org.example.chatgpt.data.domain.order.model.aggregates.CreateOrderAggregate;
import org.example.chatgpt.data.domain.order.model.entity.PayOrderEntity;
import org.example.chatgpt.data.domain.order.model.entity.ProductEntity;
import org.example.chatgpt.data.domain.order.model.entity.ShopCarEntity;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @description 订单服务
 */
public interface IOrderService {
    /**
     * @description 用户下单，通过购物车信息，返回下单后的支付单
     * @param shopCar 简单购物车对象
     * @return 支付单实体对象
     */
    PayOrderEntity createOrder(ShopCarEntity shopCar);
    
    /**
     * @description 变更订单信息，成功支付
     * @param orderId 订单ID
     * @param transactionId 事务ID
     * @param totalAmount 总支付金额
     * @param payTime 支付时间
     * @return 是否成功
     */
    boolean changeOrderPaySuccess(String orderId, String transactionId, BigDecimal totalAmount, Date payTime);
    
    /**
     * @description 查询某个订单信息
     * @param orderId
     * @return
     */
    CreateOrderAggregate queryOrder(String orderId);
    
    /**
     * @description 订单商品发货
     * @param orderId
     */
    void deliverGoods(String orderId);
    
    /**
     * @description 查询待补货订单orderId
     * @return  一串订单ID
     */
    List<String> queryReplenishmentOrder();
    /**
     * @description 查询有效期内，未接收到支付回调的订单
     */
    List<String> queryNoPayNotifyOrder();
    /**
     * @description 查询超时未支付订单
     */
    List<String> queryTimeoutCloseOrder();
    /**
     * @description 变更订单信息：订单支付关闭
     */
    boolean changeOrderClose(String orderId);
    /**
     * @description 查询商品列表
     */
    List<ProductEntity> queryProductList();
}
