package org.example.chatgpt.data.trigger.http;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.example.chatgpt.data.domain.openai.model.aggregates.ChatProcessAggregate;
import org.example.chatgpt.data.domain.openai.model.entity.MessageEntity;
import org.example.chatgpt.data.domain.openai.service.IChatService;
import org.example.chatgpt.data.trigger.http.dto.ChatGPTRequestDTO;
import org.example.chatgpt.data.types.exception.ChatGPTException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.stream.Collectors;

@Slf4j
@RestController
@CrossOrigin("*")
@RequestMapping("/api/${app.config.api-version}")
public class ChatGPTAIServiceController {
    @Resource
    private IChatService chatService;
    @PostMapping(value = "chat/completions")
    public ResponseBodyEmitter completionStream(@RequestBody ChatGPTRequestDTO request,
                                                @RequestHeader("Authorization") String token,
                                                HttpServletResponse response) {
        log.info("流式问答请求开始，使用模型: {} 请求信息: {}", request.getModel(), JSON.toJSONString(request.getMessages()));
        try {
            // 1. 基础配置: 流式输出，编码，禁用缓存
            response.setContentType("text/event-stream");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Cache-Control", "no-cache");
            
            // 2. 构建参数
            ChatProcessAggregate chatProcessAggregate = ChatProcessAggregate.builder()
                    .model(request.getModel())
                    .token(token)
                    .messages(request.getMessages().stream()
                       .map(entity -> MessageEntity.builder()
                               .role(entity.getRole())
                               .content(entity.getContent())
                               .build())
                            .collect(Collectors.toList()))
                    .build();
            return chatService.completions(chatProcessAggregate);
        } catch (Exception e) {
            log.error("流式问答请求异常, 请求模型: {}, 异常信息", request.getModel(), e);
            throw new ChatGPTException(e.getMessage());
        }
    }
}
