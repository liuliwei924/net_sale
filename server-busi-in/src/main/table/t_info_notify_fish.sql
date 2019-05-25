
-- ----------------------------
-- Table structure for t_info_notify_fish
-- ----------------------------
DROP TABLE IF EXISTS `t_info_notify_fish`;
CREATE TABLE `t_info_notify_fish` (
  `notifyId` bigint(20) NOT NULL,
  `applyId` bigint(20) DEFAULT NULL COMMENT '申请ID',
  `customerId` bigint(20) DEFAULT NULL COMMENT '处理人',
  `applyName` varchar(255) DEFAULT NULL,
  `type` smallint(2) DEFAULT '1' COMMENT '1 预约跟进 2预约上门',
  `notifyTime` varchar(100) DEFAULT NULL COMMENT '预约时间',
  `createTime` datetime DEFAULT NULL COMMENT '创建时间',
  `updateTime` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`notifyId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='客户系统通知';

