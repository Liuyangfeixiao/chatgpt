package org.example.chatgpt.data.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "alipay", ignoreInvalidFields = true)
public class AliPayConfigProperties {
    private Boolean enabled;
    private String app_id;
    private String merchant_private_key;
    private String format;
    private String alipay_public_key;
    private String notify_url;
    private String return_url;
    private String gateway_url;
    private String sign_type;
    private String charset;
}
