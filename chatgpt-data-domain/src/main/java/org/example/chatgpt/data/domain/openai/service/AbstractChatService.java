package org.example.chatgpt.data.domain.openai.service;

import cn.bugstack.chatglm.session.OpenAiSession;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.example.chatgpt.data.domain.openai.model.aggregates.ChatProcessAggregate;
import org.example.chatgpt.data.types.common.Constants;
import org.example.chatgpt.data.types.exception.ChatGPTException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import javax.annotation.Resource;

@Slf4j
public abstract class AbstractChatService implements IChatService{
    @Resource
    protected OpenAiSession openAiSession;
    @Override
    public ResponseBodyEmitter completions(ChatProcessAggregate chatProcess) {
        // 1. 校验权限
        if (!"b8b4".equals(chatProcess.getToken())) {
            throw new ChatGPTException(Constants.ResponseCode.TOKEN_ERROR.getCode(), Constants.ResponseCode.TOKEN_ERROR.getInfo());
        }

        // 2. 请求应答
        ResponseBodyEmitter emitter = new ResponseBodyEmitter(3*60*1000L); // 设置3min
        emitter.onCompletion(() -> {
            log.info("流式问答请求完成，使用模型{}", chatProcess.getModel());
        });
        emitter.onError(throwable -> log.info("流式问答请求异常，使用模型{}", chatProcess.getModel(), throwable));

        // 3. 应答处理
        try {
            this.doMessageResponse(chatProcess, emitter);
        } catch (Exception e) {
            throw new ChatGPTException(Constants.ResponseCode.UN_ERROR.getCode(), Constants.ResponseCode.UN_ERROR.getInfo());
        }

        // 4. 返回结果
        return emitter;
    }
    protected abstract void doMessageResponse(ChatProcessAggregate chatProcess, ResponseBodyEmitter emitter) throws JsonProcessingException;
}
