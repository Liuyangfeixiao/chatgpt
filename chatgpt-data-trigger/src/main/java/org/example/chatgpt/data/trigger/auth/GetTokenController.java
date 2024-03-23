package org.example.chatgpt.data.trigger.auth;

import org.example.chatgpt.data.domain.auth.utils.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class GetTokenController {
    @RequestMapping("/authorize")
    public ResponseEntity<Map<String, String>> authorize(String username, String password) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", username);
        claims.put("password", password);
        String jwt = JwtUtil.encode(username, 60*60*24*7, claims);
        Map<String, String> result = new HashMap<>();
        result.put("token", jwt);
        result.put("msg", "success");
        return ResponseEntity.ok(result);
    }
    @RequestMapping("/verify")
    public ResponseEntity<String> verify(String token) {
        boolean isValid = JwtUtil.isVerify(token);
        if (isValid) {
            return ResponseEntity.status(HttpStatus.OK).body("verify success!");
        } else
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("verify fail!");
    }
}
