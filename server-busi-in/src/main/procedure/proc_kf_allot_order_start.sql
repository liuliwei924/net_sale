
DROP PROCEDURE IF EXISTS `proc_kf_allot_order_start`;
DELIMITER $$  
CREATE PROCEDURE `proc_kf_allot_order_start`(startTime VARCHAR(25),endTime VARCHAR(25))
BEGIN   
DECLARE notFillCount_total INT(10) DEFAULT 0;
DECLARE seniorCount_total INT(10) DEFAULT 0;

DECLARE c_notFillRate DECIMAL(6,2) DEFAULT 0.00;
DECLARE c_seniorRate DECIMAL(6,2) DEFAULT 0.00;
DECLARE c_customerId BIGINT(20) DEFAULT 0;
DECLARE c_isSenior SMALLINT(1) DEFAULT 0;

DECLARE my_notFillCount INT(10) DEFAULT 0;
DECLARE my_seniorCount INT DEFAULT 0;

DECLARE lj_notFillCount INT(10) DEFAULT 0;
DECLARE lj_seniorCount INT(10) DEFAULT 0;

DECLARE done INT(5) DEFAULT FALSE;

DECLARE Z_cityNames VARCHAR(100) DEFAULT NULL;

-- 定义游标 查询组长要分配的信息
DECLARE My_Cursor1 CURSOR FOR (SELECT customerId,notFillRate,seniorRate FROM t_kf_allot_rule);

-- 定义游标 算出各客服的分配数
DECLARE My_Cursor2 CURSOR FOR ( SELECT t.customerId,t.isSenior,t.notFillCount,t.seniorCount FROM t_kf_allot_tmp t LEFT JOIN t_kf_allot_record t1 ON t.customerId=t1.customerId AND t1.recordDate=CURDATE() ORDER BY t.seniorCount ASC,t1.allotSeniorCount ASC,t1.allotNotFillCount ASC);

-- 定义游标 算出剩余优质单的分配数
DECLARE My_Cursor3 CURSOR FOR ( SELECT t.customerId,t.notFillCount,t.seniorCount FROM t_kf_allot_tmp t LEFT JOIN t_kf_allot_record t1 ON t.customerId=t1.customerId AND t1.recordDate=CURDATE() WHERE t.isSenior=1 ORDER BY t.seniorCount ASC,t1.allotSeniorCount ASC); 

-- 定义游标 分配给客服
DECLARE My_Cursor4 CURSOR FOR ( SELECT customerId,allotSeniorCount,allotNotFillCount FROM t_kf_allot_tmp);  

-- 定义游标 未填写单人员排序
DECLARE My_Cursor5 CURSOR FOR ( SELECT t.customerId,t.notFillCount FROM t_kf_allot_tmp t LEFT JOIN t_kf_allot_record t1 ON t.customerId=t1.customerId AND t1.recordDate=CURDATE() ORDER BY t1.allotNotFillCount ASC);


DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE; -- 绑定控制变量到游标,游标循环结束自动转true 


SET Z_cityNames = '郑州市,青岛市,天津市';
-- 向前推3天
SET startTime = DATE_ADD(NOW(),INTERVAL -3 DAY);

-- 查出限制时间内的未填写量和优质量(未处理)
SELECT
(SELECT COUNT(1) FROM t_borrow_apply t LEFT JOIN t_borrow_base t1 ON t.applyId=t1.applyId WHERE t.haveDetail = 0 AND t.`status`=0 AND  t.applyType NOT IN(3,4) 
	AND !FIND_IN_SET(IFNULL(t1.cityName,t.cityName),(select allotCitys FROM t_base_cfg))
	AND t.applyName !='内部测试' AND t.applyTime>=startTime AND t.applyTime<=endTime
)as 'notFillCount_total',
(SELECT COUNT(1) FROM t_borrow_apply t LEFT JOIN t_borrow_base t1 ON t.applyId=t1.applyId WHERE 
((t.applyType = 1) OR (t.applyType=6 AND FIND_IN_SET(t1.cityName,Z_cityNames))) 
	AND t.`status`=0 AND t.applyName !='内部测试' 
	AND t.applyTime>=startTime AND t.applyTime<=endTime 
  AND !FIND_IN_SET(t1.cityName,(select allotCitys FROM t_base_cfg))
) as 'seniorCount_total'
INTO notFillCount_total,seniorCount_total
FROM DUAL;

