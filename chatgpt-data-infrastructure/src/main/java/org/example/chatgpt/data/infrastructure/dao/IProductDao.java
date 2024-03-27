package org.example.chatgpt.data.infrastructure.dao;

import org.apache.ibatis.annotations.Mapper;
import org.example.chatgpt.data.infrastructure.po.ProductPO;

import java.util.List;

@Mapper
public interface IProductDao {
    /**
     * @description 查询单个商品
     * @param productId
     * @return
     */
    ProductPO queryProductByProductId(Integer productId);
    
    /**
     * @description 查询商品列表
     * @return
     */
    List<ProductPO> queryProductList();
}
