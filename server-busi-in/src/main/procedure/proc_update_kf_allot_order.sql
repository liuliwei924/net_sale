
DROP PROCEDURE IF EXISTS `proc_update_kf_allot_order`;
DELIMITER $$  
CREATE PROCEDURE `proc_update_kf_allot_order`(startTime VARCHAR(20),endTime VARCHAR(20),customerId BIGINT(20),limit_senior_count INT(10),limit_notfill_count INT(10))
BEGIN
		DECLARE my_notFill_applyIds VARCHAR(1000) DEFAULT NULL;
		DECLARE my_senior_applyIds VARCHAR(1000) DEFAULT NULL;
		DECLARE Z_cityNames VARCHAR(100) DEFAULT NULL;
    
		-- 真实分单量（未填写量）
		DECLARE my_real_notFill_count INT(10) DEFAULT 0;
		-- 真实分单量（优质量）
		DECLARE my_real_senior_count INT(10) DEFAULT 0;

		-- %w 以数字形式表示周中的天数（ 0 = Sunday, 1=Monday, . . ., 6=Saturday）
		SET @today_week = date_format(NOW(),'%w');
		SET Z_cityNames = '郑州市,青岛市,天津市';
		-- 优质单分配
		IF limit_senior_count>0 THEN
			BEGIN
				-- 周末
			  IF ((@today_week=0 OR @today_week=6) AND limit_senior_count>10) THEN
            SET limit_senior_count =10;
        ELSEIF (limit_senior_count>5) THEN
						SET limit_senior_count =5;
        END IF;
       (
				 SELECT GROUP_CONCAT(tab1.applyId),COUNT(1) 
				 INTO my_senior_applyIds,my_real_senior_count 
         FROM (SELECT n.applyId FROM t_borrow_apply n LEFT JOIN t_borrow_base n1 ON n.applyId=n1.applyId WHERE ((n.applyType = 1) OR (n.applyType=6 AND FIND_IN_SET(n1.cityName,Z_cityNames))) 
					AND n.`status`=0 AND n.applyName !='内部测试' AND n.applyTime>=startTime AND n.applyTime<=endTime 
          AND !FIND_IN_SET(n1.cityName,(select allotCitys FROM t_base_cfg))
					GROUP BY n.applyId LIMIT limit_senior_count) as tab1
				);
				
        SET my_senior_applyIds = TRIM(my_senior_applyIds);
				IF my_senior_applyIds IS NOT NULL AND my_senior_applyIds!='' THEN
					UPDATE t_borrow_apply t 
					SET t.`status` = 1,
							t.kfStatus = 0,
							t.lockBy = customerId,
							t.lastKf = customerId,
							t.lastKfDesc='系统自动分单',
							t.updateTime = NOW(),
							t.lastKfTime=NOW(),
							t.lockTime = NOW()
					WHERE FIND_IN_SET(t.applyId,my_senior_applyIds);

				 INSERT INTO t_borrow_kf_record(
						`applyId`,
						`kf`,
						`handleDesc`,
						`handleType`,
						`createTime`
					)
					SELECT 
					t.applyId,
					customerId,
					'系统自动分配优质单',
					1,
					NOW()
					FROM t_borrow_apply t  
					WHERE FIND_IN_SET(t.applyId,my_senior_applyIds);

					COMMIT; -- 提交事务 
				END IF;
      END; 
		END IF;
    
		IF limit_notfill_count>0 THEN  -- 未填写单分配
			BEGIN

         	-- 周末
			  IF ((@today_week=0 OR @today_week=6) AND limit_notfill_count>10) THEN
            SET limit_notfill_count =10;
        ELSEIF (limit_notfill_count>5) THEN
						SET limit_notfill_count =5;
        END IF;

				(
					SELECT GROUP_CONCAT(tab1.applyId),COUNT(1) 
					INTO my_notFill_applyIds,my_real_notFill_count 
					FROM (SELECT n.applyId FROM t_borrow_apply n LEFT JOIN t_borrow_base n1 ON n.applyId = n1.applyId 
							  WHERE n.haveDetail = 0 AND n.`status`=0 AND n.applyType NOT IN(3,4) AND n.applyName !='内部测试' 
								AND n.applyTime>=startTime AND n.applyTime<=endTime 
                AND !FIND_IN_SET(IFNULL(n1.cityName,n.cityName),(select allotCitys FROM t_base_cfg)) 
					GROUP BY n.applyId LIMIT limit_notfill_count) as tab1
				);
				SET my_notFill_applyIds = TRIM(my_notFill_applyIds);
				IF my_notFill_applyIds IS NOT NULL AND my_notFill_applyIds!='' THEN			
					UPDATE t_borrow_apply t 
					SET t.`status` = 1,
							t.kfStatus = 0,
							t.lockBy = customerId,
							t.lastKf = customerId,
							t.lastKfDesc='系统自动分单',
							t.updateTime = NOW(),
							t.lastKfTime=NOW(),
							t.lockTime = NOW()
					WHERE FIND_IN_SET(t.applyId,my_notFill_applyIds);

					INSERT INTO t_borrow_kf_record(
						`applyId`,
						`kf`,
						`handleDesc`,
						`handleType`,
						`createTime`
					)
					SELECT 
					t.applyId,
					customerId,
					'系统自动分配未填写单',
					1,
					NOW()
					FROM t_borrow_apply t  
					WHERE FIND_IN_SET(t.applyId,my_notFill_applyIds);
				
					COMMIT; -- 提交事务 
				END IF;
      END; 
		END IF;

   IF (my_real_notFill_count>0 OR my_real_senior_count>0) THEN
			
			UPDATE t_kf_allot_record t
			SET t.allotNotFillCount=t.allotNotFillCount+my_real_notFill_count,
					t.allotSeniorCount=t.allotSeniorCount+my_real_senior_count 
			WHERE
         t.customerId= customerId AND t.recordDate=CURDATE();
      COMMIT;

   END IF;
END
$$
DELIMITER;