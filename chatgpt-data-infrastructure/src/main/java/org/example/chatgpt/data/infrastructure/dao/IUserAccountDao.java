package org.example.chatgpt.data.infrastructure.dao;

import org.apache.ibatis.annotations.Mapper;
import org.example.chatgpt.data.infrastructure.po.UserAccountPO;

/**
 * @description 用户账户的DAO
 */
@Mapper
public interface IUserAccountDao {
    UserAccountPO queryUserAccount(String openid);

    int subAccountQuota(String openid);
}
