package org.example.chatgpt.data.domain.openai.model.entity;

import org.example.chatgpt.data.domain.openai.model.vo.UserAccountStatusVO;
import org.example.chatgpt.data.types.common.Constants;

import java.util.Arrays;
import java.util.List;

/**
 * 用户账户额度实体对象
 */
public class UserAccountQuotaEntity {
    /**
     * 用户ID
     */
    private String openid;
    /**
     * 总量额度
     */
    private Integer totalQuota;
    /**
     * 剩余额度
     */
    private Integer surplusQuota;
    /**
     * 账户状态 0可用，1冻结
     */
    UserAccountStatusVO status;
    /**
     * 可用模型, 这个账户允许使用的模型范围
     */
    List<String> allowModels;
    public void getModelTypes(String modelTypes) {
        String[] models = modelTypes.split(Constants.SPLIT);
        this.allowModels = Arrays.asList(models);
    }
}
