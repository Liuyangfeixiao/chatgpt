package org.example.chatgpt.data.domain.weixin.service.message;

import com.google.common.cache.Cache;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.example.chatgpt.data.domain.weixin.model.entity.MessageTextEntity;
import org.example.chatgpt.data.domain.weixin.model.entity.UserBehaviorMessageEntity;
import org.example.chatgpt.data.domain.weixin.model.vo.MsgTypeVO;
import org.example.chatgpt.data.domain.weixin.service.IWeChatBehaviorService;
import org.example.chatgpt.data.types.exception.ChatGPTException;
import org.example.chatgpt.data.types.sdk.weixin.XmlUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class WeChatBehaviorService implements IWeChatBehaviorService {
    @Value("${wx.config.originalid}")
    private String originalId;
    @Resource
    private Cache<String, String> codeCache;
    @Override
    public String acceptUserBehavior(UserBehaviorMessageEntity userBehaviorMessageEntity) {
        // 判断事件类型，产生验证码
        // Event事件类型，忽略处理
        if (MsgTypeVO.EVENT.getCode().equals(userBehaviorMessageEntity.getMsgType())) {
            return "";
        }
        // Text文本类型
        if (MsgTypeVO.TEXT.getCode().equals(userBehaviorMessageEntity.getMsgType())) {
            // 缓存验证码
            String isExistCode = codeCache.getIfPresent(userBehaviorMessageEntity.getOpenId());
            // 判断验证码是否存在
            if (StringUtils.isBlank(isExistCode)) {
                // 创建验证码
                String code = RandomStringUtils.randomNumeric(4);
                codeCache.put(code, userBehaviorMessageEntity.getOpenId());
                codeCache.put(userBehaviorMessageEntity.getOpenId(), code);
                isExistCode = code;
            }
            // 反馈信息[文本]
            MessageTextEntity result = MessageTextEntity.builder()
                    .toUserName(userBehaviorMessageEntity.getOpenId())
                    .fromUserName(originalId)
                    .createTime(String.valueOf(System.currentTimeMillis() / 1000L))
                    .msgType("text")
                    .content(String.format("您的验证码为: %s, 有效期%d分钟", isExistCode, 3))
                    .build();
            return XmlUtil.beanToXml(result);
        }
        throw new ChatGPTException(userBehaviorMessageEntity.getMsgType() + " 未被处理的行为类型 Err");
    }
}
