package org.example.chatgpt.data.domain.auth.service;

import lombok.extern.slf4j.Slf4j;
import org.example.chatgpt.data.domain.auth.model.entity.AuthStateEntity;
import org.example.chatgpt.data.domain.auth.model.vo.AuthTypeVo;
import org.example.chatgpt.data.domain.auth.utils.JwtUtil;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public abstract class AbstractAuthService implements IAuthService {
    @Override
    public AuthStateEntity doLogin(String code) {
        // 首先判断验证码长度是否为4位数, 否则返回NOT_VALID状态
        if (!code.matches("^\\d{4}")) {
            return AuthStateEntity.builder()
                    .code(AuthTypeVo.NOT_VALID.getCode())
                    .info(AuthTypeVo.NOT_VALID.getInfo())
                    .build();
        }
        // 验证码是否在缓存之中
        AuthStateEntity authStateEntity = this.checkIfExit(code);
        if (!AuthTypeVo.SUCCESS.getCode().equals(authStateEntity.getCode())) {
            return authStateEntity;
        }
        
        // 生成jwt
        String openid = authStateEntity.getOpenid();
        Map<String, Object> claims = new HashMap<>();
        claims.put("openId", openid);
        String token = JwtUtil.encode(openid, 7*2460*60*1000L, claims);
        authStateEntity.setToken(token);
        return authStateEntity;
    }
    
    /**
     * @description 检验验证码是否存在
     * @param code
     * @return
     */
    public abstract AuthStateEntity checkIfExit(String code);
}
