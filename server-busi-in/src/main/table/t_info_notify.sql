
-- ----------------------------
-- Table structure for t_info_notify
-- ----------------------------
DROP TABLE IF EXISTS `t_info_notify`;
CREATE TABLE `t_info_notify` (
  `notifyId` bigint(20) NOT NULL AUTO_INCREMENT,
  `applyId` bigint(20) DEFAULT NULL COMMENT '申请ID',
  `customerId` bigint(20) DEFAULT NULL COMMENT '处理人',
  `applyName` varchar(255) DEFAULT NULL COMMENT '申请人姓名',
  `type` smallint(2) DEFAULT '1' COMMENT '1预约跟进  2预约上门',
  `notifyTime` varchar(50) DEFAULT NULL COMMENT '预约时间',
  `createTime` datetime DEFAULT NULL COMMENT '创建时间',
  `updateTime` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`notifyId`)
) ENGINE=InnoDB AUTO_INCREMENT=35 DEFAULT CHARSET=utf8 COMMENT='客户系统通知';
