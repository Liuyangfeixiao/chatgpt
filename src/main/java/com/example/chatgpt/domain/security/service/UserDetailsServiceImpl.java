package com.example.chatgpt.domain.security.service;

import com.example.chatgpt.domain.security.model.vo.LoginUser;
import com.example.chatgpt.domain.security.model.vo.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 根据用户名查询用户信息
//        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
//        queryWrapper.eq("username", username);
//        User user = userMapper.selectOne(queryWrapper);
//        if (user == null) {
//            throw new RuntimeException("User don't exist");
//        }
        // 查不到数据则抛出异常
        // TODO 更改查找username
        User user = new User();
        user.setUsername(username);
        user.setId(1);
        user.setPassword(new BCryptPasswordEncoder().encode("123"));
        if(Objects.isNull(username)){
            throw new RuntimeException("用户名或密码错误");
        }
        // 封装为UserDetals对象返回
        return new LoginUser(user);
    }
}
