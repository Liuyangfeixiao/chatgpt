package org.example.chatgpt.data.domain.order.service;

import lombok.extern.slf4j.Slf4j;
import org.example.chatgpt.data.domain.order.model.entity.OrderEntity;
import org.example.chatgpt.data.domain.order.model.entity.PayOrderEntity;
import org.example.chatgpt.data.domain.order.model.entity.ProductEntity;
import org.example.chatgpt.data.domain.order.model.entity.ShopCarEntity;
import org.example.chatgpt.data.domain.order.model.vo.PayStatusVO;
import org.example.chatgpt.data.domain.order.repository.IOrderRepository;
import org.example.chatgpt.data.types.common.Constants;
import org.example.chatgpt.data.types.exception.ChatGPTException;

import javax.annotation.Resource;
import java.math.BigDecimal;

@Slf4j
public abstract class AbstractOrderService implements IOrderService {
    @Resource
    protected IOrderRepository orderRepository;
    
    @Override
    public PayOrderEntity createOrder(ShopCarEntity shopCar) {
        try {
            // 0. 基础信息
            String openid = shopCar.getOpenid();
            Integer productId = shopCar.getProductId();
            // 1. 查询是否有未支付的订单
            PayOrderEntity unpaidOrderEntity = orderRepository.queryUnpaidOrder(shopCar);
            if (null != unpaidOrderEntity) {
                if (PayStatusVO.WAIT.equals(unpaidOrderEntity.getPayStatus()) && null != unpaidOrderEntity.getPayUrl()) {
                    log.info("创建订单-存在，已生成支付宝支付单，返回 openid: {} orderId: {}\n payUrl: {}", openid, unpaidOrderEntity.getOrderId(), unpaidOrderEntity.getPayUrl());
                    return PayOrderEntity.builder()
                            .openid(openid)
                            .orderId(unpaidOrderEntity.getOrderId())
                            .productName(unpaidOrderEntity.getProductName())
                            .totalAmount(unpaidOrderEntity.getTotalAmount())
                            .payUrl(unpaidOrderEntity.getPayUrl())
                            .payStatus(unpaidOrderEntity.getPayStatus()).build();
                } else if (unpaidOrderEntity.getPayUrl() == null) {
                    log.info("创建订单-存在，未生成支付宝支付单，返回 openid: {} orderId: {}", openid, unpaidOrderEntity.getOrderId());
                    PayOrderEntity payOrderEntity = this.doPrepayOrder(openid, unpaidOrderEntity.getOrderId(), unpaidOrderEntity.getProductName(), unpaidOrderEntity.getTotalAmount());
                    log.info("创建订单-完成，生成支付单。openid: {} orderId: {} payUrl: {}", openid, payOrderEntity.getOrderId(), payOrderEntity.getPayUrl());
                    return payOrderEntity;
                }
                
            }
            // 2. 商品查询
            ProductEntity productEntity = orderRepository.queryProduct(productId);
            if (!productEntity.isAvailable()) {
                throw new ChatGPTException(Constants.ResponseCode.ORDER_PRODUCT_ERR.getCode(), Constants.ResponseCode.ORDER_PRODUCT_ERR.getInfo());
            }
            
            // 3. 先保存订单
            OrderEntity orderEntity = this.doSaveOrder(openid, productEntity);
            
            // 4. 创建支付
            PayOrderEntity payOrderEntity = this.doPrepayOrder(openid, orderEntity.getOrderId(), productEntity.getProductName(), orderEntity.getTotalAmount());
            log.info("创建订单-完成，生成支付单。openid: {} orderId: {} payUrl: {}", openid, orderEntity.getOrderId(), payOrderEntity.getPayUrl());
            
            return payOrderEntity;
        } catch (Exception e) {
            log.error("创建订单，已生成微信支付，返回 openid: {} productId: {}", shopCar.getOpenid(), shopCar.getProductId(), e);
            throw new ChatGPTException(Constants.ResponseCode.UN_ERROR.getCode(), Constants.ResponseCode.UN_ERROR.getInfo());
        }
    }
    
    protected abstract OrderEntity doSaveOrder(String openid, ProductEntity productEntity);
    
    protected abstract PayOrderEntity doPrepayOrder(String openid, String orderId, String productName, BigDecimal amountTotal);
}
