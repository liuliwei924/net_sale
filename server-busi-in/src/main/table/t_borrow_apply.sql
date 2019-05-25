ALTER TABLE t_treat_book ADD COLUMN orgId int(11) DEFAULT null COMMENT '√≈µÍ±‡∫≈';
ALTER TABLE t_treat_info ADD COLUMN orgId int(11) DEFAULT null COMMENT '√≈µÍ±‡∫≈';
ALTER TABLE t_treat_success  ADD COLUMN orgId int(11) DEFAULT null COMMENT '√≈µÍ±‡∫≈';



ALTER TABLE t_borrow_store_day ADD COLUMN orgId int(11) DEFAULT null COMMENT '√≈µÍ±‡∫≈';
ALTER TABLE t_busi_cust ADD COLUMN orgId int(11) DEFAULT null COMMENT '√≈µÍ±‡∫≈';
ALTER TABLE t_borrow_apply ADD COLUMN orgId int(11) DEFAULT null COMMENT '√≈µÍ±‡∫≈';


CREATE TABLE `t_exclusive_order` (
  `applyId` bigint(20) NOT NULL,
  `customerId` bigint(20) DEFAULT NULL,
  `orgId` bigint(20) DEFAULT NULL COMMENT '√≈µÍID',
  `orderType` smallint(1) DEFAULT '1',
  `createTime` datetime DEFAULT NULL,
  PRIMARY KEY (`applyId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Œ“µƒ◊® Ùµ•';