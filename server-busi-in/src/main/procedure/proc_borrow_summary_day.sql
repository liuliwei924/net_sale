DROP PROCEDURE IF EXISTS `proc_borrow_sumary_day` ;
DELIMITER $$  
CREATE PROCEDURE `proc_borrow_sumary_day`(startDate VARCHAR(20),endDate VARCHAR(20), yearMonthStr VARCHAR(20))
BEGIN   
/*成功签单字段*/
DECLARE successSignCount_today INT DEFAULT 0;
DECLARE sucMonthSignCount INT DEFAULT 0;
DECLARE sucTotalSignCount INT DEFAULT 0;

/*黄单字段*/
DECLARE signFailCount_today INT DEFAULT 0;
DECLARE failMonthSignCount INT DEFAULT 0;
DECLARE failTotalSignCount INT DEFAULT 0;

/*签单金额*/
DECLARE successSignAmount_today DECIMAL DEFAULT 0;
DECLARE sucTotalSignAmount DECIMAL DEFAULT 0;
DECLARE failSignAmount_today DECIMAL DEFAULT 0;
DECLARE failTotalSignAmount DECIMAL DEFAULT 0;

/*回款字段*/
DECLARE successRetCount_today INT DEFAULT 0;
DECLARE successRetAmount_today DECIMAL DEFAULT 0;
DECLARE sucMonthRetCount INT DEFAULT 0;
DECLARE sucMonthRetAmount DECIMAL DEFAULT 0;



	/*今日的成功签约量*/
 SET successSignCount_today = (SELECT COUNT(1) FROM t_treat_info n WHERE n.`status`=2 AND n.updateTime>=startDate AND n.updateTime<endDate);
 SET signFailCount_today = (SELECT COUNT(1) FROM t_treat_info n WHERE n.`status`=3 AND n.updateTime>=startDate AND n.updateTime<endDate);
 SET successSignAmount_today = (SELECT IFNULL(SUM(n.signAmount) ,0) FROM t_treat_info n WHERE n.`status`=2 AND n.updateTime>=startDate AND n.updateTime<endDate);
 SET failSignAmount_today = (SELECT IFNULL(SUM(n.signAmount) ,0) FROM t_treat_info n WHERE n.`status`=3 AND n.updateTime>=startDate AND n.updateTime<endDate);
 SET successRetCount_today =(SELECT COUNT(1) FROM t_treat_success n WHERE n.`status`=2 AND n.feeAmountDate>=startDate AND n.feeAmountDate<endDate); 
 SET successRetAmount_today = (SELECT IFNULL(ROUND(SUM(n.feeAmount),2),0) FROM t_treat_success n WHERE n.`status`=2 AND n.feeAmountDate>=startDate AND n.feeAmountDate<endDate); 

/* 本月累计的成功签约量，黄单量 回款*/
SELECT 
  IFNULL(SUM(n.successSignCount),0) + successSignCount_today,
	IFNULL(SUM(n.signFailCount),0) + signFailCount_today,
  IFNULL(SUM(n.successRetCount),0) + successRetCount_today,
  IFNULL(SUM(n.successRetAmount),0) + successRetAmount_today
 INTO sucMonthSignCount,failMonthSignCount,sucMonthRetCount,sucMonthRetAmount
 FROM t_borrow_sumary_day n WHERE  DATE_FORMAT(n.recordDate,'%Y-%m')=yearMonthStr;

