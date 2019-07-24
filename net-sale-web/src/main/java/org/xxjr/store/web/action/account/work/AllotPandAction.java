package org.xxjr.store.web.action.account.work;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.service.RemoteInvoke;
import org.ddq.common.exception.ExceptionUtil;
import org.ddq.common.util.JsonUtil;
import org.ddq.common.util.LogerUtil;
import org.ddq.common.util.StringUtil;
import org.ddq.common.web.session.DuoduoSession;
import org.ddq.common.web.session.RequestUtil;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.xxjr.busi.util.kf.ExportUtil;
import org.xxjr.busi.util.store.StoreUserUtil;
import org.xxjr.cust.util.CustConstant;
import org.xxjr.cust.util.info.CustomerIdentify;
import org.xxjr.cust.util.info.CustomerUtil;
import org.xxjr.store.web.action.BaseController;
import org.xxjr.sys.util.NumberUtil;
import org.xxjr.sys.util.ServiceKey;
import org.xxjr.sys.util.SysParamsUtil;
import org.xxjr.sys.util.ValidUtils;

/**
 * 再分配池
 * @author zenghw
 *
 */
@RestController()
@RequestMapping("/account/work/againAllotPond/")
public class AllotPandAction extends BaseController{
	/**
	 *  查询网销分配池列表
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("queryAllotPond")
	public AppResult queryAllotPond() {
		AppResult result = new AppResult();
		try {
			String customerId = StoreUserUtil.getCustomerId(request);
			AppParam param = new AppParam("storeListOptExtService", "queryAllotPond");
			RequestUtil.setAttr(param, request);
			//获取用户信息
			Map<String, Object> custInfo = CustomerIdentify.getCustIdentify(customerId);
			if(custInfo != null){
				String authType =   StringUtil.getString(custInfo.get("roleType"));
				String orgId = StringUtil.getString(custInfo.get("orgId"));
				if(!CustConstant.CUST_ROLETYPE_1.equals(authType) && !CustConstant.CUST_ROLETYPE_6.equals(authType)
						&& !CustConstant.CUST_ROLETYPE_7.equals(authType)){
					result.setSuccess(false);
					result.setMessage("没有分单权限");
					return result;
				}else if(CustConstant.CUST_ROLETYPE_6.equals(authType) || CustConstant.CUST_ROLETYPE_7.equals(authType)){
					param.addAttr("orgId", orgId);
				}
			}
			//开启渠道类型标识
			int openStoreChannelFlag = SysParamsUtil.getIntParamByKey("openStoreChannelFlag", 0);
			if(openStoreChannelFlag == 1){
				//设置查询渠道类型
				String queryChannelType = SysParamsUtil.getStringParamByKey("storeQueryChannelType", "2");
				param.addAttr("queryChannelType", queryChannelType);
			}
			String storeRealName = request.getParameter("storeRealName");
			if(ValidUtils.validateTelephone(storeRealName)){//加快查询效率
				param.addAttr("storeTelephone", param.removeAttr("storeRealName"));
			}
			param.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(param);
			return result;
		} catch (Exception e) {
			LogerUtil.error(AllotPandAction.class, e, "queryAllotPond error");
			ExceptionUtil.setExceptionMessage(e, result,
					DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 导入数据
	 * @param request
	 * @return
	 */
	@RequestMapping("importInfo")
	public AppResult importInfo(HttpServletRequest request,@RequestParam(name="file") MultipartFile fileR){
		AppResult result = new AppResult();
		String[] keys = {"id", "applyName", "sex","telephone","cityName",
						  "loanAmount","workType","houseType","carType",
						   "socialType","fundType","haveWeiLi","creditType",
						   "insurType", "wagesType","income", "desc"};// 每列的key
		try{
			String customerId = StoreUserUtil.getCustomerId(request);
			if (StringUtils.isEmpty(customerId)) {
				result.setSuccess(false);
				result.setMessage("用户ID不能为空");
				return result;
			}
			
			Map<String, Object> custInfo = CustomerIdentify.getCustIdentify(customerId);
			String authType =   StringUtil.getString(custInfo.get("roleType"));
			//门店负责人和系统管理员才可以导数据
			if(!(CustConstant.CUST_ROLETYPE_6.equals(authType)
					||CustConstant.CUST_ROLETYPE_1.equals(authType))){
				
				result.setSuccess(false);
				result.setMessage("没导数据的权限");
				return result;
			}
			
			Object orgId = null;
			if(CustConstant.CUST_ROLETYPE_6.equals(authType)) 
				orgId = custInfo.get("orgId");
			
			List<Map<String, Object>> dataList = ExportUtil.readExcel(keys,fileR);
			String fileName = fileR.getOriginalFilename();
			String lastStore = null;
			if(StringUtils.hasText(fileName) && fileName.indexOf("_") == 11) {
				String storeTel = fileName.substring(0, fileName.indexOf("_"));
				AppParam storeParam = new AppParam();
				storeParam.addAttr("telephone", storeTel);
				Map<String,Object> storeMap = CustomerUtil.queryCustInfo(storeParam);
				if(storeMap != null && !storeMap.isEmpty()) {
					lastStore = StringUtil.getString(storeMap.get("customerId"));
					if(orgId == null) orgId = StringUtil.getString(storeMap.get("orgId"));
				}
			}
			int errCount = 0;
			int sucCount = 0;
			StringBuilder errSb = new StringBuilder();
			if(dataList != null && !dataList.isEmpty()) {
				int totalSize = dataList.size();
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
					List<Map<String,Object>> subDataList = new ArrayList<Map<String, Object>>(dataList.subList(formIndex, toIndex));
					AppParam addParam = new AppParam();
					addParam.addAttr("orgId", orgId);
					addParam.addAttr("lastStore", lastStore);
					addParam.addAttr("dataList", subDataList);
					addParam.setService("storeHandleExtService");
					addParam.setMethod("batchImportData");
					addParam.setRmiServiceName(AppProperties
							.getProperties(DuoduoConstant.RMI_SERVICE_START
									+ ServiceKey.Key_busi_in));
					result = RemoteInvoke.getInstance().call(addParam);
					xhSize = xhSize + subDataList.size();
					xhCount ++;
					errCount = errCount + NumberUtil.getInt(result.getAttr("errCount"), 0);
					sucCount = sucCount + NumberUtil.getInt(result.getAttr("sucCount"), 0);
					errSb.append(result.getMessage());
				}
				String msg = "成功导入:" + sucCount + "条，导入失败:" + errCount + "条。错误行数【" +  errSb.toString() + "】";
				result.setMessage(msg);
				result.setSuccess(true);
			}else {
				result.setSuccess(false);
				result.setMessage("文件中没有数据记录!");
			}
		}catch(Exception e){
			LogerUtil.error(AllotPandAction.class, e, "importInfo error");
			ExceptionUtil.setExceptionMessage(e, result,
					DuoduoSession.getShowLog());
		}
		return result;
	}
	
	
	/**
	 * 转其他信贷经理
	 * 
	 * @param request
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping("checkTransOtherXDJL")
	public AppResult checkTransOtherXDJL() {
		AppResult result = new AppResult();
		try {
			Map<String, Object> orderInfo = JsonUtil.getInstance().json2Object(
					request.getParameter("str"), Map.class);
			if (StringUtils.isEmpty(orderInfo)) {
				return CustomerUtil.retErrorMsg("订单的基本信息不能为空");
			}
			List<Map<String, Object>> orders = (List<Map<String, Object>>) orderInfo
					.get("orders");
			if (StringUtils.isEmpty(orders)) {
				return CustomerUtil.retErrorMsg("请传入订单的基本信息");
			}
			String custId = StoreUserUtil.getCustomerId(request);
			if (StringUtils.isEmpty(custId)) {
				return CustomerUtil.retErrorMsg("用户ID不能为空");
			}
			String orgId = request.getParameter("orgId");
			if (StringUtils.isEmpty(orgId)) {
				return CustomerUtil.retErrorMsg("门店不能为空");
			}
			String customerId = request.getParameter("customerId");
			if (StringUtils.isEmpty(customerId)) {
				return CustomerUtil.retErrorMsg("门店人员不能为空");
			}
			
			List<Map<String, Object>> newOrderList = new ArrayList<Map<String,Object>>();
			List<Map<String, Object>> oldOrderList = new ArrayList<Map<String,Object>>();
			// 新单分配成功数
			int sucSize = 0;
			int againSucSize = 0;
			String newMsg = "";//新单返回信息
			String againMsg = "";//再分配返回信息
			// 输出信息
			StringBuffer strBuffer = new StringBuffer();
			// 区分新单或再分配单
			for (Map<String, Object> orderMap : orders) {
				String applyId = StringUtil.objectToStr(orderMap.get("applyId"));
				AppParam queryParam = new AppParam();
				queryParam.setService("netStorePoolService");
				queryParam.setMethod("query");
				queryParam.addAttr("applyId",applyId);
				queryParam.setRmiServiceName(AppProperties
						.getProperties(DuoduoConstant.RMI_SERVICE_START
								+ ServiceKey.Key_busi_in));
				result = RemoteInvoke.getInstance().callNoTx(queryParam);
				if(result.isSuccess() && result.getRows().size()>0){
					String orderType = StringUtil.getString(result.getRow(0).get("orderType"));
					if("1".equals(orderType)){//新单
						newOrderList.add(orderMap);
					}else{
						oldOrderList.add(orderMap);
					}
				}
			}
					
			// 新单手工分单
			if(newOrderList.size() > 0){
				AppParam applyParam = new AppParam();
				applyParam.addAttr("orders", newOrderList);
				applyParam.addAttr("orgId", orgId);
				applyParam.addAttr("custId", custId);
				applyParam.addAttr("customerId", customerId);
				applyParam.setService("storeOptExtService");
				applyParam.setMethod("newOrderAllot");
				applyParam.setRmiServiceName(AppProperties
						.getProperties(DuoduoConstant.RMI_SERVICE_START
								+ ServiceKey.Key_busi_in));
				AppResult newResult = RemoteInvoke.getInstance().call(applyParam);
				if(newResult.isSuccess()){
					sucSize = NumberUtil.getInt(newResult.getAttr("sucSize"));
				}
				newMsg = newResult.getMessage() != null ? newResult.getMessage(): "";
			}
			
			// 再分配手工分单
			if (oldOrderList.size() > 0){
				AppParam applyParam = new AppParam();
				applyParam.addAttr("orders", oldOrderList);
				applyParam.addAttr("orgId", orgId);
				applyParam.addAttr("custId", custId);
				applyParam.addAttr("customerId", customerId);
				applyParam.setService("storeOptExtService");
				applyParam.setMethod("transOtherXDJL");
				applyParam.setRmiServiceName(AppProperties
						.getProperties(DuoduoConstant.RMI_SERVICE_START
								+ ServiceKey.Key_busi_in));
				result = RemoteInvoke.getInstance().call(applyParam);
				if(result.isSuccess()){
					againSucSize = NumberUtil.getInt(result.getAttr("againSucSize"));
				}
				againMsg = result.getMessage() != null ? result.getMessage(): "";
			}
			strBuffer.append("总共分单:").append(orders.size()).append("笔,成功分配新单:").append(sucSize).append("笔,成功再分配单:")
			.append(againSucSize).append("笔").append(" ").append(newMsg).append(" ").append(againMsg);
			if(!StringUtils.isEmpty(result.getAttr("failDesc"))){
				strBuffer.append(",").append(result.getAttr("failDesc"));
			}
			result.setMessage(strBuffer.toString());
			 
		} catch (Exception e) {
			LogerUtil.error(AllotPandAction.class, e, "checkTransOtherXDJL error");
			ExceptionUtil.setExceptionMessage(e, result,
					DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 新单转门店
	 * 
	 * @param request
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping("checkTransOrg")
	public AppResult checkTransOrg() {
		AppResult result = new AppResult();
		try {
			Map<String, Object> orderInfo = JsonUtil.getInstance().json2Object(
					request.getParameter("str"), Map.class);
			if (StringUtils.isEmpty(orderInfo)) {
				return CustomerUtil.retErrorMsg("订单的基本信息不能为空");
			}
			List<Map<String, Object>> orders = (List<Map<String, Object>>) orderInfo
					.get("orders");
			if (StringUtils.isEmpty(orders)) {
				return CustomerUtil.retErrorMsg("请传入订单的基本信息");
			}
			String custId = StoreUserUtil.getCustomerId(request);
			if (StringUtils.isEmpty(custId)) {
				return CustomerUtil.retErrorMsg("用户ID不能为空");
			}
			String orgId = request.getParameter("orgId");
			if (StringUtils.isEmpty(orgId)) {
				return CustomerUtil.retErrorMsg("门店不能为空");
			}
			String roleType = StoreUserUtil.getCustomerRole(custId);
			if(!CustConstant.CUST_ROLETYPE_1.equals(roleType)){
				return CustomerUtil.retErrorMsg("抱歉您暂时没有权限转门店");
			}
			for (Map<String, Object> orderMap : orders) {
				String applyId = StringUtil.objectToStr(orderMap.get("applyId"));
				AppParam queryParam = new AppParam();
				queryParam.setService("netStorePoolService");
				queryParam.setMethod("query");
				queryParam.addAttr("applyId",applyId);
				queryParam.setRmiServiceName(AppProperties
						.getProperties(DuoduoConstant.RMI_SERVICE_START
								+ ServiceKey.Key_busi_in));
				result = RemoteInvoke.getInstance().callNoTx(queryParam);
				if(result.isSuccess() && result.getRows().size() > 0){
					String orderType = StringUtil.getString(result.getRow(0).get("orderType"));
					if("2".equals(orderType)){
						return CustomerUtil.retErrorMsg("抱歉，再分配订单暂时不能转门店");
					}
				}
			}
			AppParam applyParam = new AppParam();
			applyParam.addAttr("orders", orders);
			applyParam.addAttr("orgId", orgId);
			applyParam.addAttr("custId", custId);
			applyParam.setService("storeOptExtService");
			applyParam.setMethod("newOrderTransOrg");
			applyParam.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			AppResult newResult = RemoteInvoke.getInstance().call(applyParam);
			// 新单分配成功数
			int sucSize = 0;
			if(newResult.isSuccess()){
				sucSize = NumberUtil.getInt(newResult.getAttr("sucSize"));
			}
			// 输出信息
			StringBuffer strBuffer = new StringBuffer();
			strBuffer.append("总共订单:").append(orders.size()).append("笔,成功转门店:")
			.append(sucSize).append("笔");
			result.setMessage(strBuffer.toString());
		} catch (Exception e) {
			LogerUtil.error(AllotPandAction.class, e, "checkTransOrg error");
			ExceptionUtil.setExceptionMessage(e, result,
					DuoduoSession.getShowLog());
		}
		return result;
	}
}
