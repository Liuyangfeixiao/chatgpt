package org.example.chatgpt.data.domain.openai.service.rule.impl;

import com.google.common.cache.Cache;
import lombok.extern.slf4j.Slf4j;
import org.example.chatgpt.data.domain.openai.annotation.LogicStrategy;
import org.example.chatgpt.data.domain.openai.model.aggregates.ChatProcessAggregate;
import org.example.chatgpt.data.domain.openai.model.entity.RuleLogicEntity;
import org.example.chatgpt.data.domain.openai.model.entity.UserAccountQuotaEntity;
import org.example.chatgpt.data.domain.openai.model.vo.LogicCheckTypeVO;
import org.example.chatgpt.data.domain.openai.model.vo.UserAccountStatusVO;
import org.example.chatgpt.data.domain.openai.service.rule.ILogicFilter;
import org.example.chatgpt.data.domain.openai.service.rule.factory.DefaultLogicFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @description 访问次数限制过滤器
 */
@Slf4j
@Component
@LogicStrategy(logicModel = DefaultLogicFactory.LogicModel.ACCESS_LIMIT)
public class AccessLimitFilter implements ILogicFilter<UserAccountQuotaEntity> {
    @Value("${app.config.limit-count}")
    private Integer limitCount;
    @Value("${app.config.white-list}")
    private String whiteListStr;
    @Resource
    private Cache<String, Integer> visitCache;
    @Override
    public RuleLogicEntity<ChatProcessAggregate> filter(ChatProcessAggregate chatProcess, UserAccountQuotaEntity data) {
        try {
            // 白名单直接放行
            if (chatProcess.isWhiteList(whiteListStr)) {
                return RuleLogicEntity.<ChatProcessAggregate>builder()
                        .type(LogicCheckTypeVO.SUCCESS)
                        .data(chatProcess).build();
            }
            // 如果不在白名单中，则查看访问次数
            String openid = chatProcess.getOpenid();
            int visitCount = visitCache.get(openid, () -> 0);
            // 访问次数判断
            if (visitCount < limitCount) {
                visitCache.put(openid, visitCount+1);
                return RuleLogicEntity.<ChatProcessAggregate>builder()
                        .type(LogicCheckTypeVO.SUCCESS)
                        .data(chatProcess).build();
            }
            // 访问次数超过限制
            return RuleLogicEntity.<ChatProcessAggregate>builder()
                    .type(LogicCheckTypeVO.REFUSE)
                    .info("您今日的免费次数已经用尽!")
                    .data(chatProcess).build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
