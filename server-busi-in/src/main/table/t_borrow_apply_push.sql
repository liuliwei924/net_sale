
-- ----------------------------
-- Table structure for t_borrow_apply_push
-- ----------------------------
DROP TABLE IF EXISTS `t_borrow_apply_push`;
CREATE TABLE `t_borrow_apply_push` (
  `applyId` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '申请ID',
  `applyName` varchar(20) CHARACTER SET utf8 DEFAULT NULL COMMENT '申请人姓名',
  `telephone` char(11) DEFAULT NULL COMMENT '手机号码',
  `status` smallint(2) DEFAULT '0' COMMENT '状态（0-待推送 1推送成功 2推送失败 3挂卖）',
  `message` varchar(255) DEFAULT NULL,
  `pushType` int(11) DEFAULT '1' COMMENT '推送平台 （1-小钱包普惠）',
  `createTime` datetime DEFAULT NULL,
  `applyTime` datetime DEFAULT NULL COMMENT '申请时间',
  `updateTime` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`applyId`),
  UNIQUE KEY `telephone` (`telephone`) USING BTREE,
  KEY `applyName` (`applyName`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=22938 DEFAULT CHARSET=utf8mb4 COMMENT='推送其它平台列表';
