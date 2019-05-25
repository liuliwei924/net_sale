
DROP PROCEDURE IF EXISTS `proc_store_allot_order_start`;
DELIMITER $$  
CREATE  PROCEDURE `proc_store_allot_order_start`(startTime VARCHAR(25),endTime VARCHAR(25),orderType SMALLINT(1),orderArea SMALLINT(1),orderFlag SMALLINT(1),limitCount INT(4))
BEGIN 
		IF orderFlag=1 THEN
   		INSERT INTO t_store_allot_tmp (
			applyId,
			cityName,
			recordDate,
			applyType,
			orderType,
			orderArea,
			applyTime
			)
			SELECT 
				t.applyId,
				IFNULL(t1.cityName,t.cityName) as 'cityName',
				CURDATE(),
				orderFlag,
				t.orderType,
				orderArea,
				t.applyTime
			FROM
			 t_borrow_apply t
			 LEFT JOIN t_borrow_base t1 ON t.applyId = t1.applyId
			WHERE
					t.createTime>=startTime AND t.createTime<endTime
				AND t.`status` in(0,2,10) 
				AND FIND_IN_SET(IFNULL(t1.cityName,t.cityName),(select allotCitys FROM t_base_cfg )) 
				AND t.lastStore IS NULL
				AND t.orderType = orderType
				AND t.applyType in(1,2,6) AND t.haveDetail=1 
				AND t1.loanAmount>=4 
				AND t.applyId NOT IN (SELECT n.applyId FROM t_store_allot_tmp n)
				LIMIT 0,limitCount;

       COMMIT;
   
			UPDATE t_borrow_apply n SET n.`status`=9 WHERE n.applyId in(SELECT t.applyId FROM t_store_allot_tmp t); 
			COMMIT;
		ELSE
				INSERT INTO t_store_allot_tmp (
				applyId,
				cityName,
				recordDate,
				applyType,
				orderType,
				orderArea,
				applyTime
				)
				SELECT 
					t.applyId,
					IFNULL(t1.cityName,t.cityName) as 'cityName',
					CURDATE(),
					orderFlag,
					t.orderType,
					orderArea,
					t.applyTime
				FROM
				 t_borrow_apply t
				 LEFT JOIN t_borrow_base t1 ON t.applyId = t1.applyId
				WHERE
						t.createTime>=startTime AND t.createTime<endTime
					AND t.`status` in(0,2,10) 
					AND FIND_IN_SET(IFNULL(t1.cityName,t.cityName),(select allotCitys FROM t_base_cfg )) 
					AND t.lastStore IS NULL
					AND t.orderType = orderType
					AND IF(t1.loanAmount IS NULL,5,t1.loanAmount)>=4 
					AND t.haveDetail=0 AND t.applyType not in(3,4)
					AND t.applyId NOT IN (SELECT n.applyId FROM t_store_allot_tmp n)
					LIMIT 0,limitCount;

			COMMIT;
   
			UPDATE t_borrow_apply n SET n.`status`=9 WHERE n.applyId in(SELECT t.applyId FROM t_store_allot_tmp t); 
			COMMIT;
    END IF;
END
$$
DELIMITER;