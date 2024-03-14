package com.example.chatgpt.domain.validate;

import com.example.chatgpt.application.IWeChatValidateService;
import com.example.chatgpt.infrastracture.util.wechat.SignatureUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
public class WeChatValidateServiceImpl implements IWeChatValidateService {
    @Value("${wx.config.token}")
    private String token;
    @Override
    public boolean checkSign(String signature, String timestamp, String nonce) {
        return SignatureUtil.check(signature, timestamp, nonce, token);
    }
}
