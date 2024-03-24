# ************************************************************
# Sequel Ace SQL dump
# 版本号： 20050
#
# https://sequel-ace.com/
# https://github.com/Sequel-Ace/Sequel-Ace
#
# 主机: 127.0.0.1 (MySQL 5.6.39)
# 数据库: openai
# 生成时间: 2023-10-04 02:52:50 +0000
# ************************************************************

SET NAMES utf8mb4;
CREATE database if NOT EXISTS `openai` default character set utf8mb4 collate utf8mb4_general_ci;
use `openai`;

# 转储表 user_account
# ------------------------------------------------------------

DROP TABLE IF EXISTS `user_account`;

CREATE TABLE `user_account` (
                                `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增ID',
                                `openid` varchar(64) NOT NULL COMMENT '用户ID；这里用的是微信ID作为唯一ID，你也可以给用户创建唯一ID，之后绑定微信ID',
                                `total_quota` int(11) NOT NULL DEFAULT '0' COMMENT '总量额度；分配的总使用次数',
                                `surplus_quota` int(11) NOT NULL DEFAULT '0' COMMENT '剩余额度；剩余的可使用次数',
                                `model_types` varchar(128) NOT NULL COMMENT '可用模型；glm-3-turbo,glm-4,glm-4v,cogview-3',
                                `status` tinyint(1) NOT NULL DEFAULT '0' COMMENT '账户状态；0-可用、1-冻结',
                                `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                PRIMARY KEY (`id`),
                                UNIQUE KEY `uq_openid` (`openid`),
                                KEY `idx_surplus_quota_status` (`surplus_quota`,`status`)
) ENGINE=InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

LOCK TABLES `user_account` WRITE;
/*!40000 ALTER TABLE `user_account` DISABLE KEYS */;

INSERT INTO `user_account` (`id`, `openid`, `total_quota`, `surplus_quota`, `model_types`, `status`, `create_time`, `update_time`)
VALUES
    (1,'Assen',10,2,'glm-3-turbo,glm-4,glm-4v,cogview-3',0,'2024-3-23 18:56:13','2024-3-23 19:50:23');

/*!40000 ALTER TABLE `user_account` ENABLE KEYS */;
UNLOCK TABLES;

