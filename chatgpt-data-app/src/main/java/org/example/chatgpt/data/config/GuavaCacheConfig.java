package org.example.chatgpt.data.config;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class GuavaCacheConfig {
    @Bean
    public Cache<String, String> codeCache() {
        return CacheBuilder.newBuilder()
                .expireAfterWrite(3, TimeUnit.MINUTES)
                .build();
    }
    
    /**
     * 访问次数的缓存，12小时后过期
     * @return
     */
    @Bean
    public Cache<String, Integer> visitCache() {
        return CacheBuilder.newBuilder()
                .expireAfterWrite(12, TimeUnit.HOURS)
                .build();
    }
    
    /**
     * 访问频率的缓存，一分钟过期
     * @return
     */
    public Cache<String, Integer> frequencyCache() {
        return CacheBuilder.newBuilder()
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .build();
    }
}
