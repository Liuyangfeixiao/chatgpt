package org.example.chatgpt.data.trigger.http;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.example.chatgpt.data.domain.auth.model.entity.AuthStateEntity;
import org.example.chatgpt.data.domain.auth.model.vo.AuthTypeVo;
import org.example.chatgpt.data.domain.auth.service.IAuthService;
import org.example.chatgpt.data.domain.weixin.model.entity.MessageTextEntity;
import org.example.chatgpt.data.domain.weixin.model.entity.UserBehaviorMessageEntity;
import org.example.chatgpt.data.domain.weixin.model.vo.MsgTypeVO;
import org.example.chatgpt.data.domain.weixin.service.IWeChatBehaviorService;
import org.example.chatgpt.data.types.common.Constants;
import org.example.chatgpt.data.types.model.Response;
import org.example.chatgpt.data.types.sdk.weixin.XmlUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Slf4j
@RestController
@RequestMapping("/api/${app.config.api-version}/auth")
@CrossOrigin("${app.config.cross-origin}")
public class AuthController {
    @Resource
    private IAuthService authService;
    @Resource
    private IWeChatBehaviorService weChatBehaviorService;
    
    /**
     * 生成验证码用于测试
     * curl -X POST \
     * http://localhost:8089/api/v1/auth/gen/code \
     * -H 'Content-Type: application/x-www-form-urlencoded' \
     * -d 'openid=oxfA9w8-23yvwTmo2ombz0E4zJv4'
     * @param openid
     * @return
     */
    @RequestMapping(value = "gen/code", method = RequestMethod.POST)
    public Response<String> genCode(@RequestParam String openid) {
        log.info("开始生成验证码, 用户ID: {}", openid);
        try {
            UserBehaviorMessageEntity userBehaviorMessageEntity = UserBehaviorMessageEntity.builder()
                    .openId(openid)
                    .msgType(MsgTypeVO.TEXT.getCode())
                    .content("405").build();
            String xml = weChatBehaviorService.acceptUserBehavior(userBehaviorMessageEntity);
            MessageTextEntity messageTextEntity = XmlUtil.xmlToBean(xml, MessageTextEntity.class);
            log.info("生成验证码成功，用户ID:{}, 结果:{}", openid, messageTextEntity.getContent());
            return Response.<String>builder()
                    .code(Constants.ResponseCode.SUCCESS.getCode())
                    .info(Constants.ResponseCode.SUCCESS.getInfo())
                    .build();
        } catch (Exception e) {
            log.info("生成验证码失败，用户ID: {}", openid);
            return Response.<String>builder()
                    .code(Constants.ResponseCode.TOKEN_ERROR.getCode())
                    .info(Constants.ResponseCode.TOKEN_ERROR.getInfo())
                    .build();
        }
    }
    
    /**
     * 鉴权，根据鉴权结果返回 Token 码
     * curl -X POST \
     * http://localhost:8089/api/v1/auth/login \
     * -H 'Content-Type: application/x-www-form-urlencoded' \
     * -d 'code=6880'
     * @param code
     * @return
     */
    @RequestMapping(value = "login", method = RequestMethod.POST)
    public Response<String> doLogin(@RequestParam String code) {
        log.info("鉴权登录开始, 验证码: {}", code);
        try {
            AuthStateEntity authStateEntity = authService.doLogin(code);
            log.info("鉴权登录校验完成，验证码: {} 结果: {}", code, JSON.toJSONString(authStateEntity));
            // 拦截，鉴权失败
            if (!AuthTypeVo.SUCCESS.getCode().equals(authStateEntity.getCode())) {
                return Response.<String>builder()
                        .code(Constants.ResponseCode.TOKEN_ERROR.getCode())
                        .info(Constants.ResponseCode.TOKEN_ERROR.getInfo())
                        .build();
            }
            // 鉴权成功，放行
            return Response.<String>builder()
                    .code(Constants.ResponseCode.SUCCESS.getCode())
                    .info(Constants.ResponseCode.SUCCESS.getInfo())
                    .data(authStateEntity.getToken())
                    .build();
        } catch (Exception e) {
            log.error("鉴权登录校验失败, 验证码: {}", code);
            return Response.<String>builder()
                    .code(Constants.ResponseCode.UN_ERROR.getCode())
                    .info(Constants.ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }
}
