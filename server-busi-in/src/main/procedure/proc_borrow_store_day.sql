DROP PROCEDURE IF EXISTS `proc_borrow_store_day` ;
DELIMITER $$  
CREATE  PROCEDURE `proc_borrow_store_day`(startDate VARCHAR(20),endDate VARCHAR(20), yearMonthStr VARCHAR(20))
BEGIN   

	INSERT INTO t_borrow_store_day (
  `recordDate`,
  `customerId`,
  `orgId`,
	`feedBackCount`,
	`receiveCount`,
	`successBookCount`,
	`signingCount`,
	`successSignCount`,
	 successSignAmount,
	`signFailCount`,
	 failSignAmount,
	`signingXyCount`,
	`signingDyCount`,
	`signDjAmount`,
	`successRetCount`,
  `returnAmount`,
	`successRetAmount`,
	`sucMonthSignCount`,
	`sucTotalSignCount`,
	`failMonthSignCount`,
	`failTotalSignCount`,
	 sucMonthRetCount,
	 sucMonthRetAmount
		)
SELECT 
  startDate as 'recordDdate',
  t.customerId  as 'customerId',
  t.orgId,
  (SELECT COUNT(1) FROM t_borrow_store_record n WHERE n.isFeedback=1 AND t.customerId = n.storeBy AND n.createTime>=startDate AND n.createTime<endDate) as 'feedBackCount',
  (SELECT COUNT(DISTINCT n.applyId) FROM t_borrow_store_record n WHERE t.customerId = n.storeBy AND n.handleType=0 AND n.createTime>=startDate AND n.createTime<endDate) as 'receiveCount',
  (SELECT COUNT(1) FROM t_treat_book n WHERE t.customerId = n.customerId AND n.`status`=3 AND n.updateTime>=startDate AND n.updateTime<endDate) as 'successBookCount',
  (SELECT COUNT(1) FROM t_treat_info n WHERE t.customerId = n.customerId AND n.`status`=1 AND n.createTime>=startDate AND n.createTime<endDate) as 'signingCount',
	(SELECT COUNT(1) FROM t_treat_info n WHERE t.customerId = n.customerId AND n.`status`=2 AND n.updateTime>=startDate AND n.updateTime<endDate) as 'successSignCount',
  (SELECT IFNULL(SUM(n.signAmount) ,0) FROM t_treat_info n WHERE t.customerId = n.customerId AND n.`status`=2 AND n.updateTime>=startDate AND n.updateTime<endDate) as 'successSignAmount',
  (SELECT COUNT(1) FROM t_treat_info n WHERE t.customerId = n.customerId AND n.`status`=2 AND n.updateTime>=startDate AND n.updateTime<endDate) as 'signFailCount',
  (SELECT IFNULL(SUM(n.signAmount) ,0) FROM t_treat_info n WHERE t.customerId = n.customerId AND n.`status`=3 AND n.updateTime>=startDate AND n.updateTime<endDate) as 'failSignAmount',
	(SELECT COUNT(1) FROM t_treat_info n WHERE t.customerId = n.customerId AND n.`status`=1 AND n.createTime>=startDate AND n.createTime<endDate AND EXISTS(
			SELECT 1 FROM t_treat_success m WHERE n.applyId = m.applyId AND n.customerId = m.customerId AND m.loanType=1
	 )) as 'signingXyCount',
	(SELECT COUNT(1) FROM t_treat_info n WHERE t.customerId = n.customerId AND n.`status`=1 AND n.createTime>=startDate AND n.createTime<endDate AND EXISTS(
			SELECT 1 FROM t_treat_success m WHERE n.applyId = m.applyId AND n.customerId = m.customerId AND m.loanType=2
	 )) as 'signingDyCount',
	(SELECT IFNULL(ROUND(SUM(n.treatyAmount),2),0) FROM t_treat_info n WHERE t.customerId = n.customerId  AND n.createTime>=startDate AND n.createTime<endDate) as 'signDjAmount',
	(SELECT COUNT(1) FROM t_treat_success n WHERE t.customerId = n.customerId AND n.`status`=2 AND n.feeAmountDate>=startDate AND n.feeAmountDate<endDate) as 'successRetCount',
	(SELECT IFNULL(ROUND(SUM(n.feeAmount),2),0) FROM t_treat_success n WHERE t.customerId = n.customerId AND n.`status`=1 AND n.feeAmountDate>=startDate AND n.feeAmountDate<endDate) as 'returnAmount',
	(SELECT IFNULL(ROUND(SUM(n.feeAmount),2),0) FROM t_treat_success n WHERE t.customerId = n.customerId AND n.`status`=2 AND n.feeAmountDate>=startDate AND n.feeAmountDate<endDate) as 'successRetAmount',
  (SELECT IFNULL(SUM(n.successSignCount),0) FROM t_borrow_store_day n WHERE t.customerId = n.customerId AND DATE_FORMAT(n.recordDate,'%Y-%m') = yearMonthStr) as 'sucMonthSignCount',
	(SELECT IFNULL(SUM(n.successSignCount),0) FROM t_borrow_store_day n WHERE t.customerId = n.customerId ) as 'sucTotalSignCount',
	(SELECT IFNULL(SUM(n.signFailCount),0) FROM t_borrow_store_day n WHERE t.customerId = n.customerId AND DATE_FORMAT(n.recordDate,'%Y-%m') = yearMonthStr) as 'failMonthSignCount',
	(SELECT IFNULL(SUM(n.signFailCount),0) FROM t_borrow_store_day n WHERE t.customerId = n.customerId )as 'failTotalSignCount',
	(SELECT COUNT(1) FROM t_treat_success n WHERE t.customerId = n.customerId AND n.`status`=2 AND DATE_FORMAT(n.feeAmountDate,'%Y-%m') = yearMonthStr) as 'sucMonthRetCount',
  (SELECT IFNULL(ROUND(SUM(n.feeAmount),2),0) FROM t_treat_success n WHERE t.customerId = n.customerId AND n.`status`=2 AND DATE_FORMAT(n.feeAmountDate,'%Y-%m') = yearMonthStr) as 'sucMonthRetAmount'

FROM t_busi_cust t WHERE t.roleType=3 AND t.orgId IS NOT NULL AND t.orgId !=60;

COMMIT;

 UPDATE t_borrow_store_day t1 JOIN t_busi_cust t2 ON t1.customerId = t2.customerId
 SET
		t1.sucMonthSignCount = t1.sucMonthSignCount + t1.successSignCount,
		t1.sucTotalSignCount = t1.sucTotalSignCount + t1.successSignCount,
		t1.failMonthSignCount = t1.failMonthSignCount + t1.signFailCount,
		t1.failTotalSignCount = t1.failTotalSignCount + t1.signFailCount,
    t1.failTotalSignAmount = t1.failTotalSignAmount + t1.failSignAmount,
    t1.sucTotalSignAmount = t1.sucTotalSignAmount + t1.successSignAmount,
		t1.sucMonthRetCount = t1.sucMonthRetCount + t1.successRetCount,
    t1.sucMonthRetAmount = t1.sucMonthRetAmount + t1.successRetAmount

 WHERE t1.recordDate=startDate;
  
END

$$
DELIMITER;