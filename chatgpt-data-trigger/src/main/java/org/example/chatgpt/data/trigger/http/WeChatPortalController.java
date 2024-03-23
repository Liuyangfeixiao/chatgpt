package org.example.chatgpt.data.trigger.http;

import jdk.jfr.events.ExceptionStatisticsEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.example.chatgpt.data.domain.weixin.model.entity.MessageTextEntity;
import org.example.chatgpt.data.domain.weixin.model.entity.UserBehaviorMessageEntity;
import org.example.chatgpt.data.domain.weixin.service.IWeChatBehaviorService;
import org.example.chatgpt.data.domain.weixin.service.IWeChatValidateService;
import org.example.chatgpt.data.types.sdk.weixin.XmlUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Date;

@RestController
@Slf4j
@RequestMapping("/api/${app.config.api-version}/wx/portal")
public class WeChatPortalController {
    @Value("${wx.config.appid}")
    private String appid;
    @Value("${wx.config.originalid}")
    private String originalId;
    
    @Resource
    private IWeChatBehaviorService weChatBehaviorService;
    @Resource
    private IWeChatValidateService weChatValidateService;
    
    /**
     * 处理微信服务器发来的get请求，进行签名的验证
     * @param signature 签名
     * @param timestamp 时间戳
     * @param nonce 随机字符串
     * @param echostr 验证字符串
     * @return
     */
    @GetMapping(produces = "text/plain;charset=utf-8")
    public String validate(@RequestParam(value = "signature", required = false) String signature,
                           @RequestParam(value = "timestamp", required = false) String timestamp,
                           @RequestParam(value = "nonce", required = false) String nonce,
                           @RequestParam(value = "echostr", required = false) String echostr) {
        try {
            log.info("微信公众号验签信息{}开始, [{}, {}, {}, {}]", appid, signature, timestamp, nonce, echostr);
            if (StringUtils.isAnyBlank(signature, timestamp, nonce, echostr)) {
                throw new IllegalArgumentException("请求参数非法, 请核实");
            }
            boolean check = weChatValidateService.checkSign(signature, timestamp, nonce);
            log.info("微信公众号验签信息{}完成, check:{}", appid, check);
            if (!check) {
                return null;
            }
            return echostr;
        } catch (Exception e) {
            log.error("微信公众号验签信息{}失败 [{}, {}, {}, {}]", appid, signature, timestamp, nonce, echostr);
            return null;
        }
    }
    @PostMapping(produces = "application/xml; charset=UTF-8")
    public String post(@RequestBody String requestBody,
                       @RequestParam("signature") String signature,
                       @RequestParam("timestamp") String timestamp,
                       @RequestParam("nonce") String nonce,
                       @RequestParam("openid") String openid,
                       @RequestParam(name = "encrypt_type", required = false) String encType,
                       @RequestParam(name = "msg_signature", required = false) String msgSignature) {
        try {
            log.info("接收微信公众号信息请求{}开始 {}", openid, requestBody);
            // 消息转换
            MessageTextEntity message = XmlUtil.xmlToBean(requestBody, MessageTextEntity.class);
            
            // 构建UserBehaviorMessage实体，用于weChatBehaviorService生成验证码
            UserBehaviorMessageEntity entity = UserBehaviorMessageEntity.builder()
                    .fromUserName(message.getFromUserName())
                    .event(message.getEvent())
                    .openId(openid)
                    .createTime(new Date(Long.parseLong(message.getCreateTime()) * 1000L))
                    .msgType(message.getMsgType())
                    .content(StringUtils.isBlank(message.getContent()) ? null : message.getContent().trim())
                    .build();
            // MessageTextEntity 转换而来的xml
            String result = weChatBehaviorService.acceptUserBehavior(entity);
            log.info("接收公众号信息请求{}完成{}", openid, result);
            return result;
        } catch (Exception e) {
            log.error("接收微信公众号请求{} 失败 {}", openid, requestBody, e);
            return "";
        }
    }
    
}
