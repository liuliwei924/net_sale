package org.xxjr.job.listener.busi.store;

import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.ddq.common.util.StringUtil;
import org.llw.job.core.BaseExecteJob;
import org.llw.job.util.JobConstant;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/***
 * 门店分单
 * @author ZQH
 *
 */
@Lazy
@Component
public class StoreAutoAllotJob  implements BaseExecteJob{
	@Override
	public AppResult executeJob(AppParam param) {
		AppResult result = new AppResult();
		Object processId = param.getAttr(JobConstant.KEY_processId);
		Object cityName = param.getAttr("jobParams");
		String cityNames = StringUtil.getString(cityName);
		
		//分配新单
		result = StoreAutoAllotUtils.allotStoreNewOrder(processId , cityNames);
		
		//分配再分配单
		result = StoreAutoAllotUtils.allotStoreAgainOrder(processId, cityNames);
		
		return result;
	}
	
}
