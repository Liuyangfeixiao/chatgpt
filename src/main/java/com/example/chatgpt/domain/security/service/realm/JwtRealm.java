package com.example.chatgpt.domain.security.service.realm;

import com.example.chatgpt.domain.security.model.vo.JwtToken;
import com.example.chatgpt.domain.security.service.JwtUtil;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @description 自定义Realm
 */
public class JwtRealm extends AuthorizingRealm {
    private Logger logger = LoggerFactory.getLogger(JwtRealm.class);

    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof JwtToken;
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        // 暂时不需要实现
        return null;
    }
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        String jwt = (String) token.getPrincipal();
        if (jwt == null) {
            throw new NullPointerException("jwt token 不允许为空");
        }
        // 判断
        if (!JwtUtil.isVerify(jwt)) {
            throw new UnknownAccountException();
        }
        // 可以获取username信息，并做一些处理
        String username;
        try {
            username = (String) JwtUtil.parseJWT(jwt).get("username");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        logger.info("鉴权用户: {}", username);
        return new SimpleAuthenticationInfo(jwt, jwt, "JwtRealm");
    }
}
