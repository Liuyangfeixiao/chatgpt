package com.example.chatgpt.domain.security.service;

import com.example.chatgpt.domain.security.model.vo.LoginUser;
import com.example.chatgpt.domain.security.model.vo.User;
import com.example.chatgpt.domain.security.service.redis.RedisCache;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;

@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {
    @Autowired
    private RedisCache redisCache;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 获取请求Header中的token
        String token = request.getHeader("Authorization");  // token 一般跟在这个key后面
        // 从请求参数中获得token
        String[] values = request.getParameterValues("token");
        if (!Objects.isNull(values)) {
            token = values[0];
        }
        if (!StringUtils.hasText(token)) {
            // 放行
            filterChain.doFilter(request, response);
            return;
        }
        // 解析token
        // TODO 之后可以更改为userid作为唯一标识
        String username;
        try {
            Claims claims = JwtUtil.parseJWT(token);
            username = (String) claims.get("username");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // 从 redis 中获取用户信息
        String redisKey = "login:" + username;
        User user = redisCache.getCacheObject(redisKey);
        if (user == null) {
            throw new RuntimeException("用户未登录");
        }
        LoginUser loginUser = new LoginUser(user);
        // 存入SecurityContextHolder
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginUser, null, null);
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        // 放行
        filterChain.doFilter(request, response);
    }
}
