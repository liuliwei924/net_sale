package org.xxjr.job.listener.busi.sum.util;

import java.util.List;
import java.util.Map;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.service.RemoteInvoke;
import org.ddq.common.util.LogerUtil;
import org.llw.job.util.JobUtil;
import org.xxjr.sys.util.NumberUtil;
import org.xxjr.sys.util.OrgUtils;
import org.xxjr.sys.util.ServiceKey;

/**
 * 
 * 上门统计工具类
 * @author liulw
 *
 */
public class BookSumUtil {

	/**
	 * 渠道上门统计
	 * @param processId
	 * @param today
	 */
	public static void channelBook(Object processId, String today) {
		AppResult result = new AppResult();
		try {
			LogerUtil.log("BookSumUitl channelBook>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> start");
			//获取统计数据
			AppParam queryParam = new AppParam("sumUtilExtService","channelBook");
			queryParam.addAttr("today", today);
			queryParam.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START+ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(queryParam);
			List<Map<String,Object>> dataList = result.getRows();
			int size = 0;
			if(dataList.size()>0){
				//将统计数据插入统计表
				AppParam insertParam = new AppParam("sumBookChannelService","save");
				insertParam.addAttr("today", today);
				insertParam.addAttr("list", dataList);
				insertParam.setRmiServiceName(AppProperties
						.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
				result = RemoteInvoke.getInstance().call(insertParam);
				size =NumberUtil.getInt(result.getAttr(DuoduoConstant.DAO_Insert_SIZE), 0);
			}
			LogerUtil.log("BookSumUitl channelBook >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> end insert count=" +size);
		} catch (Exception e) {
			LogerUtil.error(BookSumUtil.class,e, "BookSumUitl channelBook error");
			JobUtil.addProcessExecute(processId, "统计渠道上门数据 报错：" + e.getMessage() );
		}
	}
	
	/**
	 * 门店经理上门统计
	 * @param processId
	 * @param today
	 */
	public static void storeBook(Object processId, String today) {
		AppResult result = new AppResult();
		try {
			LogerUtil.log("BookSumUitl storeBook>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> start");
			//获取统计的门店列表
			List<Map<String, Object>> orgList = OrgUtils.getIsCountOrgList();
			int size = 0;
			for(Map<String, Object> map : orgList){
				//门店ID
				int orgId = NumberUtil.getInt(map.get("orgId"),0);
				if(0 == orgId){
					continue;
				}
				//获取统计数据
				AppParam queryParam = new AppParam("sumUtilExtService","storeBook");
				queryParam.addAttr("today", today);
				queryParam.addAttr("orgId", orgId);
				queryParam.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START+ServiceKey.Key_busi_in));
				result = RemoteInvoke.getInstance().callNoTx(queryParam);
				List<Map<String,Object>> dataList = result.getRows();
				if(dataList.size()>0){
					//将统计数据插入统计表
					AppParam insertParam = new AppParam("sumBookStoreService","save");
					insertParam.addAttr("today", today);
					insertParam.addAttr("orgId", orgId);
					insertParam.addAttr("list", dataList);
					insertParam.setRmiServiceName(AppProperties
							.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
					result = RemoteInvoke.getInstance().call(insertParam);
					size += NumberUtil.getInt(result.getAttr(DuoduoConstant.DAO_Insert_SIZE), 0);
				}
			}
			LogerUtil.log("BookSumUitl storeBook >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> end insert count=" +size);
		} catch (Exception e) {
			LogerUtil.error(BookSumUtil.class,e, "BookSumUitl storeBook error");
			JobUtil.addProcessExecute(processId, "统计门店经理上门数据 报错：" + e.getMessage() );
		}
	}
	
	/**
	 * -总的上门统计(按处理时间)
	 * @param processId
	 * @param today
	 */
	public static void sumTotalBook(Object processId, String today) {
		AppResult result = new AppResult();
		try {
			LogerUtil.log("BookSumUitl sumTotalBook>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> start");
			//获取统计数据
			AppParam queryParam = new AppParam("sumUtilExtService","sumTotalBook");
			queryParam.addAttr("today", today);
			queryParam.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START+ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(queryParam);
			List<Map<String,Object>> dataList = result.getRows();
			int size = dataList.size();
			if(size>0){
				Map<String,Object> paramsMap = dataList.get(0);
				//将统计数据插入统计表
				AppParam insertParam = new AppParam("sumBookBaseService","save");
				insertParam.addAttrs(paramsMap);
				insertParam.setRmiServiceName(AppProperties
						.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
				result = RemoteInvoke.getInstance().call(insertParam);
				size =NumberUtil.getInt(result.getAttr(DuoduoConstant.DAO_Insert_SIZE), 0);
			}
			LogerUtil.log("BookSumUitl sumTotalBook >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> end insert count=" +size);
		} catch (Exception e) {
			LogerUtil.error(BookSumUtil.class,e, "BookSumUitl sumTotalBook error");
			JobUtil.addProcessExecute(processId, "统计门店经理上门数据 报错：" + e.getMessage() );
		}
	}
	
	
	/**
	 * 门店经理上门统计
	 * @param processId
	 * @param today
	 */
	public static void storeBookMonth(Object processId, String toMonth) {
		AppResult result = new AppResult();
		try {
			LogerUtil.log("BookSumUitl storeBookMonth>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> start");
			//获取统计的门店列表
			List<Map<String, Object>> orgList = OrgUtils.getIsCountOrgList();
			int size = 0;
			for(Map<String, Object> map : orgList){
				//门店ID
				int orgId = NumberUtil.getInt(map.get("orgId"),0);
				if(0 == orgId){
					continue;
				}
				//获取统计数据
				AppParam queryParam = new AppParam("sumUtilExtService","storeBookMonth");
				queryParam.addAttr("toMonth", toMonth);
				queryParam.addAttr("orgId", orgId);
				queryParam.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START+ServiceKey.Key_busi_in));
				result = RemoteInvoke.getInstance().callNoTx(queryParam);
				List<Map<String,Object>> dataList = result.getRows();
				if(dataList.size()>0){
					//将统计数据插入统计表
					AppParam insertParam = new AppParam("sumBookStoreMonthService","save");
					insertParam.addAttr("toMonth", toMonth);
					insertParam.addAttr("orgId", orgId);
					insertParam.addAttr("list", dataList);
					insertParam.setRmiServiceName(AppProperties
							.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
					result = RemoteInvoke.getInstance().call(insertParam);
					size += NumberUtil.getInt(result.getAttr(DuoduoConstant.DAO_Insert_SIZE), 0);
				}
			}
			LogerUtil.log("BookSumUitl storeBookMonth >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> end insert count=" +size);
		} catch (Exception e) {
			LogerUtil.error(BookSumUtil.class,e, "BookSumUitl storeBookMonth error");
			JobUtil.addProcessExecute(processId, "月统计门店经理上门数据 报错：" + e.getMessage() );
		}
	}
}
