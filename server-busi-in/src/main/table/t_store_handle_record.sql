CREATE TABLE `t_store_handle_record` (
  `applyId` bigint(20) NOT NULL COMMENT '申请ID',
  `customerId` bigint(20) NOT NULL COMMENT '门店人员',
  `handleDesc` varchar(255) DEFAULT NULL COMMENT '处理描述',
  `lastTime` datetime DEFAULT NULL COMMENT '最近更新时间',
  PRIMARY KEY (`applyId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='门店处理记录';

alter table xxjr_busi_in.t_work_list_fish drop  column fishDesc;