package org.example.chatgpt.data.domain.openai.service;

import cn.bugstack.chatglm.model.ChatCompletionSyncResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.example.chatgpt.data.domain.openai.model.aggregates.ChatProcessAggregate;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.util.List;

public class ChatService extends AbstractChatService{
    @Override
    protected void doMessageResponse(ChatProcessAggregate chatProcess, ResponseBodyEmitter emitter) throws JsonProcessingException {
        // 1. 请求消息

    }
}
