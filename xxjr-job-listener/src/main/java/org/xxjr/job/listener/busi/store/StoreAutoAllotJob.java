package org.xxjr.job.listener.busi.store;

import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.llw.job.core.BaseExecteJob;
import org.llw.job.util.JobConstant;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/***
 * 门店分单
 * @author LiulwS
 *
 */
@Lazy
@Component
public class StoreAutoAllotJob  implements BaseExecteJob{
	@Override
	public AppResult executeJob(AppParam param) {
		AppResult result = new AppResult();
		Object processId = param.getAttr(JobConstant.KEY_processId);
		
		result = OrgAllotUtils.allotOrgNewOrder(processId,1);// 分实时单
		
		result = OrgAllotUtils.allotOrgNewOrder(processId,2);// 分历史单
		
		//分配新单
		result = StoreAutoAllotUtils.allotStoreNewOrder(processId);
		
		//分配再分配单
		result = StoreAutoAllotUtils.allotStoreAgainOrder(processId);
		
		return result;
	}
	
}
