package com.example.chatgpt.application;

import java.util.Map;

public interface LoginService {
    Map<String, String> getToken(String username, String password);
}
