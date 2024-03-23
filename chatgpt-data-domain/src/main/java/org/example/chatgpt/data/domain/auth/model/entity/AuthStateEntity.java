package org.example.chatgpt.data.domain.auth.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthStateEntity {
    /**
     * JWT 生成的token
     */
    private String token;
    /**
     * 错误码
     */
    private String code;
    /**
     * 错误信息
     */
    private String info;
    /**
     * 微信用户个人凭证
     */
    private String openid;
}
