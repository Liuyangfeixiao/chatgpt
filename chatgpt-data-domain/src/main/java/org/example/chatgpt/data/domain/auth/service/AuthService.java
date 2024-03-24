package org.example.chatgpt.data.domain.auth.service;

import com.google.common.cache.Cache;
import io.jsonwebtoken.Claims;
import org.apache.commons.lang3.StringUtils;
import org.example.chatgpt.data.domain.auth.model.entity.AuthStateEntity;
import org.example.chatgpt.data.domain.auth.model.vo.AuthTypeVo;
import org.example.chatgpt.data.domain.auth.utils.JwtUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class AuthService extends AbstractAuthService{
    @Resource
    private Cache<String, String> codeCache;
    @Override
    public AuthStateEntity checkIfExit(String code) {
        String openId = codeCache.getIfPresent(code);
        if (StringUtils.isBlank(openId)) {
            return AuthStateEntity.builder()
                    .code(AuthTypeVo.NOT_EXIST.getCode())
                    .info(AuthTypeVo.NOT_EXIST.getInfo())
                    .build();
        }
        // 清除缓存
        codeCache.invalidate(openId);
        codeCache.invalidate(code);
        return AuthStateEntity.builder()
                .code(AuthTypeVo.SUCCESS.getCode())
                .info(AuthTypeVo.SUCCESS.getInfo())
                .openid(openId)
                .build();
    }
    
    @Override
    public String getOpenid(String token) {
        Claims claims = JwtUtil.decode(token);
        return claims.get("openId").toString();
    }
    
    @Override
    public boolean checkToken(String token) {
        return JwtUtil.isVerify(token);
    }
}
