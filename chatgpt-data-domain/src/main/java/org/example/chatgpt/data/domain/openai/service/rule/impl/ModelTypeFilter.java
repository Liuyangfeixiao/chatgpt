package org.example.chatgpt.data.domain.openai.service.rule.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.chatgpt.data.domain.openai.annotation.LogicStrategy;
import org.example.chatgpt.data.domain.openai.model.aggregates.ChatProcessAggregate;
import org.example.chatgpt.data.domain.openai.model.entity.RuleLogicEntity;
import org.example.chatgpt.data.domain.openai.model.entity.UserAccountQuotaEntity;
import org.example.chatgpt.data.domain.openai.model.vo.LogicCheckTypeVO;
import org.example.chatgpt.data.domain.openai.service.rule.ILogicFilter;
import org.example.chatgpt.data.domain.openai.service.rule.factory.DefaultLogicFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@LogicStrategy(logicModel = DefaultLogicFactory.LogicModel.MODEL_TYPE)
public class ModelTypeFilter implements ILogicFilter<UserAccountQuotaEntity> {
    @Override
    public RuleLogicEntity<ChatProcessAggregate> filter(ChatProcessAggregate chatProcess, UserAccountQuotaEntity data) {
        List<String> types = data.getAllowModels();
        String model = chatProcess.getModel();
        // 校验通过
        if (types.contains(model)) {
            return RuleLogicEntity.<ChatProcessAggregate>builder()
                    .type(LogicCheckTypeVO.SUCCESS)
                    .info(LogicCheckTypeVO.SUCCESS.getInfo())
                    .data(chatProcess).build();
        }
        // 模型校验拦截
        return RuleLogicEntity.<ChatProcessAggregate>builder()
                .type(LogicCheckTypeVO.REFUSE)
                .info("当前账户不支持使用 " + model + " 模型，请联系客服进行升级")
                .data(chatProcess).build();
    }
}
