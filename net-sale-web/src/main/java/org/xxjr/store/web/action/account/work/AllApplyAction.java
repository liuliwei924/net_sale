package org.xxjr.store.web.action.account.work;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.service.RemoteInvoke;
import org.ddq.common.exception.ExceptionUtil;
import org.ddq.common.util.LogerUtil;
import org.ddq.common.web.session.DuoduoSession;
import org.ddq.common.web.session.RequestUtil;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.xxjr.busi.util.kf.BorrowApplyUtils;
import org.xxjr.busi.util.kf.ExportUtil;
import org.xxjr.busi.util.kf.KfUserUtil;
import org.xxjr.busi.util.push.PushPlatformUtils;
import org.xxjr.cust.util.CustConstant;
import org.xxjr.cust.util.info.CustomerUtil;
import org.xxjr.store.web.util.KfExportParamUtil;
import org.xxjr.store.web.util.PageUtil;
import org.xxjr.sys.util.NumberUtil;
import org.xxjr.sys.util.ServiceKey;
import org.xxjr.sys.util.SysParamsUtil;
import org.xxjr.sys.util.ValidUtils;

@Controller()
@RequestMapping("/account/work/")
public class AllApplyAction {
	
	/***
	 * 查询所有列表
	 * @param request
	 * @return
	 */
	@RequestMapping("all/queryAllList")
	@ResponseBody
	public AppResult queryAllList(HttpServletRequest request){
		AppResult result = new AppResult();
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			KfExportParamUtil.getInstance().allList(params, result, request);
			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "allList error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	/***
	 * 查询所有列表
	 * @param request
	 * @return
	 */
	@RequestMapping("all/queryAllListCount")
	@ResponseBody
	public AppResult queryAllListCount(HttpServletRequest request){
		AppResult result = new AppResult();
		try {
			AppParam params = new AppParam("borrowApplyService","query2Count");
			RequestUtil.setAttr(params, request);
			String customerId = KfUserUtil.getCustomerId(request);
			
			String roleType = CustomerUtil.getRoleType(customerId);
			Map<String,Object> custRight = KfUserUtil.getUserRight(customerId);
			Object fixChannels = custRight.get("channels");
			params.addAttr("fixChannels", fixChannels);//固定渠道
			
			String applyName = request.getParameter("applyName");
			if(ValidUtils.validateTelephone(applyName)){//加快查询效率
				params.addAttr("telephone", applyName);
				params.removeAttr("applyName");
			}
			
			params.addAttr("roleType", roleType);
			params.addAttr("loginKf", customerId);
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "allList error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	
	/***
	 * 查询所有列表(原始的)
	 * @param request
	 * @return
	 */
	@RequestMapping("origAll/queryOrigList")
	@ResponseBody
	public AppResult queryOrigList(HttpServletRequest request){
		AppResult result = new AppResult();
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			KfExportParamUtil.getInstance().origAllList(params, result, request);
			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "tgAllList error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}

	
	@RequestMapping("errAll/queryErrList")
	@ResponseBody
	public AppResult queryErrList (HttpServletRequest request) {
		AppResult result = new AppResult();
		try {
			AppParam param = new AppParam("applyService", "queryByPage");
			param.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			param.setOrderBy("applyTime");
			param.setOrderValue("DESC");
			RequestUtil.setAttr(param, request);
			String applyName = request.getParameter("applyName");
			if(ValidUtils.validateTelephone(applyName)){//加快查询效率
				param.addAttr("telephone", applyName);
				param.removeAttr("applyName");
			}
			param.addAttr("status", "3");
			result = RemoteInvoke.getInstance().callNoTx(param);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "queryErrList error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	@RequestMapping("errAll/transferApplyData")
	@ResponseBody
	public AppResult transferApplyData(HttpServletRequest request){
		AppResult result = new AppResult();
		String applyIds = request.getParameter("applyIds");
		if(StringUtils.isEmpty(applyIds)){
			result.setMessage("缺少参数");
			result.setSuccess(false);
			return result;
		}
		try {
			int count = 0;
			int error = 0;
			String[] ids = applyIds.split(",");
			StringBuilder builder = new StringBuilder();
			for (String applyId : ids) {
				AppParam param = new AppParam("applyService", "handlerErrData");
				param.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
				param.addAttr("applyId", applyId);
				AppResult saveResult = RemoteInvoke.getInstance().call(param);
				if (!saveResult.isSuccess()) {
					error++;
					builder.append("申请id为:" + applyId + "失败，失败原因为" + saveResult.getMessage() + "\r");
				}
				count++;
			}
			result.setMessage("一共转移" + count + "条成功:" + (count-error) + "条,失败:" + error +"条。" + (error > 0 ? "\r" + builder.toString() : ""));
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "kfLockApply error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/***
	 * 查询所有列表(给推广用的)
	 * @param request
	 * @return
	 */
	@RequestMapping("tgAll/queryTgList")
	@ResponseBody
	public AppResult tgAllList(HttpServletRequest request){
		AppResult result = new AppResult();
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			KfExportParamUtil.getInstance().tgAllList(params, result, request);
			if (result.isSuccess()) {
				result = RemoteInvoke.getInstance().callNoTx(params);
			}
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "tgAllList error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	/***
	 * 查询所有列表(给推广用的)
	 * @param request
	 * @return
	 */
	@RequestMapping("tgAll/queryTgListCount")
	@ResponseBody
	public AppResult tgAllListCount(HttpServletRequest request){
		AppResult result = new AppResult();
		try {
			AppParam params = new AppParam("borrowApplyService","query2Count");
			RequestUtil.setAttr(params, request);
			String customerId = KfUserUtil.getCustomerId(request);
			String roleType = CustomerUtil.getRoleType(customerId);
			String applyName = request.getParameter("applyName");
			if(ValidUtils.validateTelephone(applyName)){//加快查询效率
				params.addAttr("telephone", applyName);
				params.removeAttr("applyName");
			}
			
			Map<String,Object> custRight = KfUserUtil.getUserRight(customerId);
			int spread = NumberUtil.getInt(custRight.get("spread"),0);
			Object fixChannels = custRight.get("channels");
			params.addAttr("fixChannels", fixChannels);//固定渠道
			
			//关闭未处理单的查询，系统分单后，不能让客服在所有的里面查到待处理的单子(0-忽略 1-开启),推广的可查询
			if(CustConstant.CUST_ROLETYPE_2.equals(roleType)){
				int autoAllotStatus = SysParamsUtil.getIntParamByKey("autoAllotStatus", 0);
				if(autoAllotStatus == 1 && spread == 0){
					return result;
				}
			}
			params.addAttr("roleType", roleType);
			params.addAttr("loginKf", customerId);
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "tgAllList error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/***
	 *批量导入贷上我的数据
	 * @param request
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@RequestMapping("tgAll/batchImportDswData")
	@ResponseBody
	public AppResult batchImportDswData(MultipartHttpServletRequest request){
		AppResult result = new AppResult();
		String[] keys = {"serId","applyName","telephone","loanAmount","cityName","applyTime",
				"sourceChannel","workType","houseType","carType","socialType","fundType",
				"insurType","income","pubAmount","age","sex","education","zimaScore","haveWeiLi","desc"};//每列的key
		
		try {
			
		//	String customerId = KfUserUtil.getCustomerId(request);
//			Map<String,Object> custMap = CustomerIdentify.getCustIdentify(customerId);
//			String orgId = StringUtil.getString(custMap.get("orgId"));
	/*		if(!(CustomerUtil.isAdmin(customerId))){
				result.setSuccess(false);
				result.setMessage("你没有导入数据的权限!");
				return result;
			}*/

			List<Map<String,Object>> dswDataList = ExportUtil.readExcel(keys,request);
			int errCount = 0;
			int sucCount = 0;
			StringBuilder errSb = new StringBuilder();
			if(dswDataList != null && !dswDataList.isEmpty()){
				int totalSize = dswDataList.size();
				int everySize = 50;
				int xhCount = 1;
				int xhSize = 0;
				while (xhSize < totalSize && xhCount <= 100) {// 最多循环100次
					int formIndex = (xhCount-1)*everySize ;
					int toIndex =  xhCount*everySize ;
					if(toIndex > totalSize){
						toIndex = totalSize;
					}
					if(formIndex > toIndex){
						formIndex = toIndex;
					}

					List<Map<String,Object>> subDswDataList = new ArrayList(dswDataList.subList(formIndex, toIndex));
					AppParam addParam = new AppParam();
					addParam.addAttr("dswDataList", subDswDataList);
					addParam.setService("thirdDataService");
					addParam.setMethod("batchImportDswData");
					addParam.setRmiServiceName(AppProperties
							.getProperties(DuoduoConstant.RMI_SERVICE_START
									+ ServiceKey.Key_busi_in));
					result = RemoteInvoke.getInstance().call(addParam);
					xhSize = xhSize + subDswDataList.size();
					xhCount ++;
					errCount = errCount + NumberUtil.getInt(result.getAttr("errCount"), 0);
					sucCount = sucCount + NumberUtil.getInt(result.getAttr("sucCount"), 0);
					errSb.append(result.getMessage());
				}

				String msg = "成功导入:" + sucCount + "条，导入失败:" + errCount + "条。错误行数【" +  errSb.toString() + "】";
				result.setMessage(msg);
				result.setSuccess(true);
			}else{
				result.setSuccess(false);
				result.setMessage("文件中没有数据记录!");
			}
			
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "batchImportDswData error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	
	/***
	 * 查询转化的列表
	 * @param request
	 * @return
	 */
	@RequestMapping("saleCan/queryTranferList")
	@ResponseBody
	public AppResult queryTranferList(HttpServletRequest request){
		AppResult result = new AppResult();
		try {
			String status = request.getParameter("status");
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			if("6".equals(status)){
				params.removeAttr("status");
				params.addAttr("inStatus", "6,8");
			}
			String searchType = request.getParameter("searchType");
			String customerId = KfUserUtil.getCustomerId(request);
			if("1".equals(searchType)){
				params.addAttr("lastKf", customerId);
			}

			String applyName = request.getParameter("applyName");
			if(ValidUtils.validateTelephone(applyName)){//加快查询效率
				params.addAttr("telephone", applyName);
				params.removeAttr("applyName");
			}

			String roleType = CustomerUtil.getRoleType(customerId);
			params.addAttr("roleType", roleType);
			params.addAttr("loginKf", customerId);

			Map<String,Object> custRight = KfUserUtil.getUserRight(customerId);
			Object fixChannels = custRight.get("channels");
			params.addAttr("fixChannels", fixChannels);//固定渠道
			
			PageUtil.packageQueryParam(request, params);
			
			params.setService("salePoolService");
			params.setMethod("canTransXxjrList");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));

			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "tranferList error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}

	@RequestMapping("pushAll/queryPushList")
	@ResponseBody
	public AppResult queryPushList(HttpServletRequest request){
		AppResult result = new AppResult();
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			
			String applyName = request.getParameter("applyName");
			if(ValidUtils.validateTelephone(applyName)){//加快查询效率
				params.addAttr("telephone", applyName);
				params.removeAttr("applyName");
			}
			
			PageUtil.packageQueryParam(request, params);
			
			params.setService("borrowApplyPushService");
			params.setMethod("queryPushListAll");
			params.addAttr("orderSql", "createTime");
			params.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START
					+ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(params);

		}catch(Exception e){
			LogerUtil.error(AllApplyAction.class,e, "queryPushList error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}

		return result;
	}
	
	@RequestMapping("pushAll/failDataRestore")
	@ResponseBody
	public AppResult failDataRestore(HttpServletRequest request){
		AppResult result = new AppResult();
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			
			String startTime = request.getParameter("startTime");
			String endTime = request.getParameter("endTime");
			String pushType = request.getParameter("pushType");
			String pushStatus = request.getParameter("pushStatus");
			if (StringUtils.isEmpty(startTime) || StringUtils.isEmpty(endTime) || StringUtils.isEmpty(pushType) || StringUtils.isEmpty(pushStatus)) {
				return CustomerUtil.retErrorMsg("缺少必传参数!");
			}
			
			Map<String, Object> channel = PushPlatformUtils.getConfigByCode(NumberUtil.getInt(pushType, -1));
			if (channel == null || channel.isEmpty()) {
				return CustomerUtil.retErrorMsg("输入的第三方渠道id有误!");
			}
			if (NumberUtil.getInt(channel.get("type")) == 1) {
				params.setService("insurancePushPoolService");
			}else {
				params.setService("thirdPushPoolService");
			}
			params.setMethod("failDataRestore");
			params.addAttr("orderSql", "createTime");
			params.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START
					+ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(params);

		}catch(Exception e){
			LogerUtil.error(AllApplyAction.class,e, "queryPushList error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}

		return result;
	}
	
	
	@RequestMapping("pushAll/updateStatus")
	@ResponseBody
	public AppResult updateStatus (HttpServletRequest request) {
		AppResult result = new AppResult();
		try {
			AppParam params = new AppParam("borrowApplyPushService", "updateStatus");
			params.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			RequestUtil.setAttr(params, request);
			String message = request.getParameter("message");
			if (StringUtils.isEmpty(message)) {
				return CustomerUtil.retErrorMsg("缺少必传参数");
			}
			message = message.replace("\n", ",").trim();
			params.addAttr("messageArray", Arrays.asList(message.split(",")));
			result = RemoteInvoke.getInstance().call(params);
			if (NumberUtil.getInt(result.getAttr(DuoduoConstant.DAO_Update_SIZE), 0) > 0) {
				LocalDate startTime = LocalDate.parse(request.getParameter("startTime").split(" ")[0]);
				LocalDate endTime = LocalDate.parse(request.getParameter("endTime").split(" ")[0]);
				int start = startTime.getDayOfYear();
				for (int i = start; i <= endTime.getDayOfYear(); i++) {//重新统计
					String now = startTime.toString();
					PushPlatformUtils.sumPushData(now);
					PushPlatformUtils.sumChannelPushData(now);
					startTime = startTime.minusDays(-1);
				}
			}
		} catch (Exception e) {
			LogerUtil.error(AllApplyAction.class,e, "updateStatus error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/***
	 * 查询所有列表
	 * @param request
	 * @return
	 */
	@RequestMapping("overFail/queryOverFail")
	@ResponseBody
	public AppResult queryOverFail(HttpServletRequest request){
		AppResult result = new AppResult();
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			String customerId = KfUserUtil.getCustomerId(request);

			String roleType = CustomerUtil.getRoleType(customerId);
			String searchType = request.getParameter("searchType");
			Map<String,Object> custRight = KfUserUtil.getUserRight(customerId);
			int isZSenior = NumberUtil.getInt(custRight.get("isZSenior"),0);
			int isSenior = NumberUtil.getInt(custRight.get("isSenior"),0);
			Object fixChannels = custRight.get("channels");
			params.addAttr("fixChannels", fixChannels);//固定渠道
			params.addAttr("isZSenior", isZSenior);//查询准优质单
			params.addAttr("isSenior", isSenior);//查询优质单
			
			if("1".equals(searchType)){// 我录入的
				params.addAttr("lastKf", customerId);
			}else if ("4".equals(searchType)) {
				params.addAttr("grade", "A");
			}else if ("5".equals(searchType)) {
				params.addAttr("grade", "B");
			}else if ("6".equals(searchType)) {
				params.addAttr("grade", "C");
			}else if ("7".equals(searchType)) {
				params.addAttr("grade", "D");
			}else if ("8".equals(searchType)) {
				params.addAttr("grade", "E");
			}else if ("9".equals(searchType)) {
				params.addAttr("grade", "F");
			}
			//关闭未处理单的查询，系统分单后，不能让客服在所有的里面查到待处理的单子(0-忽略 1-开启)
			if(CustConstant.CUST_ROLETYPE_2.equals(roleType)){
				params.addAttr("autoAllotStatus", SysParamsUtil.getIntParamByKey("autoAllotStatus", 0));
			}

			String applyName = request.getParameter("applyName");
			if(ValidUtils.validateTelephone(applyName)){//加快查询效率
				params.addAttr("telephone", applyName);
				params.removeAttr("applyName");
			}

			params.addAttr("roleType", roleType);
			params.addAttr("loginKf", customerId);
			
			PageUtil.packageQueryParam(request, params);
			
			result = BorrowApplyUtils.queryBorrowList(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "allList error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/***
	 * 查询第三方挂卖数据
	 * @param request
	 * @return
	 */
//	@RequestMapping("saleThird/queryByPage")
//	@ResponseBody
//	public AppResult querySaleThird(HttpServletRequest request){
//		AppResult result = new AppResult();
//		try {
//			AppParam params = new AppParam("thirdSaleDataService","queryByPage");
//			params.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi));
//			RequestUtil.setAttr(params, request);
//			params.setOrderBy("createTime");
//			params.setOrderValue("DESC");
//			result = RemoteInvoke.getInstance().callNoTx(params);
//		} catch (Exception e) {
//			LogerUtil.error(this.getClass(), e, "querySaleThird error");
//			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
//		}
//		return result;
//	}
}
