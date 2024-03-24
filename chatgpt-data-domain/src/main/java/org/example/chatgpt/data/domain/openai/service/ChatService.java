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
import org.example.chatgpt.data.domain.openai.model.vo.LogicCheckTypeVO;
import org.example.chatgpt.data.domain.openai.service.rule.ILogicFilter;
import org.example.chatgpt.data.domain.openai.service.rule.factory.DefaultLogicFactory;
import org.example.chatgpt.data.types.enums.ChatGLMModel;
import org.example.chatgpt.data.types.exception.ChatGPTException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import javax.annotation.Resource;
import java.io.IOException;
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
    protected RuleLogicEntity<ChatProcessAggregate> doCheckLogic(ChatProcessAggregate chatProcess, String... logics) {
        Map<String, ILogicFilter> logicFilterMap = logicFactory.openLogicFilter();
        RuleLogicEntity<ChatProcessAggregate> entity = null;
        // 通过不同的规则校验
        for (String logic : logics) {
            entity = logicFilterMap.get(logic).filter(chatProcess);
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
                            .role(Role.valueOf(entity.getRole()).name())
                            .content(entity.getContent())
                            .build())
                    .collect(Collectors.toList());
            // 封装参数
            // stream=true时返回的是 delta， 否则是 message
            ChatCompletionRequest request = ChatCompletionRequest.builder()
                    .isCompatible(true)
                    .model(Model.valueOf(ChatGLMModel.get(chatProcess.getModel()).name()))
                    .prompt(messages).build();
            
            try {
                this.openAiSession.completions(request, new EventSourceListener() {
                    @Override
                    public void onEvent(@NotNull EventSource eventSource, @Nullable String id, @Nullable String type, @NotNull String data) {
                        ChatCompletionResponse response = JSON.parseObject(data, ChatCompletionResponse.class);
                        List<ChatCompletionResponse.Choice> choices = response.getChoices();
                        for (ChatCompletionResponse.Choice choice : choices) {
                            ChatCompletionResponse.Delta delta = choice.getDelta();
                            // 判断是不是 assistant
                            if (!Role.assistant.getCode().equals(delta.getRole())) {
                                continue;
                            }
                            // 判断是否结束
                            String finishReason = choice.getFinishReason();
                            if (StringUtils.isNoneBlank(finishReason) && "stop".equals(finishReason)) {
                                emitter.complete();
                                break;
                            }
                            
                            // 发送消息
                            try {
                                emitter.send(delta.getContent());
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
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
