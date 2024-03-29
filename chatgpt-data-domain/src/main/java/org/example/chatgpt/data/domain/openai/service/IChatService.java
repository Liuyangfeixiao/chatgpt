package org.example.chatgpt.data.domain.openai.service;

import org.example.chatgpt.data.domain.openai.model.aggregates.ChatProcessAggregate;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface IChatService {
    ResponseBodyEmitter completions(ChatProcessAggregate chatProcess, ResponseBodyEmitter emitter);
}
