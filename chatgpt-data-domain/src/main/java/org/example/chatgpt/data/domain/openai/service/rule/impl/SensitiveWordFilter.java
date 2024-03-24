package org.example.chatgpt.data.domain.openai.service.rule.impl;

import com.github.houbb.sensitive.word.bs.SensitiveWordBs;
import lombok.extern.slf4j.Slf4j;
import org.example.chatgpt.data.domain.openai.annotation.LogicStrategy;
import org.example.chatgpt.data.domain.openai.model.aggregates.ChatProcessAggregate;
import org.example.chatgpt.data.domain.openai.model.entity.MessageEntity;
import org.example.chatgpt.data.domain.openai.model.entity.RuleLogicEntity;
import org.example.chatgpt.data.domain.openai.model.vo.LogicCheckTypeVO;
import org.example.chatgpt.data.domain.openai.service.rule.ILogicFilter;
import org.example.chatgpt.data.domain.openai.service.rule.factory.DefaultLogicFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@LogicStrategy(logicModel = DefaultLogicFactory.LogicModel.SENSITIVE_WORD)
public class SensitiveWordFilter implements ILogicFilter {
    @Value("${app.config.white-list}")
    private String whiteListStr;
    
    @Resource
    private SensitiveWordBs sensitiveWordBs;
    @Override
    public RuleLogicEntity<ChatProcessAggregate> filter(ChatProcessAggregate chatProcess) {
        // 白名单不处理敏感词
        if (chatProcess.isWhiteList(whiteListStr)) {
            return RuleLogicEntity.<ChatProcessAggregate>builder()
                    .type(LogicCheckTypeVO.SUCCESS)
                    .data(chatProcess).build();
        }
        // 有敏感词，重新构建一个ChatProcessAggregate
        ChatProcessAggregate newChatProcess = ChatProcessAggregate.builder()
                .openid(chatProcess.getOpenid())
                .model(chatProcess.getModel())
                .token(chatProcess.getToken())
                .build();
        List<MessageEntity> newMessages = chatProcess.getMessages().stream()
                .map(message -> {
                    String content = message.getContent();
                    String replace = sensitiveWordBs.replace(content);
                    return MessageEntity.builder()
                            .role(message.getRole())
                            .content(replace)
                            .build();
                }).collect(Collectors.toList());
        newChatProcess.setMessages(newMessages);
        return RuleLogicEntity.<ChatProcessAggregate>builder()
                .type(LogicCheckTypeVO.SUCCESS)
                .data(newChatProcess).build();
    }
}
