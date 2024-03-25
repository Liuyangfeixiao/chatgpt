package org.example.chatgpt.data.infrastructure.repository;

import org.example.chatgpt.data.domain.openai.model.entity.UserAccountQuotaEntity;
import org.example.chatgpt.data.domain.openai.model.vo.UserAccountStatusVO;
import org.example.chatgpt.data.domain.openai.repository.IOpenAiRepository;
import org.example.chatgpt.data.infrastructure.dao.IUserAccountDao;
import org.example.chatgpt.data.infrastructure.po.UserAccountPO;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * @description OpenAi 仓储实现
 */
@Repository
public class OpenAiRepository implements IOpenAiRepository {
    /**
     * @description 调用数据库进行增删查改
     */
    @Resource
    private IUserAccountDao userAccountDao;
    @Override
    public UserAccountQuotaEntity queryUserAccount(String openid) {
        UserAccountPO userAccountPO = userAccountDao.queryUserAccount(openid);
        if (null == userAccountPO) return null;
        // 对象转换
        UserAccountQuotaEntity userAccountQuotaEntity = new UserAccountQuotaEntity();
        userAccountQuotaEntity.setOpenid(userAccountPO.getOpenid());
        userAccountQuotaEntity.setTotalQuota(userAccountPO.getTotalQuota());
        userAccountQuotaEntity.setSurplusQuota(userAccountPO.getSurplusQuota());
        userAccountQuotaEntity.setStatus(UserAccountStatusVO.get(userAccountPO.getStatus()));
        userAccountQuotaEntity.getModelTypes(userAccountPO.getModelTypes());
        return userAccountQuotaEntity;
    }

    /**
     * @param openid
     * @return
     * @description 减少用户额度
     */
    @Override
    public Integer subAccountQuota(String openid) {
        return userAccountDao.subAccountQuota(openid);
    }
}
