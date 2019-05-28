package org.xxjr.job.listener.busi.store;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.service.RemoteInvoke;
import org.ddq.common.util.DateUtil;
import org.ddq.common.util.LogerUtil;
import org.llw.job.util.JobUtil;
import org.xxjr.sys.util.NumberUtil;
import org.xxjr.sys.util.ServiceKey;

/***
 * 门店通知相关工具类
 * @author ZQH
 *
 */
public class StoreNotifyUtils {

	/***
	 * 删除一个月以上的个人通知
	 * @param processId
	 * @return
	 */
	public static AppResult deleteMoreOneMonthNotify(Object processId){
		AppResult result = new AppResult();
		int totalCount = 0;
		try{
			AppParam delParam = new AppParam();
			delParam.setService("sysNotifyService");
			delParam.setMethod("deleteMoreNotify");
			delParam.addAttr("messNotifyType", "2");
			delParam.addAttr("createTime", DateUtil.toStringByParttern(new Date(),DateUtil.DATE_PATTERN_YYYY_MM_DD));
			delParam.addAttr("subDay", "30");
			delParam.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START+ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().call(delParam);
			if(result.isSuccess()){
				totalCount = NumberUtil.getInt(result.getAttr(DuoduoConstant.DAO_Delete_SIZE),0);
			}
			
		}catch(Exception e){
			LogerUtil.error(StoreNotifyUtils.class, e, "deleteMoreOneMonthNotify >>>>>>>>>>>>>>>>>>error");
			JobUtil.addProcessExecute(processId, "删除一个月以上的个人消息报错：" + e.getMessage() );
		}
		LogerUtil.log("总共删除个人通知数："  + totalCount);
		return result;
	
	}
	
	/**
	 * 7天未处理的专属单 提醒门店人员处理
	 * @param processId
	 * @return
	 */
	public static AppResult exclusiveNotDeal(Object processId) {
		AppResult result = new AppResult();
		int totalSucSize = 0;
		int totalFailSize = 0;
		try{
			// 查询7天未处理的专属单
			AppParam queryParam = new AppParam("exclusiveOrderService","queryNotDeal");
			queryParam.addAttr("orgId", 236);
			queryParam.setEveryPage(50);
			queryParam.setRmiServiceName(AppProperties.getProperties(
					DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(queryParam);
			
			List<Map<String,Object>> orderList = new ArrayList<>();
			AppParam updateParam = new AppParam("storeExcluesiveNotifyService","save");
			updateParam.setRmiServiceName(AppProperties.getProperties(
					DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			
			// 只处理500笔
			if(result.isSuccess() && result.getRows().size() > 0){
				int totalPage = result.getPage().getTotalPage();
				if(totalPage > 10){
					totalPage = 10;
				}
				for(int i = 1; i<=totalPage; i++){
					// 查询7天未处理的专属单
					queryParam.setCurrentPage(i);
					queryParam.setRmiServiceName(AppProperties.getProperties(
							DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
					result = RemoteInvoke.getInstance().callNoTx(queryParam);
					if(result.isSuccess() && result.getRows().size() >0){
						orderList.addAll(result.getRows());
					}
					try{
						updateParam.addAttr("orderList", orderList);
						result  = RemoteInvoke.getInstance().call(updateParam);
						if(result.isSuccess()){
							totalSucSize = totalSucSize + NumberUtil.getInt(result.getAttr("sucSize"),0);
							totalFailSize = totalFailSize + NumberUtil.getInt(result.getAttr("failSize"),0);
						}
					}catch(Exception e){
						LogerUtil.error(StoreNotifyUtils.class,e, "exclusiveNotDeal error");
						JobUtil.addProcessExecute(processId, " excluNotDeal 报错：" + e.getMessage() );
					}
					orderList.clear();
				}
			}

		}catch(Exception e){
			LogerUtil.error(StoreNotifyUtils.class,e, "exclusiveNotDeal error");
			JobUtil.addProcessExecute(processId, " exclusiveNotDeal 报错：" + e.getMessage() );
		}
		LogerUtil.log("专属单加入通知成功总笔数:"  + totalSucSize + "失败总笔数:" + totalFailSize);
		JobUtil.addProcessExecute(processId, "专属单加入通知成功总笔数:" + totalSucSize + "失败总笔数:" + totalFailSize);
		return result;
	}
}
