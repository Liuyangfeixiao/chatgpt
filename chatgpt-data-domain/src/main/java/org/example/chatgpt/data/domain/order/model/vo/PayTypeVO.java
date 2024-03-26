package org.example.chatgpt.data.domain.order.model.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PayTypeVO {
    WEIXIN_NATIVE(0, "微信Native支付"),
    ALIPAY(1, "支付宝支付"),
    ;
    private final Integer code;
    private final String desc;
    public static PayTypeVO get(Integer code) {
        switch (code) {
            case 0:
                return PayTypeVO.WEIXIN_NATIVE;
            case 1:
                return PayTypeVO.ALIPAY;
            default:
                return PayTypeVO.ALIPAY;
        }
    }
}
