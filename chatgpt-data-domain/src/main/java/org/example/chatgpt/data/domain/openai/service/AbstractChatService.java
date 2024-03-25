package org.example.chatgpt.data.domain.openai.service;

import cn.bugstack.chatglm.session.OpenAiSession;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.example.chatgpt.data.domain.openai.model.aggregates.ChatProcessAggregate;
import org.example.chatgpt.data.domain.openai.model.entity.RuleLogicEntity;
import org.example.chatgpt.data.domain.openai.model.entity.UserAccountQuotaEntity;
import org.example.chatgpt.data.domain.openai.model.vo.LogicCheckTypeVO;
import org.example.chatgpt.data.domain.openai.repository.IOpenAiRepository;
import org.example.chatgpt.data.domain.openai.service.rule.factory.DefaultLogicFactory;
import org.example.chatgpt.data.types.common.Constants;
import org.example.chatgpt.data.types.exception.ChatGPTException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.Resource;

@Slf4j
public abstract class AbstractChatService implements IChatService{
    @Resource
    protected OpenAiSession openAiSession;
    @Resource
    protected IOpenAiRepository openAiRepository;
    @Override
    public ResponseBodyEmitter completions(ChatProcessAggregate chatProcess, ResponseBodyEmitter emitter) {
        try {
            // 1. 请求问答设置
            emitter.onCompletion(() -> {
                log.info("流式问答请求完成，使用模型{}", chatProcess.getModel());
            });
            emitter.onError(throwable -> log.info("流式问答请求异常，使用模型{}", chatProcess.getModel(), throwable));
            // 2. 查询用户信息, 做了一层防腐操作
            UserAccountQuotaEntity userAccountQuotaEntity = openAiRepository.queryUserAccount(chatProcess.getOpenid());
            
            // 3. 规则过滤：频次过滤，敏感词过滤，账户状态过滤，用户额度过滤，
            // 把要操作数据库的过滤器往后放
            RuleLogicEntity<ChatProcessAggregate> ruleLogicEntity = this.doCheckLogic(chatProcess,
                    userAccountQuotaEntity,
                    DefaultLogicFactory.LogicModel.ACCESS_LIMIT.getCode(),
                    DefaultLogicFactory.LogicModel.SENSITIVE_WORD.getCode(),
                    null == userAccountQuotaEntity ? null : DefaultLogicFactory.LogicModel.ACCOUNT_STATUS.getCode(),
                    null == userAccountQuotaEntity ? null : DefaultLogicFactory.LogicModel.MODEL_TYPE.getCode(),
                    null == userAccountQuotaEntity ? null : DefaultLogicFactory.LogicModel.USER_QUOTA.getCode()
            );
            // 没有通过规则过滤
            if (!LogicCheckTypeVO.SUCCESS.equals(ruleLogicEntity.getType())) {
                emitter.send(ruleLogicEntity.getInfo());
                emitter.complete();
                return emitter;
            }
            
            // 4. 应答处理
            this.doMessageResponse(chatProcess, emitter);
        } catch (Exception e) {
            throw new ChatGPTException(Constants.ResponseCode.UN_ERROR.getCode(), Constants.ResponseCode.UN_ERROR.getInfo());
        }
        
        // 4. 返回结果
        return emitter;
    }
    protected abstract void doMessageResponse(ChatProcessAggregate chatProcess, ResponseBodyEmitter emitter);
    
    protected abstract RuleLogicEntity<ChatProcessAggregate> doCheckLogic(ChatProcessAggregate chatProcess, UserAccountQuotaEntity data, String... logics);
}
