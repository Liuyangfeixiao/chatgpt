package org.example.chatgpt.data.domain.openai.service;

import cn.bugstack.chatglm.model.*;
import cn.bugstack.chatglm.session.OpenAiSession;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import org.apache.commons.lang3.StringUtils;
import org.example.chatgpt.data.domain.openai.model.aggregates.ChatProcessAggregate;
import org.example.chatgpt.data.domain.openai.model.entity.RuleLogicEntity;
import org.example.chatgpt.data.domain.openai.model.entity.UserAccountQuotaEntity;
import org.example.chatgpt.data.domain.openai.model.vo.LogicCheckTypeVO;
import org.example.chatgpt.data.domain.openai.service.rule.ILogicFilter;
import org.example.chatgpt.data.domain.openai.service.rule.factory.DefaultLogicFactory;
import org.example.chatgpt.data.types.enums.ChatGLMModel;
import org.example.chatgpt.data.types.exception.ChatGPTException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.Resource;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ChatService extends AbstractChatService{
    @Resource
    private DefaultLogicFactory logicFactory;
    
    @Override
    protected RuleLogicEntity<ChatProcessAggregate> doCheckLogic(ChatProcessAggregate chatProcess, UserAccountQuotaEntity data, String... logics) {
        Map<String, ILogicFilter<UserAccountQuotaEntity>> logicFilterMap = logicFactory.openLogicFilter();
        RuleLogicEntity<ChatProcessAggregate> entity = null;
        // 通过不同的规则校验
        for (String logic : logics) {
            entity = logicFilterMap.get(logic).filter(chatProcess, data);
            if (!LogicCheckTypeVO.SUCCESS.equals(entity.getType())) {
                return entity;
            }
        }
        return entity != null ? entity : RuleLogicEntity.<ChatProcessAggregate>builder()
                .type(LogicCheckTypeVO.SUCCESS).data(chatProcess).build();
    }
    
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
            ChatCompletionRequest request = new ChatCompletionRequest();
            request.setModel(Model.valueOf(ChatGLMModel.get(chatProcess.getModel()).name()));
            request.setIncremental(true);
            request.setIsCompatible(true);
            request.setPrompt(messages);
            // ChatCompletionRequest.builder()
            //                    .isCompatible(true)
            //                    .incremental(false)
            //                    .model(Model.valueOf(ChatGLMModel.get(chatProcess.getModel()).name()))
            //                    .prompt(messages).build();
            // Model.valueOf(ChatGLMModel.get(chatProcess.getModel()).name())
            try {
                this.openAiSession.completions(request, new EventSourceListener() {
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
                        // type 消息类型，add 增量，finish 结束，error 错误，interrupted 中断
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
                log.info("流式应答出现问题:{}", e.getMessage());
                throw new RuntimeException(e);
            }
            
        } catch (Exception e) {
            log.info("流式问答请求出现异常,异常信息:{}",e.getMessage());
            throw new RuntimeException(e);
        }
        
    }
}
