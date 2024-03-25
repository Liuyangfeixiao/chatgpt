package org.example.chatgpt.data.domain.openai.service.rule.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.chatgpt.data.domain.openai.annotation.LogicStrategy;
import org.example.chatgpt.data.domain.openai.model.aggregates.ChatProcessAggregate;
import org.example.chatgpt.data.domain.openai.model.entity.RuleLogicEntity;
import org.example.chatgpt.data.domain.openai.model.entity.UserAccountQuotaEntity;
import org.example.chatgpt.data.domain.openai.model.vo.LogicCheckTypeVO;
import org.example.chatgpt.data.domain.openai.model.vo.UserAccountStatusVO;
import org.example.chatgpt.data.domain.openai.service.rule.ILogicFilter;
import org.example.chatgpt.data.domain.openai.service.rule.factory.DefaultLogicFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@LogicStrategy(logicModel = DefaultLogicFactory.LogicModel.ACCOUNT_STATUS)
public class AccountStatusFilter implements ILogicFilter<UserAccountQuotaEntity> {
    @Override
    public RuleLogicEntity<ChatProcessAggregate> filter(ChatProcessAggregate chatProcess, UserAccountQuotaEntity data) {
        // 模型校验通过
        if (UserAccountStatusVO.AVAILABLE.equals(data.getStatus())) {
            return RuleLogicEntity.<ChatProcessAggregate>builder()
                    .type(LogicCheckTypeVO.SUCCESS)
                    .info(LogicCheckTypeVO.SUCCESS.getInfo())
                    .data(chatProcess).build();
        }
        // 模型校验未通过
        return RuleLogicEntity.<ChatProcessAggregate>builder()
                .type(LogicCheckTypeVO.REFUSE)
                .info("您的账户已冻结，暂时不可使用。如果有疑问，可以联系客户解冻账户")
                .data(chatProcess).build();
    }
}
