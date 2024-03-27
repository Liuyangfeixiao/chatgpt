CREATE database if NOT EXISTS `openai` default character set utf8mb4 collate utf8mb4_general_ci;
use `openai`;

# 转储表 openai_order
# ------------------------------------------------------------

DROP TABLE IF EXISTS `openai_order`;

CREATE TABLE `openai_order` (
                                `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增ID',
                                `openid` varchar(128) NOT NULL COMMENT '用户ID；微信分配的唯一ID编码',
                                `product_id` int(4) NOT NULL COMMENT '商品ID',
                                `product_name` varchar(32) NOT NULL COMMENT '商品名称',
                                `product_quota` int(8) NOT NULL COMMENT '商品额度',
                                `order_id` varchar(12) NOT NULL COMMENT '订单编号',
                                `order_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
                                `order_status` tinyint(1) NOT NULL COMMENT '订单状态；0-创建完成、1-等待发货、2-发货完成、3-系统关单',
                                `total_amount` decimal(10,2) NOT NULL COMMENT '订单金额',
                                `pay_type` tinyint(1) NOT NULL DEFAULT '0' COMMENT '支付方式；0-微信支付',
                                `pay_url` varchar(128) DEFAULT NULL COMMENT '支付地址；创建支付后，获得的URL地址',
                                `pay_amount` decimal(10,2) DEFAULT NULL COMMENT '支付金额；支付成功后，以回调信息更新金额',
                                `transaction_id` varchar(32) DEFAULT NULL COMMENT '交易单号；支付成功后，回调信息的交易单号',
                                `pay_status` tinyint(1) DEFAULT NULL COMMENT '支付状态；0-等待支付、1-支付完成、2-支付失败、3-放弃支付',
                                `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;



# 转储表 openai_product
# ------------------------------------------------------------

DROP TABLE IF EXISTS `openai_product`;

CREATE TABLE `openai_product` (
                                  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增ID',
                                  `product_id` int(4) NOT NULL COMMENT '商品ID',
                                  `product_name` varchar(32) NOT NULL COMMENT '商品名称',
                                  `product_desc` varchar(128) NOT NULL COMMENT '商品描述',
                                  `quota` int(8) NOT NULL COMMENT '额度次数',
                                  `price` decimal(10,2) NOT NULL COMMENT '商品价格',
                                  `sort` int(4) NOT NULL COMMENT '商品排序',
                                  `is_enabled` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否有效；0无效、1有效',
                                  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
