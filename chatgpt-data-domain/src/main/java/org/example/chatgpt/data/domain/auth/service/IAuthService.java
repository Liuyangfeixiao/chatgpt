package org.example.chatgpt.data.domain.auth.service;

import org.example.chatgpt.data.domain.auth.model.entity.AuthStateEntity;

public interface IAuthService {
    /**
     * @description 登录验证，生成 token 返回
     * @param code
     */
    public AuthStateEntity doLogin(String code);
    
    /**
     * @description 检查 token 是否有效
     * @param token
     * @return
     */
    public boolean checkToken(String token);
    
    /**
     * @description 获取token中的openID
     * @param token jwt token
     * @return
     */
    String getOpenid(String token);
}
