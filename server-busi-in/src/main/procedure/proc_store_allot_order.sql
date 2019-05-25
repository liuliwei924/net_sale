
DROP PROCEDURE IF EXISTS `proc_store_allot_order`;
DELIMITER $$  
CREATE PROCEDURE `proc_store_allot_order`(custId BIGINT(20),orgId BIGINT(20), cityName VARCHAR(20),allotOrderCount INT(10),orderArea SMALLINT(1))
BEGIN 
		DECLARE c_applyId BIGINT(20) DEFAULT 0;
		DECLARE c_applyType SMALLINT(1) DEFAULT 0;
		DECLARE c_orderType SMALLINT(1) DEFAULT 0;

		DECLARE done INT(5) DEFAULT FALSE;
		DECLARE My_Cursor1 CURSOR FOR (SELECT n.applyId,n.applyType,n.orderType FROM t_store_allot_tmp n
																		WHERE n.cityName=cityName 
																		AND n.orderArea=orderArea 
																		AND NOT EXISTS (SELECT 1 FROM t_borrow_store_record n1 WHERE n.applyId = n1.applyId AND n1.storeBy = custId AND n1.handleType=0 ) 
																		LIMIT allotOrderCount);
		DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE; -- 绑定控制变量到游标,游标循环结束自动转true 

		-- 插入记录
     SET @isExistRecord = (SELECT COUNT(1) FROM t_store_allot_record n WHERE n.customerId =custId  AND n.recordDate=CURDATE());
     IF @isExistRecord=0 THEN
				INSERT INTO t_store_allot_record(recordDate,customerId,allotNotFillCount,allotSeniorCount,totalCount) 
				VALUES (CURDATE(),custId ,0,0,0);
				COMMIT;
		 END IF;

    -- 优质单配置
     SELECT transOrderDay1,transOrderDay2,transOrderDay3,transOrderDay4 
     INTO @S_transOrderDay1,@S_transOrderDay2,@S_transOrderDay3,@S_transOrderDay4
     FROM t_trans_order_cfg WHERE orderLevel=1 LIMIT 1;

		-- 未填写单配置
     SELECT transOrderDay1,transOrderDay2,transOrderDay3,transOrderDay4 
     INTO @N_transOrderDay1,@N_transOrderDay2,@N_transOrderDay3,@N_transOrderDay4
     FROM t_trans_order_cfg WHERE orderLevel=2 LIMIT 1;

		OPEN My_Cursor1; -- 打开游标  
			myLoop: LOOP -- 开始循环体,myLoop为自定义循环名,结束循环时用到  
				FETCH My_Cursor1 into c_applyId,c_applyType,c_orderType; -- 将游标当前读取行的数据顺序赋予自定义变量  
				IF done THEN -- 判断是否继续循环  
					LEAVE myLoop; -- 结束循环  
				END IF;
				BEGIN
					UPDATE t_borrow_apply t 
					SET t.`status` = 2,
							t.storeStatus = 0,
							t.stageStatus=1,
							t.orgId = orgId,
							t.transTime=(
								CASE t.orderType
									WHEN 1 THEN date_add(NOW(), INTERVAL IF(t.haveDetail=0,@N_transOrderDay1,@S_transOrderDay1) day)
									WHEN 2 THEN date_add(NOW(), INTERVAL IF(t.haveDetail=0,@N_transOrderDay2,@S_transOrderDay2) day)
									WHEN 3 THEN date_add(NOW(), INTERVAL IF(t.haveDetail=0,@N_transOrderDay3,@S_transOrderDay3) day)
									WHEN 4 THEN date_add(NOW(), INTERVAL IF(t.haveDetail=0,@N_transOrderDay4,@S_transOrderDay4) day)
								ELSE t.transTime END
							),
							t.lastStore=custId ,
							t.updateTime = NOW()
					WHERE t.applyId=c_applyId;
				
					INSERT INTO t_borrow_store_record(
							`applyId` ,
							`storeBy` ,
							`robWay`,
							`score`,
							`amount` ,
							`readFlag`,
							`handleType`,
							`handleDesc` ,
							`createTime` ,
							`orderType`
						)
						VALUES 
						(
							c_applyId,
							custId ,
							0,
							0,
							0,
							1,
							0,
							'系统自动分单',
							NOW(),
							c_orderType
						);

				END;
     
      SELECT t.telephone,t.applyName,t.storeStatus INTO @c_telephone,@c_applyName,@c_storeStatus FROM t_borrow_apply t WHERE t.applyId=c_applyId;

			DELETE FROM t_work_list WHERE extraId=c_applyId AND workType=1;

			INSERT INTO t_work_list (
				extraId,
				workType,
				customerId,
				custTel,
				custName,
				allotTime,
				allotBy,
				allotDesc,
				remark
			)
			SELECT
				c_applyId,
				1,
				custId,
				t.telephone,
				t.applyName,
				NOW(),
				'sys',
				'系统分单',
				NULL
				FROM t_borrow_apply t WHERE t.applyId=c_applyId;
   

			UPDATE t_store_allot_record n
        SET
					n.allotNotFillCount=n.allotNotFillCount+IF(c_applyType=2,1,0),
					n.allotSeniorCount=n.allotSeniorCount+IF(c_applyType=1,1,0),
					n.totalCount = n.totalCount+1
				WHERE n.customerId=custId AND n.recordDate=CURDATE();

        DELETE FROM t_store_allot_tmp WHERE applyId=c_applyId;

				COMMIT;
			END LOOP myLoop; -- 结束自定义循环体  
		CLOSE My_Cursor1; -- 关闭游标 
END
$$
DELIMITER;