package org.example.chatgpt.data.domain.openai.service.rule.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.chatgpt.data.domain.openai.annotation.LogicStrategy;
import org.example.chatgpt.data.domain.openai.model.aggregates.ChatProcessAggregate;
import org.example.chatgpt.data.domain.openai.model.entity.RuleLogicEntity;
import org.example.chatgpt.data.domain.openai.model.entity.UserAccountQuotaEntity;
import org.example.chatgpt.data.domain.openai.model.vo.LogicCheckTypeVO;
import org.example.chatgpt.data.domain.openai.repository.IOpenAiRepository;
import org.example.chatgpt.data.domain.openai.service.rule.ILogicFilter;
import org.example.chatgpt.data.domain.openai.service.rule.factory.DefaultLogicFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
@LogicStrategy(logicModel = DefaultLogicFactory.LogicModel.USER_QUOTA)
public class UserQuotaFilter implements ILogicFilter<UserAccountQuotaEntity> {
    @Resource
    private IOpenAiRepository openAiRepository;
    @Override
    public RuleLogicEntity<ChatProcessAggregate> filter(ChatProcessAggregate chatProcess, UserAccountQuotaEntity data) {
        if (data.getSurplusQuota() > 0) {
            // 扣减账户额度；因为是个人账户数据，无资源竞争，所以直接使用数据库也可以。但为了效率，也可以优化为 Redis 扣减。
            // TODO 使用Redis缓存更新，加快效率
            int updateCount = openAiRepository.subAccountQuota(data.getOpenid());
            // 校验通过
            if (updateCount != 0) {
                return RuleLogicEntity.<ChatProcessAggregate>builder()
                        .type(LogicCheckTypeVO.SUCCESS)
                        .info(LogicCheckTypeVO.SUCCESS.getInfo())
                        .data(chatProcess).build();
            }
        }
        return RuleLogicEntity.<ChatProcessAggregate>builder()
                .type(LogicCheckTypeVO.REFUSE)
                .info("个人账户总额度 ["+data.getTotalQuota()+"] 次, 已经耗尽！")
                .data(chatProcess).build();
    }
}
