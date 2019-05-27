package org.xxjr.job.listener.busi.sum;

import java.util.Date;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.service.RemoteInvoke;
import org.ddq.common.util.DateUtil;
import org.llw.job.core.BaseExecteJob;
import org.llw.job.util.JobConstant;
import org.llw.model.cache.RedisUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.xxjr.job.listener.busi.store.StoreNotifyUtils;
import org.xxjr.job.listener.busi.sum.util.BaseSumUtil;
import org.xxjr.job.listener.busi.sum.util.BookSumUtil;
import org.xxjr.job.listener.busi.sum.util.RetSumUtil;
import org.xxjr.job.listener.busi.sum.util.SignSumUtil;
import org.xxjr.job.listener.busi.sum.util.SumTeamUtil;
import org.xxjr.job.listener.busi.sum.util.SumaryChannelUtil;
import org.xxjr.sys.util.ServiceKey;

/**
 * 延迟统计数据
 *
 */
@Lazy
@Component
public class DelaySumJob implements BaseExecteJob {

	@Override
	public AppResult executeJob(AppParam param) {
		AppResult result = new AppResult();
		Object processId = param.getAttr(JobConstant.KEY_processId);
		String today = DateUtil.toStringByParttern(DateUtil.getNextDay(new Date(),-1),DateUtil.DATE_PATTERN_YYYY_MM_DD);
		String toMonth = DateUtil.toStringByParttern(new Date(),"yyyy-MM");
		
		//基本数据统计
		BaseSumUtil.channelBase(processId, today);
		BaseSumUtil.storeBase(processId, today);
		BaseSumUtil.orgBase(processId, today);
		BaseSumUtil.totalBase(processId, today);
		BaseSumUtil.storeBaseMonth(processId, toMonth);
		BaseSumUtil.riskBase(processId, today);
		BaseSumUtil.dealOrderTypeSum(processId, today);
		BaseSumUtil.orderRateSum(processId, today);
		BaseSumUtil.channelDealOrderTypeSum(processId, today);
		BaseSumUtil.orgDealOrderSum(processId, today);
		
		//上门统计
		BookSumUtil.channelBook(processId, today);
		BookSumUtil.storeBook(processId, today);
		BookSumUtil.sumTotalBook(processId, today);
		BookSumUtil.storeBookMonth(processId, toMonth);
		
		//签单统计
		SignSumUtil.signFailChannel(processId, today);
		SignSumUtil.channelSign(processId, today);
		SignSumUtil.storeSign(processId, today);
		SignSumUtil.sumTotalSign(processId, today);
		SignSumUtil.storeSignMonth(processId, toMonth);
		
		//回款相关统计
		RetSumUtil.channelRet(processId, today);
		RetSumUtil.storeRet(processId, today);
		RetSumUtil.retByBase(processId, today);
		RetSumUtil.storeRetMonth(processId, toMonth);
		
		//门店相关通知
		StoreNotifyUtils.exclusiveNotDeal(processId);
		
		//简单实时统计
		SumaryChannelUtil.simpleSummary(processId,today);

		
		SumTeamUtil.sumBaseTeamData(processId, today);
		SumTeamUtil.updateFailSale(processId);
		return result;
	}

	public static final String allApplyCountKey = "key_all_apply_count";
	
	public static void refreshApplyCount () {
		AppParam queryParam = new AppParam();
		String timeStr = DateUtil.toStringByParttern(new Date(), DateUtil.DATE_PATTERN_YYYY_MM_DD);
		queryParam.addAttr("queryCountTime", timeStr);
		int queryCount = queryCount(queryParam);
		RedisUtils.getRedisService().set(allApplyCountKey, queryCount);
	}
	
	public static int queryCount(AppParam params) {
		params.setService("borrowApplyService");
		params.setMethod("queryCount");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START
						+ ServiceKey.Key_busi_in));

		AppResult result = RemoteInvoke.getInstance().callNoTx(params);

		int count = (Integer) result.getAttr(DuoduoConstant.TOTAL_SIZE);

		return count;
	}
}
