package org.xxjr.job.listener.busi.sum;

import java.util.Date;

import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.ddq.common.util.DateUtil;
import org.llw.job.core.BaseExecteJob;
import org.llw.job.util.JobConstant;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.xxjr.job.listener.busi.sum.util.BaseSumUtil;
import org.xxjr.job.listener.busi.sum.util.BookSumUtil;
import org.xxjr.job.listener.busi.sum.util.RetSumUtil;
import org.xxjr.job.listener.busi.sum.util.SignSumUtil;
import org.xxjr.job.listener.busi.sum.util.SumTeamUtil;
import org.xxjr.job.listener.busi.sum.util.SumaryChannelUtil;


/**
 * 实时统计数据
 *
 */
@Lazy
@Component
public class RealTimeSumJob implements BaseExecteJob {

	@Override
	public AppResult executeJob(AppParam param) {
		AppResult result = new AppResult();
		Object processId = param.getAttr(JobConstant.KEY_processId);
		String today = DateUtil.toStringByParttern(new Date(),DateUtil.DATE_PATTERN_YYYY_MM_DD);
		String toMonth = DateUtil.toStringByParttern(new Date(),"yyyy-MM");
		
		//基本数据统计
		BaseSumUtil.channelBase(processId, today);
		BaseSumUtil.storeBase(processId, today);
		BaseSumUtil.orgBase(processId, today);
		BaseSumUtil.totalBase(processId, today);
		BaseSumUtil.storeBaseMonth(processId, toMonth);
		BaseSumUtil.riskBase(processId, today);
		
		//上门统计
		BookSumUtil.channelBook(processId, today);
		BookSumUtil.storeBook(processId, today);
		BookSumUtil.sumTotalBook(processId, today);
		BookSumUtil.storeBookMonth(processId, toMonth);
		
		//签单统计
		SignSumUtil.channelSign(processId, today);
		SignSumUtil.storeSign(processId, today);
		SignSumUtil.sumTotalSign(processId, today);
		SignSumUtil.storeSignMonth(processId, toMonth);
		
		//回款相关统计
		RetSumUtil.channelRet(processId, today);
		RetSumUtil.storeRet(processId, today);
		RetSumUtil.retByBase(processId, today);
		RetSumUtil.storeRetMonth(processId, toMonth);
	
		//简单实时统计
		SumaryChannelUtil.simpleSummary(processId,today);
		
	
		SumTeamUtil.sumBaseTeamData(processId, today);
		SumTeamUtil.updateFailSale(processId);
		
		//门店人员暂停分单统计
		BaseSumUtil.querystorePauseAddPool(processId);
		BaseSumUtil.storePauseAllotCount(processId);
		BaseSumUtil.storePauseAllotSum(processId);
		
		return result;
	}

}
