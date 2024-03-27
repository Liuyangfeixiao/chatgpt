package org.example.chatgpt.data.infrastructure.dao;

import org.apache.ibatis.annotations.Mapper;
import org.example.chatgpt.data.infrastructure.po.OrderPO;

import java.util.List;

@Mapper
public interface IOrderDao {
    OrderPO queryUnpaidOrder(OrderPO order);
    
    void insert(OrderPO order);
    void updateOrderPayInfo(OrderPO order);
    int changeOrderPaySuccess(OrderPO order);
    OrderPO queryOrder(String orderId);
    int updateOrderStatusDeliverGoods(String orderId);
    List<String> queryReplenishmentOrder();
    List<String> queryNoPayNotifyOrder();
    List<String> queryTimeoutCloseOrderList();
    boolean changeOrderClose(String orderId);
}
