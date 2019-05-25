CREATE TABLE `t_treat_visit_detail` (
  `detailId` bigint(20) NOT NULL AUTO_INCREMENT,
  `applyId` bigint(20) NOT NULL,
  `visitTime` datetime DEFAULT NULL COMMENT '上门时间',
  `recCustId` bigint(20) NOT NULL COMMENT '接待人',
  `visitType` varchar(255) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '添加类型 1手工 2访客登记',
  `createTime` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`detailId`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;