/*历史累计的成功签约量*/
SELECT 
 IFNULL(SUM(n.successSignCount),0) + successSignCount_today,
 IFNULL(SUM(n.signFailCount),0) + signFailCount_today,
 IFNULL(SUM(n.successSignAmount),0) + successSignAmount_today,
 IFNULL(SUM(n.failSignAmount),0) + failSignAmount_today
 INTO sucTotalSignCount,failTotalSignCount,sucTotalSignAmount,failTotalSignAmount
 FROM t_borrow_sumary_day n;
 
	INSERT INTO t_borrow_sumary_day (
  `recordDate`,
  `applyCount`,
  `seniorCount`,
  `notFillCount`,
  `costAmount`,
  `browseCount`,
  `storeCount`,
	`kfReceivedCount`,
  `successBookCount`,
  `signingCount`,
  `successSignCount`,
  `sucMonthSignCount`,
  `sucTotalSignCount`,
  `successSignAmount`,
  `sucTotalSignAmount`,
  `signFailCount`,
  `failMonthSignCount`,
  `failTotalSignCount`,
  `failSignAmount`,
  `failTotalSignAmount`,
  `successRetCount`,
  `successRetAmount`,
  `returnAmount`,
  `feedBackCount` ,
  `signingXyCount`,
  `signingDyCount`,
  `signDjAmount`,
  `sucMonthRetCount`,
  `sucMonthRetAmount`
		)
	SELECT
	 startDate as 'recordDate',
	 (SELECT COUNT(1) FROM t_borrow_apply n WHERE n.applyTime>=startDate AND n.applyTime<endDate) as 'applyCount',
	 (SELECT COUNT(1) FROM t_borrow_apply n WHERE n.`applyType` in(1,6) AND n.applyTime>=startDate AND n.applyTime<endDate) as 'seniorCount',
	 (SELECT COUNT(1) FROM t_borrow_apply n WHERE (n.`haveDetail`=0 OR EXISTS (SELECT 1 FROM t_borrow_kf_record r WHERE r.handleType=10 AND n.applyId=r.applyId)) AND n.applyTime>=startDate AND n.applyTime<endDate) as 'notFillCount',
	 (SELECT IFNULL(SUM(amount),0) FROM t_borrow_channel_cost n WHERE n.recordDate=startDate) as 'costAmount',
	 (SELECT IFNULL(SUM(browseCount),0) FROM t_borrow_channel_cost n WHERE n.recordDate=startDate) as 'browseCount',
	 (SELECT COUNT(DISTINCT n.applyId) from t_borrow_store_record n WHERE n.handleType=0 AND n.createTime>=startDate AND n.createTime<endDate) as 'storeCount',
	 (SELECT COUNT(DISTINCT n.applyId) from t_borrow_store_record n JOIN t_borrow_apply e1 ON e1.applyId = n.applyId WHERE 
						n.handleType in(-1,0) AND n.createTime>= startDate AND n.createTime<endDate 
						AND IFNULL(TIMESTAMPDIFF(DAY,DATE_FORMAT(e1.lastKfTime,'%Y-%m-%d'),DATE_FORMAT(n.createTime,'%Y-%m-%d')),0)=0
						AND e1.stageStatus=1
		) as 'kfReceivedCount',
	 (SELECT COUNT(1) FROM t_treat_book n WHERE n.`status`=3 AND n.updateTime>=startDate AND n.updateTime<endDate) as 'successBookCount',
	 (SELECT COUNT(1) FROM t_treat_info n WHERE n.`status`=1 AND n.createTime>=startDate AND n.createTime<endDate) as 'signingCount',
	  successSignCount_today as 'successSignCount',
	  sucMonthSignCount as 'sucMonthSignCount',
	  sucTotalSignCount as 'sucTotalSignCount',
		successSignAmount_today as 'successSignAmount',
	  sucTotalSignAmount as 'sucTotalSignAmount',
		signFailCount_today as 'signFailCount',
		failMonthSignCount as 'failMonthSignCount',
		failTotalSignCount as 'failTotalSignCount',
		failSignAmount_today as 'failSignAmount',
	  failTotalSignAmount as 'failTotalSignAmount',
	  successRetCount_today as 'successRetCount',
	  successRetAmount_today as 'successRetAmount',
	 (SELECT IFNULL(ROUND(SUM(n.feeAmount),2),0) FROM t_treat_success n WHERE n.`status`=1 AND n.feeAmountDate>=startDate AND n.feeAmountDate<endDate) as 'returnAmount',
	 (SELECT COUNT(1) FROM t_borrow_store_record n WHERE n.isFeedback=1 AND n.createTime>=startDate AND n.createTime<endDate) as 'feedBackCount',
	 (SELECT COUNT(1) FROM t_treat_info n WHERE n.`status`=1 AND n.createTime>=startDate AND n.createTime<endDate AND EXISTS(
			SELECT 1 FROM t_treat_success m WHERE n.applyId = m.applyId  AND m.loanType=1
		)) as 'signingXyCount',
	(SELECT COUNT(1) FROM t_treat_info n WHERE n.`status`=1 AND n.createTime>=startDate AND n.createTime<endDate AND EXISTS(
			SELECT 1 FROM t_treat_success m WHERE n.applyId = m.applyId  AND m.loanType=2
	 )) as 'signingDyCount',
	(SELECT IFNULL(ROUND(SUM(n.treatyAmount),2),0) FROM t_treat_info n WHERE n.createTime>=startDate AND n.createTime<endDate) as 'signDjAmount',
	sucMonthRetCount as 'sucMonthRetCount',
	sucMonthRetAmount as 'sucMonthRetAmount'
  FROM DUAL ;
END

$$
DELIMITER;