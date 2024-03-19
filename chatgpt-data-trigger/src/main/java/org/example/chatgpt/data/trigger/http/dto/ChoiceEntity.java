package org.example.chatgpt.data.trigger.http.dto;

import lombok.Data;

@Data
public class ChoiceEntity {
    private MessageEntity delta;
    private MessageEntity message;
}
