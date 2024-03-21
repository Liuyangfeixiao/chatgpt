package org.example.chatgpt.data.domain.openai.service;

import cn.bugstack.chatglm.model.*;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import org.example.chatgpt.data.domain.openai.model.aggregates.ChatProcessAggregate;
import org.example.chatgpt.data.types.enums.ChatGLMModel;
import org.example.chatgpt.data.types.exception.ChatGPTException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ChatService extends AbstractChatService{
    @Override
    protected void doMessageResponse(ChatProcessAggregate chatProcess, ResponseBodyEmitter emitter) {
        try {
            // 1. 请求消息
            List<ChatCompletionRequest.Prompt> messages = chatProcess.getMessages().stream()
                    .map(entity -> ChatCompletionRequest.Prompt.builder()
                            .role(Role.user.getCode())
                            .content(entity.getContent())
                            .build())
                    .collect(Collectors.toList());
            // 封装参数
            ChatCompletionRequest request = ChatCompletionRequest.builder().isCompatible(true)
                    .model(Model.valueOf(ChatGLMModel.get(chatProcess.getModel()).name()))
                    .prompt(messages).build();
            // 调用服务
            this.chatGlMOpenAiSession.completions(request, new EventSourceListener() {
                @Override
                public void onEvent(@NotNull EventSource eventSource, @Nullable String id, @Nullable String type, @NotNull String data) {
                    ChatCompletionResponse response = JSON.parseObject(data, ChatCompletionResponse.class);
                    
                    // 发送信息
                    if (EventType.add.getCode().equals(type)) {
                        try {
                            emitter.send(response.getData());
                        } catch (Exception e) {
                            throw new ChatGPTException(e.getMessage());
                        }
                    }
                    
                    // type 消息类型, add: 增量, finish: 结束, error: 错误, interrupted: 中断
                    if (EventType.finish.getCode().equals(type)) {
                        ChatCompletionResponse.Meta meta = JSON.parseObject(response.getMeta(), ChatCompletionResponse.Meta.class);
                        log.info("[输出结束] Tokens {}", JSON.toJSONString(meta));
                    }
                }
                
                @Override
                public void onClosed(@NotNull EventSource eventSource) {
                    emitter.complete();
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
        
        // 2. 封装参数
//        ChatCompletionRequest request = new ChatCompletionRequest();
//        request.setModel(Model.valueOf(ChatGLMModel.get(chatProcess.getModel()).name()));
//        request.setPrompt(prompts);
        
        
        // 3. 请求应答
//        chatGlMOpenAiSession.completions(request, new EventSourceListener() {
//            @Override
//            public void onEvent(@NotNull EventSource eventSource, @Nullable String id, @Nullable String type, @NotNull String data) {
//                ChatCompletionResponse response = JSON.parseObject(data, ChatCompletionResponse.class);
//
//                // 发送信息
//                if (EventType.add.getCode().equals(type)) {
//                    try {
//                        emitter.send(response.getData());
//                    } catch (Exception e) {
//                        throw new ChatGPTException(e.getMessage());
//                    }
//                }
//
//                // type 消息类型, add: 增量, finish: 结束, error: 错误, interrupted: 中断
//                if (EventType.finish.getCode().equals(type)) {
//                    ChatCompletionResponse.Meta meta = JSON.parseObject(response.getMeta(), ChatCompletionResponse.Meta.class);
//                    log.info("[输出结束] Tokens {}", JSON.toJSONString(meta));
//                }
//            }
//
//            @Override
//            public void onClosed(@NotNull EventSource eventSource) {
//                emitter.complete();
//            }
//        });
    }
}
