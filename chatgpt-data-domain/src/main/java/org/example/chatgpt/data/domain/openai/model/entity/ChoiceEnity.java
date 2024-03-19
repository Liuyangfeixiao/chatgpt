package org.example.chatgpt.data.domain.openai.model.entity;

import lombok.Data;

@Data
public class ChoiceEnity {
    /** stream = true 请求参数里返回的属性的属性是 delta */
    private MessageEntity delta;
    /** stream =false 请求参数中返回的属性是 message*/
    private MessageEntity message;
}
