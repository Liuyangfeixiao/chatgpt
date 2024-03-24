package org.example.chatgpt.data.infrastructure.dao;

import org.example.chatgpt.data.infrastructure.po.UserAccountPO;

/**
 * @description 用户账户的DAO
 */
public interface IUserAccountDao {
    UserAccountPO queryUserAccount();

    int subAccountQuota(String openid);
}
