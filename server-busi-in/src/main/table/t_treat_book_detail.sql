CREATE TABLE `t_treat_book_detail` (
  `detailId` bigint(20) NOT NULL AUTO_INCREMENT,
  `applyId` bigint(20) DEFAULT NULL COMMENT '申请人Id',
  `bookTime` datetime DEFAULT NULL COMMENT '预约上门时间',
  `bookCustId` bigint(20) DEFAULT NULL COMMENT '预约人',
  `createTime` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`detailId`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8;
