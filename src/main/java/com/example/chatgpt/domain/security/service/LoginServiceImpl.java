package com.example.chatgpt.domain.security.service;

import com.example.chatgpt.application.LoginService;
import com.example.chatgpt.domain.security.model.vo.LoginUser;
import com.example.chatgpt.domain.security.model.vo.User;
import com.example.chatgpt.domain.security.service.redis.RedisCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class LoginServiceImpl implements LoginService {
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private RedisCache redisCache;
    @Override
    public Map<String, String> getToken(String username, String password) {
        // 将明文转化为密文
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(username, password);
        // 登录失败会自动报异常
        Authentication authenticate = authenticationManager.authenticate(authenticationToken);
        // 获取用户信息
        LoginUser loginUser = (LoginUser) authenticate.getPrincipal();
        User user = loginUser.getUser();
        // 生成jwt
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", user.getUsername());
        claims.put("password", user.getPassword());
        String jwt = JwtUtil.createJWT(user.getUsername(), claims);
        // authenticate 存入 redis
        redisCache.setCacheObject("login:"+user.getUsername(), user);
        // token 返回
        Map<String, String> result = new HashMap<>();
        result.put("token", jwt);
        result.put("msg", "success");
        return result;
    }
}
