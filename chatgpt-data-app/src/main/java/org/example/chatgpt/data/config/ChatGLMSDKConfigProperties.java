package org.example.chatgpt.data.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @description 将应用程序的配置属性绑定到java对象中方便进行管理
 */
@Data
@ConfigurationProperties(prefix = "chatglm.sdk.config", ignoreInvalidFields = true)
public class ChatGLMSDKConfigProperties {
    /** 状态；open = 开启、close 关闭 */
    private boolean enable;
    /**转发地址**/
    private String apiHost;
    private String apiSecretKey;
}
