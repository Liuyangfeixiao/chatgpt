package org.example.chatgpt.data.config;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AliPayConfigProperties.class)
public class AliPayConfig {
    @Bean
    @ConditionalOnProperty(value = "alipay.enabled", havingValue = "true", matchIfMissing = false)
    public AlipayClient alipayClient(AliPayConfigProperties properties) {
        return new DefaultAlipayClient(properties.getGateway_url(),
                properties.getApp_id(),
                properties.getMerchant_private_key(),
                properties.getFormat(),
                properties.getCharset(),
                properties.getAlipay_public_key(),
                properties.getSign_type());
    }
}
