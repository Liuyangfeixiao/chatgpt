package org.example.chatgpt.data.domain.weixin.service;

import org.example.chatgpt.data.domain.weixin.model.entity.UserBehaviorMessageEntity;

/**
 * 受理用户行为接口
 */
public interface IWeChatBehaviorService {
    String acceptUserBehavior(UserBehaviorMessageEntity userBehaviorMessageEntity);
}
