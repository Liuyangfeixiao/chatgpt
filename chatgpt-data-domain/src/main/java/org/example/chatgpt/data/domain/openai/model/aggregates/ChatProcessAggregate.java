package org.example.chatgpt.data.domain.openai.model.aggregates;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.chatgpt.data.domain.openai.model.entity.MessageEntity;
import org.example.chatgpt.data.types.common.Constants;
import org.example.chatgpt.data.types.enums.ChatGLMModel;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatProcessAggregate {
    /**微信用户ID*/
    String openid;
    /**验证信息*/
    private String token;
    /**默认模型*/
    private String model = ChatGLMModel.GLM_3_5_TURBO.getCode();
    /**问题描述*/
    private List<MessageEntity> messages;
    
    public boolean isWhiteList(String whiteList) {
        String[] list = whiteList.split(Constants.SPLIT);
        for (String openID : list) {
            if (openID.equals(openid)) return true;
        }
        return false;
    }
}