-- 对于个别组员的额外分配（比如说，组长）
OPEN My_Cursor1; -- 打开游标  
  myLoop: LOOP -- 开始循环体,myLoop为自定义循环名,结束循环时用到  
    FETCH My_Cursor1 into c_customerId,c_notFillRate, c_seniorRate; -- 将游标当前读取行的数据顺序赋予自定义变量  
    IF done THEN -- 判断是否继续循环  
      LEAVE myLoop; -- 结束循环  
    END IF;
		  -- 插入客服记录
     SET @isExistKfRecord = (SELECT COUNT(1) FROM t_kf_allot_record WHERE customerId =c_customerId AND recordDate=CURDATE());
     IF @isExistKfRecord=0 THEN
				INSERT INTO t_kf_allot_record(recordDate,customerId) VALUES (CURDATE(),c_customerId);
				COMMIT;
		 END IF;

		SET my_notFillCount = ROUND(notFillCount_total*c_notFillRate/100);
		SET my_seniorCount = ROUND(seniorCount_total*c_seniorRate/100);
		
		IF my_notFillCount<1 THEN
				SET my_notFillCount=1;
		END IF;

		IF my_seniorCount<1 THEN
				SET my_seniorCount=1;
		END IF;

		CALL proc_update_kf_allot_order(startTime, endTime, c_customerId,my_seniorCount,my_notFillCount);

		SET lj_notFillCount = lj_notFillCount+my_notFillCount;
		SET lj_seniorCount = lj_seniorCount+my_seniorCount;

  END LOOP myLoop; -- 结束自定义循环体  
  CLOSE My_Cursor1; -- 关闭游标 


	SET notFillCount_total = notFillCount_total-lj_notFillCount;
	SET seniorCount_total = seniorCount_total-lj_seniorCount;

	 -- *************************************算出每个客服分配量 begin **************************************
	SET @kf_count = (SELECT COUNT(1) FROM t_kf_allot_tmp);
	SET @sy_NotFillcount = MOD(notFillCount_total,@kf_count);
	SET @everyone_NotFillCount = (notFillCount_total-@sy_NotFillcount)/@kf_count;

	SELECT COUNT(1),AVG(seniorCount),AVG(notFillCount) 
	INTO 
	@kf_senior_count,@kf_avg_senior_count,@kf_avg_notfill_count 
	FROM t_kf_allot_tmp WHERE isSenior=1;

	SET @sy_Seniorcount = MOD(seniorCount_total,@kf_senior_count);
  SET @everyone_seniorCount =(seniorCount_total-@sy_Seniorcount)/@kf_senior_count;

  SET done = FALSE;
	
  SET @tmp_notFillCount = 0;
	SET @tmp_seniorCount = 0;

	-- *************************************算出客服的分配量 begin **************************************

	OPEN My_Cursor5; -- 打开游标  未填写单
		myLoop: LOOP -- 开始循环体,myLoop为自定义循环名,结束循环时用到  
    FETCH My_Cursor5 into c_customerId,my_notFillCount; -- 将游标当前读取行的数据顺序赋予自定义变量  
    IF done THEN -- 判断是否继续循环  
      LEAVE myLoop; -- 结束循环  
    END IF;
		BEGIN
			IF @sy_NotFillcount>0 THEN
					SET @my_allot_notfill_count = @everyone_NotFillCount+1;
					SET @sy_NotFillcount=@sy_NotFillcount-1;
			ELSE
					SET @my_allot_notfill_count = @everyone_NotFillCount;
      END IF;

			UPDATE t_kf_allot_tmp SET allotNotFillCount=@my_allot_notfill_count WHERE customerId=c_customerId;
			COMMIT;
      
       -- 插入客服记录
     SET @isExistKfRecord = (SELECT COUNT(1) FROM t_kf_allot_record WHERE customerId =c_customerId AND recordDate=CURDATE());
     IF @isExistKfRecord=0 THEN
				INSERT INTO t_kf_allot_record(recordDate,customerId) VALUES (CURDATE(),c_customerId);
				COMMIT;
		 END IF;
     
		END;
  END LOOP myLoop; -- 结束自定义循环体  
  CLOSE My_Cursor5; -- 关闭游标 


  SET done = FALSE;
	OPEN My_Cursor2; -- 打开游标  
		myLoop: LOOP -- 开始循环体,myLoop为自定义循环名,结束循环时用到  
    FETCH My_Cursor2 into c_customerId,c_isSenior,my_notFillCount,my_seniorCount; -- 将游标当前读取行的数据顺序赋予自定义变量  
    IF done THEN -- 判断是否继续循环  
      LEAVE myLoop; -- 结束循环  
    END IF;
		BEGIN

			SET @my_allot_senior_count=0;

			IF (c_isSenior=1 AND @everyone_seniorCount>0) THEN
					SET @my_allot_senior_count =@everyone_seniorCount;
					
				IF(@everyone_seniorCount+@kf_avg_senior_count-my_seniorCount)<=0 THEN
					SET @my_allot_senior_count =0;
					SET @sy_Seniorcount = @sy_Seniorcount + @everyone_seniorCount;
				END IF;
			END IF;

			UPDATE t_kf_allot_tmp SET allotSeniorCount=@my_allot_senior_count WHERE customerId=c_customerId;
			COMMIT;
      
       -- 插入客服记录
     SET @isExistKfRecord = (SELECT COUNT(1) FROM t_kf_allot_record WHERE customerId =c_customerId AND recordDate=CURDATE());
     IF @isExistKfRecord=0 THEN
				INSERT INTO t_kf_allot_record(recordDate,customerId) VALUES (CURDATE(),c_customerId);
				COMMIT;
		 END IF;
     
		END;
  END LOOP myLoop; -- 结束自定义循环体  
  CLOSE My_Cursor2; -- 关闭游标 

	IF @sy_Seniorcount>seniorCount_total THEN
		SET @sy_Seniorcount = seniorCount_total;
	END IF;

	-- 将剩余优质单分配
	IF @sy_Seniorcount>0 THEN
				
			REPEAT
					SET done = FALSE;
					OPEN My_Cursor3; -- 打开游标  
						myLoop: LOOP -- 开始循环体,myLoop为自定义循环名,结束循环时用到  
						FETCH My_Cursor3 into c_customerId,my_notFillCount,my_seniorCount; -- 将游标当前读取行的数据顺序赋予自定义变量  
						IF (done OR @sy_Seniorcount<=0) THEN -- 判断是否继续循环  
							LEAVE myLoop; -- 结束循环  
						END IF;
						BEGIN
							SET @my_allot_senior_count =1;
							IF(1+@kf_avg_senior_count-my_seniorCount)<=0 THEN
								SET @my_allot_senior_count =0;
							END IF;
					
							UPDATE t_kf_allot_tmp SET allotSeniorCount=allotSeniorCount+@my_allot_senior_count WHERE customerId=c_customerId;
							COMMIT;
							SET @sy_Seniorcount=@sy_Seniorcount- @my_allot_senior_count;
						END;
					END LOOP myLoop; -- 结束自定义循环体  
					CLOSE My_Cursor3; -- 关闭游标 

			UNTIL @sy_Seniorcount<=0 END REPEAT;
		END IF;

-- *************************************算出客服的分配量 end **************************************


 -- *************************************开始分配 begin **************************************

	SET done = FALSE;

	OPEN My_Cursor4; -- 打开游标  
		myLoop: LOOP -- 开始循环体,myLoop为自定义循环名,结束循环时用到  
    FETCH My_Cursor4 into c_customerId,my_seniorCount,my_notFillCount; -- 将游标当前读取行的数据顺序赋予自定义变量  
    IF done THEN -- 判断是否继续循环  
      LEAVE myLoop; -- 结束循环  
    END IF;
		
		CALL proc_update_kf_allot_order(startTime, endTime, c_customerId,my_seniorCount,my_notFillCount);
	
  END LOOP myLoop; -- 结束自定义循环体  
  CLOSE My_Cursor4; -- 关闭游标 

 -- *************************************开始分配 end **************************************

END
$$
DELIMITER;