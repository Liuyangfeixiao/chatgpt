package org.example.chatgpt.data.config;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class GuavaCacheConfig {
    @Bean
    public Cache<String, String> cache() {
        return CacheBuilder.newBuilder()
                .expireAfterAccess(3, TimeUnit.MINUTES)
                .build();
    }
}
