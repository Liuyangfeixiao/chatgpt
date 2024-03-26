package org.example.chatgpt.data.domain.order.model.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProductStatusVO {
    CLOSE(0, "无效，已关闭"),
    OPEN(1, "有效，使用中"),
    ;
    private final Integer code;
    private final String info;
    
    public static ProductStatusVO get(Integer code) {
        switch (code) {
            case 0:
                return ProductStatusVO.CLOSE;
            case 1:
                return ProductStatusVO.OPEN;
            default:
                return ProductStatusVO.CLOSE;
        }
    }
    
}
