DROP VIEW IF EXISTS `v_store_per_count_today` ;

CREATE VIEW `v_store_per_count_today` AS SELECT 
		  date_format(now(), '%Y-%m-%d') as 'recordDate',
		 	t.customerId  as 'customerId',
		  t1.orgNo as 'orgNo',
			t.orgId as 'orgId',
		  t.realName as 'realName',
			t.telephone as 'telephone',
		  t1.orgName as 'orgName',
			t1.cityName as 'cityName',
		  (SELECT COUNT(1) FROM t_borrow_store_record n WHERE t.customerId = n.storeBy AND n.isFeedback=1 AND n.createTime>=date_format(now(), '%Y-%m-%d')) as 'feedBackCount',
		  (SELECT COUNT(DISTINCT n.applyId) FROM t_borrow_store_record n WHERE t.customerId = n.storeBy AND n.handleType=0 AND n.createTime>=date_format(now(), '%Y-%m-%d')) as 'receiveCount',
		  (SELECT COUNT(1) FROM t_treat_book n WHERE t.customerId = n.customerId AND n.`status`=3 AND n.updateTime>=date_format(now(), '%Y-%m-%d')) as 'successBookCount',
		  (SELECT COUNT(1) FROM t_treat_info n WHERE t.customerId = n.customerId AND n.`status`=1 AND n.createTime>=date_format(now(), '%Y-%m-%d')) as 'signingCount',
			(SELECT COUNT(1) FROM t_treat_info n WHERE t.customerId = n.customerId AND n.`status`=2 AND n.updateTime>=date_format(now(), '%Y-%m-%d')) as 'successSignCount',
			(SELECT IFNULL(SUM(n.signAmount) ,0) FROM t_treat_info n WHERE t.customerId = n.customerId AND n.`status`=2 AND n.updateTime>=date_format(now(), '%Y-%m-%d')) as 'successSignAmount',
			(SELECT IFNULL(SUM(n.signAmount) ,0) FROM t_treat_info n WHERE t.customerId = n.customerId AND n.`status`=2) as 'sucTotalSignAmount',
			(SELECT COUNT(1) FROM t_treat_info n JOIN t_borrow_base n1 ON n.applyId = n1.applyId WHERE t.customerId = n.customerId AND n.`status`=2) as 'sucTotalSignCount',
		  (SELECT COUNT(1) FROM t_treat_info n WHERE t.customerId = n.customerId AND n.`status`=3 AND n.updateTime>=date_format(now(), '%Y-%m-%d')) as 'signFailCount',
			(SELECT IFNULL(SUM(n.signAmount) ,0) FROM t_treat_info n WHERE t.customerId = n.customerId AND n.`status`=3 AND n.updateTime>=date_format(now(), '%Y-%m-%d')) as 'failSignAmount',
			(SELECT IFNULL(SUM(n.signAmount) ,0) FROM t_treat_info n WHERE t.customerId = n.customerId AND n.`status`=3 ) as 'failTotalSignAmount',
			(SELECT COUNT(1) FROM t_treat_info n JOIN t_borrow_base n1 ON n.applyId = n1.applyId WHERE t.customerId = n.customerId AND n.`status`=3 ) as 'failTotalSignCount',
			(SELECT COUNT(1) FROM t_treat_info n WHERE t.customerId = n.customerId AND n.`status`=1 AND n.updateTime>=date_format(now(), '%Y-%m-%d') AND EXISTS(
					SELECT 1 FROM t_treat_success m WHERE n.applyId = m.applyId AND n.customerId = m.customerId AND m.loanType=1
			 )) as 'signingXyCount',
			(SELECT COUNT(1) FROM t_treat_info n WHERE t.customerId = n.customerId AND n.`status`=1 AND n.updateTime>=date_format(now(), '%Y-%m-%d')AND EXISTS(
					SELECT 1 FROM t_treat_success m WHERE n.applyId = m.applyId AND n.customerId = m.customerId AND m.loanType=1
			 )) as 'signingDyCount',
			(SELECT IFNULL(ROUND(SUM(n.treatyAmount),2),0) FROM t_treat_info n WHERE t.customerId = n.customerId  AND n.updateTime>=date_format(now(), '%Y-%m-%d')) as 'signDjAmount',
       0 as 'accountedDj',
			(SELECT COUNT(1) FROM t_treat_success n WHERE t.customerId = n.customerId AND n.`status`=2 AND n.feeAmountDate=date_format(now(), '%Y-%m-%d')) as 'successRetCount',
			(SELECT IFNULL(ROUND(SUM(n.feeAmount),2),0) FROM t_treat_success n WHERE t.customerId = n.customerId AND n.`status`=1 AND n.feeAmountDate=date_format(now(), '%Y-%m-%d')) as 'returnAmount',
			(SELECT IFNULL(ROUND(SUM(n.feeAmount),2),0) FROM t_treat_success n WHERE t.customerId = n.customerId AND n.`status`=2 AND n.feeAmountDate=date_format(now(), '%Y-%m-%d')) as 'successRetAmount',
			(SELECT COUNT(1) FROM t_treat_success n WHERE t.customerId = n.customerId AND n.`status`=2 AND DATE_FORMAT(n.feeAmountDate,'%Y-%m') = date_format(now(), '%Y-%m')) as 'sucMonthRetCount',
			(SELECT IFNULL(ROUND(SUM(n.feeAmount),2),0) FROM t_treat_success n WHERE t.customerId = n.customerId AND n.`status`=2 AND DATE_FORMAT(n.feeAmountDate,'%Y-%m') = date_format(now(), '%Y-%m')) as 'sucMonthRetAmount'
		FROM t_busi_cust t 
		LEFT JOIN t_org t1 ON t.orgId = t1.orgId
		WHERE t.roleType=3