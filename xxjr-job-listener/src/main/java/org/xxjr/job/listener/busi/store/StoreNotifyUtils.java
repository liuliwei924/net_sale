package org.xxjr.job.listener.busi.store;

import java.util.Date;
import java.util.Map;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.service.RemoteInvoke;
import org.ddq.common.util.DateUtil;
import org.ddq.common.util.LogerUtil;
import org.ddq.common.util.StringUtil;
import org.llw.job.util.JobUtil;
import org.xxjr.busi.util.store.CFSUtil;
import org.xxjr.sys.util.NumberUtil;
import org.xxjr.sys.util.ServiceKey;
import org.xxjr.sys.util.SysParamsUtil;

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
	
	/***
	 * 批量添加访客记录上传到CFS
	 * @param processId
	 * @return
	 */
	public static AppResult batchVisitUpLoadToCFS(Object processId){
		AppResult result = new AppResult();
		int autoAddVisitToCFSFlag = SysParamsUtil.getIntParamByKey("autoAddVisitToCFSFlag", 1);
		if(autoAddVisitToCFSFlag == 0){
			result.setMessage("自动添加访客记录上传到CFS暂未开启!");
			result.setSuccess(false);
			return result;
		}
		// 批量添加访客登记上传到CFS
		batchAddVisitToCFS(processId);
		// 批量添加手动添加上门记录上传到CFS
		batchHandleVisitToCFS(processId);
		return result;
	}
	
	/***
	 * 批量添加访客登记上传到CFS
	 * @param processId
	 * @return
	 */
	public static void batchAddVisitToCFS(Object processId){
		try{
			AppParam queryParam = new AppParam("custVisitService","queryVisitByPage");
			//查询未上传
			queryParam.addAttr("upStatus", "1"); 
			queryParam.setOrderValue("desc");
			queryParam.setOrderBy("t.createTime");
			queryParam.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START
					+ ServiceKey.Key_busi_in));
			queryParam.setEveryPage(50);
			AppResult queryResult = RemoteInvoke.getInstance().callNoTx(queryParam);
			int currentPage = 1;
			int successCount = 0;
			int failCount = 0;
			while(queryResult.getRows().size() > 0 ){
				for(Map<String, Object> queryMap : queryResult.getRows()){
					AppParam addparams = new AppParam();
					addparams.addAttr("orgNo", StringUtil.getString(queryMap.get("orgNo")));
					addparams.addAttr("custName", StringUtil.getString(queryMap.get("custName")));
					addparams.addAttr("custTel", StringUtil.getString(queryMap.get("custTel")));
					addparams.addAttr("receiverTel", StringUtil.getString(queryMap.get("receiverTel")));
					addparams.addAttr("realName", StringUtil.getString(queryMap.get("realName")));
					addparams.addAttr("loanType", "0");
					addparams.addAttr("visitTime", StringUtil.getString(queryMap.get("createTime")));
					addparams.addAttr("recordId", StringUtil.getString(queryMap.get("recordId")));
					addparams.addAttr("employeeNo", StringUtil.getString(queryMap.get("employeeNo")));
					Map<String, Object> resultMap = CFSUtil.addVisitToCFS(addparams);
					String messageCode = StringUtil.getString(resultMap.get("MessageCode"));
					if ("200".equals(messageCode)) {
						successCount ++;
					}else{
						failCount ++;
					}
				}
				currentPage ++;
				queryParam.setCurrentPage(currentPage);
				queryResult = RemoteInvoke.getInstance().callNoTx(queryParam);
			}
			JobUtil.addProcessExecute(processId, " 批量添加访客记录上传到CFS msg：成功笔数:"+ successCount +"，失败笔数：" + failCount);
			LogerUtil.log("批量添加访客记录上传到CFS msg：成功笔数:"+ successCount +"，失败笔数：" + failCount);
		}catch(Exception e){
			LogerUtil.error(StoreNotifyUtils.class, e, "batchAddVisitToCFS >>>>>>>>>>>>>>>>>> error");
			JobUtil.addProcessExecute(processId, "批量添加访客记录上传到CFS报错：" + e.getMessage() );
		}
	}
	
	/***
	 * 批量添加手动添加上门记录上传到CFS
	 * @param processId
	 * @return
	 */
	public static void batchHandleVisitToCFS(Object processId){
		try{
			AppParam queryParam = new AppParam("treatVisitDetailService","queryHandleVisitByPage");
			// 1-未上传
			queryParam.addAttr("upStatus", "1");
			// 1-手动添加
			queryParam.addAttr("visitType", "1");
			queryParam.setOrderValue("desc");
			queryParam.setOrderBy("t.createTime");
			queryParam.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START
					+ ServiceKey.Key_busi_in));
			queryParam.setEveryPage(50);
			AppResult queryResult = RemoteInvoke.getInstance().callNoTx(queryParam);
			int currentPage = 1;
			int successCount = 0;
			int failCount = 0;
			while(queryResult.getRows().size() > 0 ){
				for(Map<String, Object> queryMap : queryResult.getRows()){
					AppParam addparams = new AppParam();
					addparams.addAttr("orgNo", StringUtil.getString(queryMap.get("orgNo")));
					addparams.addAttr("custName", StringUtil.getString(queryMap.get("applyName")));
					addparams.addAttr("custTel", StringUtil.getString(queryMap.get("telephone")));
					addparams.addAttr("receiverTel", StringUtil.getString(queryMap.get("receiverTel")));
					addparams.addAttr("realName", StringUtil.getString(queryMap.get("realName")));
					addparams.addAttr("loanType", "0");
					addparams.addAttr("visitTime", StringUtil.getString(queryMap.get("visitTime")));
					addparams.addAttr("detailId", StringUtil.getString(queryMap.get("detailId")));
					addparams.addAttr("employeeNo", StringUtil.getString(queryMap.get("employeeNo")));
					Map<String, Object> resultMap = CFSUtil.addVisitToCFS(addparams);
					String messageCode = StringUtil.getString(resultMap.get("MessageCode"));
					if ("200".equals(messageCode)) {
						successCount ++;
					}else{
						failCount ++;
					}
				}
				currentPage ++;
				queryParam.setCurrentPage(currentPage);
				queryResult = RemoteInvoke.getInstance().callNoTx(queryParam);
			}
			JobUtil.addProcessExecute(processId, " 批量添加手动添加上门记录上传到CFS msg：成功笔数:"+ successCount +"，失败笔数：" + failCount);
			LogerUtil.log("批量添加手动添加上门记录上传到CFS msg：成功笔数:"+ successCount +"，失败笔数：" + failCount);
		}catch(Exception e){
			LogerUtil.error(StoreNotifyUtils.class, e, "batchHandleVisitToCFS >>>>>>>>>>>>>>>>>> error");
			JobUtil.addProcessExecute(processId, "批量添加手动添加上门记录上传到CFS报错：" + e.getMessage() );
		}
	}
}
