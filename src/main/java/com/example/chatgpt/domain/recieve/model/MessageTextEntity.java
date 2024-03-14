package com.example.chatgpt.domain.recieve.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageTextEntity {
    // 消息ID
    private String msgId;
    // 开发者微信号
    private String toUserName;
    // 发送方OpenID
    private String fromUserName;
    // 消息创建时间
    private String createTime;
    // 消息类型
    private String msgType;
    // 文本消息内容
    private String content;
    // 事件
    private String event;
    // 事件Key
    private String eventKey;
}
