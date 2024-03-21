package org.example.chatgpt.data.types.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ChatGLMModel {
    GLM_4("glm-4"),
    GLM_4V("glm-4v"),
    GLM_3_5_TURBO("glm-3-turbo"),
    COGVIEW_3("cogview-3"),
    ;
    private final String code;
    public static ChatGLMModel get(String code) {
        switch (code){
            case "glm-4":
                return ChatGLMModel.GLM_4;
            case "glm-4v":
                return ChatGLMModel.GLM_4V;
            case "glm-3-turbo":
                return ChatGLMModel.GLM_3_5_TURBO;
            case "cogview-3":
                return ChatGLMModel.COGVIEW_3;
            default:
                return ChatGLMModel.GLM_3_5_TURBO;
        }
    }
}

