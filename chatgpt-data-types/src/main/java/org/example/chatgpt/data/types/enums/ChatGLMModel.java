package org.example.chatgpt.data.types.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ChatGLMModel {
    CHATGLM_4("glm-4"),
    CHATGLM_4V("glm-4v"),
    CHATGLM_3_TURBO("glm-3-turbo"),
    COGVIEW_3("cogview-3"),
    ;
    private final String code;
    public static ChatGLMModel get(String code) {
        switch (code){
            case "glm-4":
                return ChatGLMModel.CHATGLM_4;
            case "glm-4v":
                return ChatGLMModel.CHATGLM_4V;
            case "glm-3-turbo":
                return ChatGLMModel.CHATGLM_3_TURBO;
            case "cogview-3":
                return ChatGLMModel.COGVIEW_3;
            default:
                return ChatGLMModel.CHATGLM_3_TURBO;
        }
    }
}

