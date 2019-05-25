package org.xxjr.job.listener.busi.sum.util;

import java.util.List;
import java.util.Map;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.util.LogerUtil;
import org.ddq.common.core.service.RemoteInvoke;
import org.xxjr.sys.util.ServiceKey;

/**
 * 每天-渠道自动统计任务
 * 
 * @author liulw
 * @date   2017年2月22日---上午9:27:22
 * @problem
 * @answer
 */
public class SumaryChannelUtil{
	
	/***
	 * 渠道简单统计
	 * @param processId
	 */
	public  static void simpleSummary(Object processId,String today) {
		AppResult result = new AppResult();
		try {
			LogerUtil.log("***********执行自动保存统计数据开始**************");
			//查询需要保存的数据
			AppParam params = new AppParam();
			params.addAttr("recordDate", today);

			params.setService("sumUtilExtService");
			params.setMethod("channelSimpleByToday");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(params);

			List<Map<String,Object>> dataList = result.getRows();
			if(dataList.size()>0){
				AppParam updateParam = new AppParam("borrowChannelRecordService","batchInsert");
				updateParam.addAttr("recordDate", today);
				updateParam.addAttr("list", dataList);
				updateParam.setRmiServiceName(AppProperties
						.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
				result = RemoteInvoke.getInstance().call(updateParam);
			}

			result.setMessage("成功处理了：" + dataList.size() +" 条记录");
			LogerUtil.log("***********成功处理了："+dataList.size()+"条记录**************");
			LogerUtil.log("***********执行自动更新浏览次数结束**************");
		}catch(Exception e) {
			LogerUtil.error(SumaryChannelUtil.class, e, "AutoSaveTotalDataJob >>>>>>>>>>>>>>>>>>error");
		}

	}


}

