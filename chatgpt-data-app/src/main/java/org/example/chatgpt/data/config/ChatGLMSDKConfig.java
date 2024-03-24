package org.example.chatgpt.data.config;

import cn.bugstack.chatglm.session.OpenAiSession;
import cn.bugstack.chatglm.session.OpenAiSessionFactory;
import cn.bugstack.chatglm.session.defaults.DefaultOpenAiSessionFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ChatGLMSDKConfigProperties.class)
public class ChatGLMSDKConfig {
    @Bean
    public OpenAiSession openAiSession(ChatGLMSDKConfigProperties properties) {
        // 1. 配置文件
        cn.bugstack.chatglm.session.Configuration configuration = new cn.bugstack.chatglm.session.Configuration();
        configuration.setApiHost(properties.getApiHost());
        configuration.setApiSecretKey("2f35c38c217c30ad13e87d83de59aa22.UUjqNdr0pJFpDtJn");
        // 2. 会话工厂
        OpenAiSessionFactory factory = new DefaultOpenAiSessionFactory(configuration);
        // 3. 开启会话
        return factory.openSession();
    }
}
