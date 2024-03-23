package org.example.chatgpt.data.domain.auth.model.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public enum AuthTypeVo {
    SUCCESS("0000","验证成功"),
    NOT_EXIST("0001","验证码不存在"),
    NOT_VALID("0002","验证码失效");
    
    private String code;
    private String info;
}
