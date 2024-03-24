package org.example.chatgpt.data.domain.openai.repository;

import org.example.chatgpt.data.domain.openai.model.entity.UserAccountQuotaEntity;

/**
 * @description OpenAi的仓储接口
 */
public interface IOpenAiRepository {
    /**
     * @description 减少用户账户额度
     * @param openid
     * @return
     */
    int subAccountQuota(String openid);

    /**
     * 查找用户账户额度
     * @param openid
     * @return
     */
    UserAccountQuotaEntity queryUserAccount(String openid);
}
