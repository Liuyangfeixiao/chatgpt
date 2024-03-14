package com.example.chatgpt.interfaces;


import cn.bugstack.chatglm.model.ChatCompletionRequest;
import cn.bugstack.chatglm.model.Model;
import cn.bugstack.chatglm.model.Role;
import cn.bugstack.chatglm.session.Configuration;
import cn.bugstack.chatglm.session.OpenAiSession;
import cn.bugstack.chatglm.session.OpenAiSessionFactory;
import cn.bugstack.chatglm.session.defaults.DefaultOpenAiSessionFactory;
import com.example.chatgpt.application.IWeChatValidateService;
import com.example.chatgpt.domain.recieve.model.MessageTextEntity;
import com.example.chatgpt.infrastracture.util.wechat.XmlUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/wechat/portal")
public class WeChatPortalController {
    @Value("${wx.config.originalid}")
    private String originalId;
    @Resource
    private IWeChatValidateService weChatValidateService;
    private final OpenAiSession openAiSession;
    @Resource
    private ThreadPoolTaskExecutor taskExecutor;
    // 存放OpenAI结果返回数据
    private final Map<String, String> openAiDataMap = new ConcurrentHashMap<>();
    // 存放OpenAI次数调用数据
    private final Map<String, Integer> openAiRetryCountMap = new ConcurrentHashMap<>();
    WeChatPortalController(@Value("${openai.api-host}") String apiHost,
                           @Value("${openai.api-key}") String apiKey) {
        // 配置文件
        Configuration configuration = new Configuration();
        configuration.setApiHost(apiHost);
        configuration.setApiSecretKey(apiKey);
        // 2. 会话工厂
        OpenAiSessionFactory factory = new DefaultOpenAiSessionFactory(configuration);
        // 3. 开启会话
        this.openAiSession = factory.openSession();
        log.info("开始OpenAiSession");
    }
    @GetMapping(produces = "text/plain;charset=utf-8")
    public String validate(@RequestParam(required = false) String signature,
                           @RequestParam(required = false) String timestamp,
                           @RequestParam(required = false) String nonce,
                           @RequestParam(required = false) String echostr) {
        try {
            log.info("微信公众号验签开始: [signature: {}, timestamp: {}, nonce: {}, echostr: {}]",
                    signature, timestamp, nonce, echostr);
            if (StringUtils.isAnyBlank(signature, timestamp, nonce, timestamp)) {
                throw new IllegalArgumentException("请求参数非法，请核实");
            }
            boolean check = weChatValidateService.checkSign(signature, timestamp, nonce);
            return check ? echostr : null;
        } catch (Exception e) {
            log.error("微信公众号验签失败");
            return null;
        }
    }
    @PostMapping(produces = "application/xml; charset=UTF-8")
    public String replyMessage(@RequestBody String requestBody,
                               @RequestParam String signature,
                               @RequestParam String timestamp,
                               @RequestParam String nonce,
                               @RequestParam String openid,
                               @RequestParam(value = "encrypt_type", required = false) String encType,
                               @RequestParam(value = "msg_signature", required = false) String msgSignature) {
        try {
            log.info("公众号开始接收{}消息请求 {}", openid, requestBody);
            // 验证消息签名, 确认请求来源
            boolean validate = weChatValidateService.checkSign(signature, timestamp, nonce);
            if (!validate) {
                throw new IllegalArgumentException("验签失败，确认参数是否正确");
            }
            // 提取用户消息
            MessageTextEntity message = XmlUtil.xmlToBean(requestBody, MessageTextEntity.class);
            // 创建反馈信息对象，设置公共属性
            MessageTextEntity res = new MessageTextEntity();
            res.setToUserName(openid);
            res.setFromUserName(originalId);
            res.setMsgType("text");
            // 异步任务【加入超时重试，对于小体量的调用反馈，可以在重试有效次数内返回结果】
            // 微信服务器在五秒内收不到响应会断掉连接，并且重新发起请求，总共重试三次
            if (openAiDataMap.get(message.getContent().trim()) == null || "NULL".equals(openAiDataMap.get(message.getContent().trim()))) {
                String data ="消息处理中，请再回复我一句【" + message.getContent().trim() + "】";
                // 休眠等待
                Integer retryCount = openAiRetryCountMap.get(message.getContent().trim());
                if (null == retryCount) {
                    if (openAiDataMap.get(message.getContent().trim()) == null) {
                        doChatGPTTask(message.getContent().trim());
                    }
                    log.info("超时重试: {}", 1);
                    openAiRetryCountMap.put(message.getContent().trim(), 1);
                    // 等待5s，微信重新发起请求
                    TimeUnit.SECONDS.sleep(5);
                    new CountDownLatch(1).await();
                } else if (retryCount < 2) {
                    retryCount = retryCount +1;
                    log.info("超时重试: {}", retryCount);
                    openAiRetryCountMap.put(message.getContent().trim(), retryCount);
                    TimeUnit.SECONDS.sleep(5);
                    new CountDownLatch(1).await();
                } else {
                    // 请求重传的次数等于3次时，如果超时还未得到结果，回复让其重新发送
                    retryCount = retryCount+1;
                    log.info("超时重试: {}", retryCount);
                    openAiRetryCountMap.put(message.getContent().trim(), retryCount);
                    TimeUnit.SECONDS.sleep(3);
                    // 如果此时得到回复消息
                    if (openAiDataMap.get(message.getContent().trim()) != null &&
                            !"NULL".equals(openAiDataMap.get(message.getContent().trim()))) {
                        data = openAiDataMap.get(message.getContent().trim());
                        // TODO 此时是否需要消除调用次数和返回内容
                    }
                }
                res.setContent(data);
                res.setCreateTime(String.valueOf(System.currentTimeMillis() / 1000L));
                return XmlUtil.beanToXml(res);
            }

            res.setContent(openAiDataMap.get(message.getContent().trim()));
            res.setCreateTime(String.valueOf(System.currentTimeMillis() / 1000L));
            String result = XmlUtil.beanToXml(res);
            log.info("微信公众号消息{}请求完成: {}", openid, result);
            // 移除这条消息保存的内容
            openAiDataMap.remove(message.getContent().trim());
            return result;
        } catch (Exception e) {
            log.error("接收微信公众号消息{}请求{}失败", openid, requestBody, e);
            return ""; // 返回空字符串，微信服务器不对此进行处理，也不重新发送请求
        }
    }
    public void doChatGPTTask(String content) {
        openAiDataMap.put(content, "NULL");
        taskExecutor.execute(() -> {
            // 入参：模型型号，请求信息，需要更新最新版ChatGLM-SDK-Java
            ChatCompletionRequest request = new ChatCompletionRequest();
            // chatGLM_6b_SSE、chatglm_lite、chatglm_lite_32k、chatglm_std、chatglm_pro
            request.setModel(Model.GLM_3_5_TURBO);
            request.setPrompt(new ArrayList<ChatCompletionRequest.Prompt>() {
                private static final long serialVersionUID = -7988151926241837899L;
                {
                    add(ChatCompletionRequest.Prompt.builder()
                            .role(Role.user.getCode())
                            .content(content)
                            .build());
                }
            });
            // 同步获取结果
            try {
                CompletableFuture<String> future = openAiSession.completions(request);
                openAiDataMap.put(content, future.get());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}
