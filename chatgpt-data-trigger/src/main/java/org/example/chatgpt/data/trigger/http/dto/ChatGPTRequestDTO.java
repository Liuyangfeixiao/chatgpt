package org.example.chatgpt.data.trigger.http.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.chatgpt.data.types.enums.ChatGLMModel;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatGPTRequestDTO {
    /**默认模型*/
    private String model = ChatGLMModel.GLM_3_5_TURBO.getCode();
    /** 问题描述 */
    private List<MessageEntity> messages;
}
