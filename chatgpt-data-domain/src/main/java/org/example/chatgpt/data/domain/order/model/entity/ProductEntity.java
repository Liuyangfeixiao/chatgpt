package org.example.chatgpt.data.domain.order.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.chatgpt.data.domain.order.model.vo.ProductStatusVO;

import java.math.BigDecimal;


/**
 * @description 商品实体信息
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductEntity {
    /**
     * 商品ID
     */
    private Integer productId;
    /**
     * 商品名称
     */
    private String productName;
    /**
     * 商品描述
     */
    private String productDesc;
    /**
     * 额度次数
     */
    private Integer quota;
    /**
     * 商品价格
     */
    private BigDecimal price;
    /**
     * 商品状态: 0-无效, 1-有效
     */
    private ProductStatusVO enable;
    /**
     * 判断是否有效, true=有效, false=无效
     */
    public boolean isAvailable() {
        return ProductStatusVO.OPEN.equals(enable);
    }
}
