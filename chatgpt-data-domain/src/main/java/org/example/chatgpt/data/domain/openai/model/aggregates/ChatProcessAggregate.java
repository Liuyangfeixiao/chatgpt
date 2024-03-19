package org.example.chatgpt.data.domain.openai.model.aggregates;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.chatgpt.data.domain.openai.model.entity.MessageEntity;
import org.example.chatgpt.data.types.enums.ChatGLMModel;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatProcessAggregate {
    /**用户ID*/
    String openid;
    /**验证信息*/
    private String token;
    /**默认模型*/
    private String model = ChatGLMModel.CHATGLM_3_TURBO.getCode();
    /**问题描述*/
    private List<MessageEntity> messages;
}
