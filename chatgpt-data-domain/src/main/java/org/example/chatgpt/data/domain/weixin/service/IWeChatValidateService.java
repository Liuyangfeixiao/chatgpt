package org.example.chatgpt.data.domain.weixin.service;

public interface IWeChatValidateService {
    /**
     * @description 微信验签服务
     * @param signature
     * @param timestamp
     * @param nonce
     * @return
     */
    boolean checkSign(String signature, String timestamp, String nonce);
}
