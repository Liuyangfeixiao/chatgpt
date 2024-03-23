package org.example.chatgpt.data.domain.weixin.service.validate;

import org.example.chatgpt.data.domain.weixin.service.IWeChatValidateService;
import org.example.chatgpt.data.types.sdk.weixin.SignatureUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class WeChatValidateService implements IWeChatValidateService {
    @Value("${wx.config.token}")
    private String token;
    @Override
    public boolean checkSign(String signature, String timestamp, String nonce) {
        return SignatureUtil.check(token, signature, timestamp, nonce);
    }
}
