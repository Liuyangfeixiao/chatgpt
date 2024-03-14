package com.example.chatgpt.application;

public interface IWeChatValidateService {
    /**
     * @description 公众号校验签名
     * @param signature 签名
     * @param timestamp 时间戳
     * @param nonce 随机数
     * @return 签名校验是否成功
     */
    boolean checkSign(String signature, String timestamp, String nonce);
}
