package org.example.chatgpt.data.domain.openai.repository;

import org.example.chatgpt.data.domain.openai.model.entity.UserAccountQuotaEntity;

/**
 * @description OpenAi的仓储接口
 */
public interface IOpenAiRepository {
    /**
     * @param openid
     * @return
     * @description 减少用户账户额度
     */
    Integer subAccountQuota(String openid);

    /**
     * 查找用户账户额度
     * @param openid
     * @return
     */
    UserAccountQuotaEntity queryUserAccount(String openid);
}
