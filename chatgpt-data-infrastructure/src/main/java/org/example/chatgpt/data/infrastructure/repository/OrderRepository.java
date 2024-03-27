package org.example.chatgpt.data.infrastructure.repository;

import cn.hutool.db.sql.Order;
import org.example.chatgpt.data.domain.openai.model.vo.UserAccountStatusVO;
import org.example.chatgpt.data.domain.order.model.aggregates.CreateOrderAggregate;
import org.example.chatgpt.data.domain.order.model.entity.OrderEntity;
import org.example.chatgpt.data.domain.order.model.entity.PayOrderEntity;
import org.example.chatgpt.data.domain.order.model.entity.ProductEntity;
import org.example.chatgpt.data.domain.order.model.entity.ShopCarEntity;
import org.example.chatgpt.data.domain.order.model.vo.OrderStatusVO;
import org.example.chatgpt.data.domain.order.model.vo.PayStatusVO;
import org.example.chatgpt.data.domain.order.model.vo.ProductStatusVO;
import org.example.chatgpt.data.domain.order.repository.IOrderRepository;
import org.example.chatgpt.data.infrastructure.dao.IOrderDao;
import org.example.chatgpt.data.infrastructure.dao.IProductDao;
import org.example.chatgpt.data.infrastructure.dao.IUserAccountDao;
import org.example.chatgpt.data.infrastructure.po.OrderPO;
import org.example.chatgpt.data.infrastructure.po.ProductPO;
import org.example.chatgpt.data.infrastructure.po.UserAccountPO;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Repository
public class OrderRepository implements IOrderRepository {
    @Resource
    private IOrderDao orderDao;
    @Resource
    private IProductDao productDao;
    @Resource
    private IUserAccountDao userAccountDao;
    @Override
    public PayOrderEntity queryUnpaidOrder(ShopCarEntity shopCar) {
        OrderPO orderPOReq = new OrderPO();
        orderPOReq.setOpenid(shopCar.getOpenid());
        orderPOReq.setProductId(shopCar.getProductId());
        OrderPO orderPORes = orderDao.queryUnpaidOrder(orderPOReq);
        // 没有未支付订单, 返回null
        if (null == orderPORes) return null;
        return PayOrderEntity.builder()
                .openid(shopCar.getOpenid())
                .orderId(orderPORes.getOrderId())
                .productName(orderPORes.getProductName())
                .totalAmount(orderPORes.getTotalAmount())
                .payUrl(orderPORes.getPayUrl())
                .payStatus(PayStatusVO.get(orderPORes.getPayStatus()))
                .build();
    }
    
    @Override
    public ProductEntity queryProduct(Integer productId) {
        ProductPO productPO = productDao.queryProductByProductId(productId);
        ProductEntity product = new ProductEntity();
        product.setProductId(productPO.getProductId());
        product.setProductDesc(productPO.getProductDesc());
        product.setProductName(productPO.getProductName());
        product.setQuota(productPO.getQuota());
        product.setPrice(productPO.getPrice());
        product.setEnable(ProductStatusVO.get(productPO.getIsEnabled()));
        return product;
    }
    
    @Override
    public void saveOrder(CreateOrderAggregate aggregate) {
        String openid = aggregate.getOpenid();
        ProductEntity product = aggregate.getProduct();
        OrderEntity order = aggregate.getOrder();
        OrderPO orderPO = new OrderPO();
        orderPO.setOpenid(openid);
        orderPO.setProductId(product.getProductId());
        orderPO.setProductName(product.getProductName());
        orderPO.setProductQuota(product.getQuota());
        orderPO.setOrderId(order.getOrderId());
        orderPO.setOrderTime(order.getOrderTime());
        orderPO.setOrderStatus(order.getOrderStatus().getCode());
        orderPO.setTotalAmount(order.getTotalAmount());
        orderPO.setPayType(order.getPayType().getCode());
        // 此时刚创建支付单, 需要等待支付
        orderPO.setPayStatus(PayStatusVO.WAIT.getCode());
        // pay_url 在 prePayOrder 之后生成
        // pay_amount 和 pay_time 在回调时更新
        orderDao.insert(orderPO);
    }
    
