package org.example.chatgpt.data.domain.weixin.model.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum MsgTypeVO {
    EVENT("event","事件消息"),
    TEXT("text","文本消息");
    
    private String code;
    private String desc;
}