    @Override
    public void updateOrderPayInfo(PayOrderEntity payOrderEntity) {
        OrderPO orderPO = new OrderPO();
        orderPO.setOpenid(payOrderEntity.getOpenid());
        orderPO.setOrderId(payOrderEntity.getOrderId());
        orderPO.setPayUrl(payOrderEntity.getPayUrl());
        orderPO.setPayStatus(payOrderEntity.getPayStatus().getCode());
        orderDao.updateOrderPayInfo(orderPO);
    }
    
    @Override
    public boolean changeOrderPaySuccess(String orderId, String transactionId, BigDecimal totalAmount, Date payTime) {
        OrderPO orderPO = new OrderPO();
        orderPO.setOrderId(orderId);
        orderPO.setPayTime(payTime);
        orderPO.setPayAmount(totalAmount);
        orderPO.setTransactionId(transactionId);
        int count = orderDao.changeOrderPaySuccess(orderPO);
        return count == 1;
    }
    
    @Override
    public CreateOrderAggregate queryOrder(String orderId) {
        OrderPO orderPO = orderDao.queryOrder(orderId);
        ProductEntity product = new ProductEntity();
        product.setProductId(orderPO.getProductId());
        product.setProductName(orderPO.getProductName());
        
        OrderEntity order = new OrderEntity();
        order.setOrderId(orderPO.getOrderId());
        order.setOrderTime(orderPO.getOrderTime());
        order.setOrderStatus(OrderStatusVO.get(orderPO.getOrderStatus()));
        order.setTotalAmount(orderPO.getTotalAmount());
        
        CreateOrderAggregate createOrderAggregate = new CreateOrderAggregate();
        createOrderAggregate.setOpenid(orderPO.getOpenid());
        createOrderAggregate.setOrder(order);
        createOrderAggregate.setProduct(product);
        
        return createOrderAggregate;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 350, propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT)
    public void deliverGoods(String orderId) {
        OrderPO orderPO = orderDao.queryOrder(orderId);
        // 变更发货状态
        int updateCount = orderDao.updateOrderStatusDeliverGoods(orderId);
        if (1 != updateCount) {
            throw new RuntimeException("updateOrderStatusDeliverGoods的update count返回值不为1");
        }
        // 账户额度变更
        UserAccountPO userAccountPO = userAccountDao.queryUserAccount(orderPO.getOpenid());
        UserAccountPO userAccountPOReq = new UserAccountPO();
        userAccountPOReq.setOpenid(orderPO.getOpenid());
        userAccountPOReq.setTotalQuota(orderPO.getProductQuota());
        userAccountPOReq.setSurplusQuota(orderPO.getProductQuota());
        if (null != userAccountPO){
            int addAccountQuotaCount = userAccountDao.addAccountQuota(userAccountPOReq);
            if (1 != addAccountQuotaCount) throw new RuntimeException("addAccountQuotaCount的update count返回值不为1");
        } else {
            userAccountPOReq.setStatus(UserAccountStatusVO.AVAILABLE.getCode());
            userAccountPOReq.setModelTypes("glm-3-turbo,glm-4,glm-4v,cogview-3");
            userAccountDao.insert(userAccountPOReq);
        }
        
    }
    
    @Override
    public List<String> queryReplenishmentOrder() {
        return orderDao.queryReplenishmentOrder();
    }
    
    @Override
    public List<String> queryNoPayNotifyOrder() {
        return orderDao.queryNoPayNotifyOrder();
    }
    
    @Override
    public List<String> queryTimeoutCloseOrderList() {
        return orderDao.queryTimeoutCloseOrderList();
    }
    
    @Override
    public boolean changeOrderClose(String orderId) {
        return orderDao.changeOrderClose(orderId);
    }
    
    @Override
    public List<ProductEntity> queryProductList() {
        // 查询商品列表
        List<ProductPO> productPOList = productDao.queryProductList();
        List<ProductEntity> productEntityList = new ArrayList<>(productPOList.size());
        for (ProductPO productPO : productPOList) {
            ProductEntity productEntity = new ProductEntity();
            productEntity.setProductId(productPO.getProductId());
            productEntity.setProductName(productPO.getProductName());
            productEntity.setProductDesc(productPO.getProductDesc());
            productEntity.setQuota(productPO.getQuota());
            productEntity.setPrice(productPO.getPrice());
            productEntityList.add(productEntity);
        }
        return productEntityList;
    }
}
