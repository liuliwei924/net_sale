package org.xxjr.store.web.action.account.user;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.exception.ExceptionUtil;
import org.ddq.common.exception.SysException;
import org.ddq.common.util.DateUtil;
import org.ddq.common.util.JsonUtil;
import org.ddq.common.util.LogerUtil;
import org.ddq.common.util.StringUtil;
import org.ddq.common.web.session.DuoduoSession;
import org.ddq.common.web.session.RequestUtil;
import org.ddq.common.core.service.RemoteInvoke;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.xxjr.busi.util.kf.ExportUtil;
import org.xxjr.busi.util.store.StoreUserUtil;
import org.xxjr.sys.util.ServiceKey;
import org.xxjr.sys.util.ValidUtils;

import com.alibaba.fastjson.JSONArray;

@Controller()
@RequestMapping("/account/user/export/")
public class DataExportAction {

	/*** 
	 * 导出excel
	 * @param request
	 * @param response
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping("exportExcel")
	@ResponseBody
	public void  exportExcel(HttpServletRequest request,HttpServletResponse response){
		AppResult result = new AppResult();
		PrintWriter printWriter = null;
		OutputStream os = null;
		try{
			printWriter = response.getWriter();
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			
			if(StringUtils.isEmpty(params.getAttr("exportTitles"))){
				printWriter.print("not found params:exportTitles");
				printWriter.flush();
				return;
			}
			if(StringUtils.isEmpty(params.getAttr("exportParams"))){
				printWriter.print("not found params:exportParams");
				printWriter.flush();
				return;
			}
			String exportType = request.getParameter("exportType");
			String exportTitles = params.removeAttr("exportTitles").toString();
			String exportParams = params.removeAttr("exportParams").toString();//查询参数
			Map<String,Object> queryParams = JsonUtil.getInstance().json2Object(exportParams, Map.class);
			params.addAttrs(queryParams);
			
			LinkedHashMap<String, String> exchangeTitle = new LinkedHashMap<String, String>();
			JSONArray titleJson = JSONArray.parseArray(exportTitles);
			for (int i = 0; i < titleJson.size(); i++) {
				Map<String,String> titleMap=(Map<String,String>)titleJson.get(i);
				if(titleMap.containsKey("title")&&titleMap.containsKey("name")){
					exchangeTitle.put(titleMap.get("title"),titleMap.get("name"));
				}
			}
			
			String fileName = "";
			if("waitDeal".equals(exportType)){
				result = this.queryWaitDeal2(params,request);
				fileName = "待处理列表";
			}else if("allOrder".equals(exportType)){
				result = this.queryAllOrder(params,request);
				fileName = "所有订单列表";
			}else if("reserving".equals(exportType)){
				result = this.queryReserving(params,request);
				fileName = "预约中列表";
			}else if("visitOrder".equals(exportType)){
				result = this.visitOrder(params,request);
				fileName = "已上门列表";
			}else if("signing".equals(exportType)){
				result = this.querySigning(params,request);
				fileName = "签单列表";
			}else if("backDeal".equals(exportType)){
				result = this.querybackDeal(params,request);
				fileName = "回款处理列表";
			}else if("storeToDayExp".equals(exportType)){
				result = this.queryStoreToDay(params,request);
				fileName = "门店人员今日统计列表";
			}else if("storeDayExp".equals(exportType)){
				result = this.queryStoreDay(params,request);
				fileName = "门店人员日统计列表";
			}else if("storeMonth".equals(exportType)){
				result = this.queryStoreMonth(params,request);
				fileName = "门店人员月统计列表";
			}else if("storeOrgToDay".equals(exportType)){
				result = this.queryStoreOrgToDay(params,request);
				fileName = "门店今日统计列表";
			}else if("storeOrgDay".equals(exportType)){
				result = this.queryStoreOrgDay(params,request);
				fileName = "门店日统计列表";
			}else if("storeOrgMonth".equals(exportType)){
				result = this.queryStoreOrgMonth(params,request);
				fileName = "门店月统计列表";
			}else if("storeAllToDay".equals(exportType)){
				result = this.queryStoreAllToDay(params,request);
				fileName = "总的今日统计列表";
			}else if("storeAllDay".equals(exportType)){
				result = this.queryStoreAllDay(params,request);
				fileName = "总的日统计列表";
			}else if("storeAllMonth".equals(exportType)){
				result = this.queryStoreAllMonth(params,request);
				fileName = "总的月统计列表";
			}else if("callDay".equals(exportType)){      
				result = this.queryCallDay(params,request);
				fileName = "门店人员通话日统计列表";
			}else if("callMonth".equals(exportType)){
				result = this.queryCallMonth(params,request);
				fileName = "门店人员通话本月统计列表";
			}else if("callMonthly".equals(exportType)){
				result = this.queryCallMonthly(params,request);
				fileName = "门店人员通话月度统计列表";
			}else if("againAllot".equals(exportType)){
				result = this.queryAgainAllot(params,request);
				fileName = "再分配列表";
			}else if("foreignAllotPond".equals(exportType)){
				result = this.queryForeignAllotList(params,request);
				fileName = "对外分配列表";
			}else if("riskAllOrder".equals(exportType)){
				result = this.queryRiskAllOrder(params, request);
				fileName = "命中多头借贷列表";
			}else if("StoreCallToday".equals(exportType)){      
				result = this.queryStoreCallToday(params,request);
				fileName = "门店通话日统计列表";
			}else if("StoreCallDay".equals(exportType)){
				result = this.queryStoreCallDay(params,request);
				fileName = "门店通话本月统计列表";
			}else if("StoreCallMonth".equals(exportType)){
				result = this.queryStoreCallMonth(params,request);
				fileName = "门店通话月度统计列表";
			}else if("signEnd".equals(exportType)){
				result = this.querySigning(params,request);
				fileName = "结案列表";
			}else{
				printWriter.print("exportType not find exportType" + exportType);
				printWriter.flush();
				return;
			}
			
			fileName = fileName + "_" + DateUtil.toStringByParttern(new Date(),
        			DateUtil.DATE_PATTERNYYYYMMDDHHMMSSSSS) + ".xls";
			response.reset();// 清空输出流
			response.setHeader("Content-disposition", "attachment; filename=" + new String(fileName.getBytes(),"iso-8859-1"));
			// 设定输出文件头
			response.setContentType("application/msexcel");// 定义输出类型
			response.setCharacterEncoding("UTF-8");
			os = response.getOutputStream();
			ExportUtil.writeExcel(os, exchangeTitle, result.getRows());
			
		}catch(Exception e){
			LogerUtil.error(this.getClass(), e,"DataExportAction exportExcel error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
			printWriter.print("error exportExcel:" + e.getMessage());
			printWriter.flush();
		}finally{
			IOUtils.closeQuietly(printWriter);
			IOUtils.closeQuietly(os);
		}
	}

	/***
	 * 待处理导出
	 * @param params
	 * @return
	 */
	public AppResult queryWaitDeal(AppParam params ,HttpServletRequest request){
		//处理条件
		String customerId = StoreUserUtil.getCustomerId(request);
		StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
		
		params.setService("storeListOptExtService");
		params.setMethod("queryWaitDeal");
		params.setCurrentPage(-1);
		params.setOrderBy("allotTime");
		params.setOrderValue("desc");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
		return RemoteInvoke.getInstance().callNoTx(params);
	}
	
	/***
	 * 所有订单导出
	 * @param params
	 * @return
	 */
	public AppResult queryAllOrder(AppParam params ,HttpServletRequest request){
		//处理条件
		String customerId = StoreUserUtil.getCustomerId(request);
		StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
		String orderStatus = StringUtil.getString(params.getAttr("orderStatus"));
		if("0".equals(orderStatus)){
			params.addAttr("orderStatus","0");
		}
		params.removeAttr("custLabel");
		params.setService("storeHandleExtService");
		params.setMethod("queryAllList");
		params.setOrderBy("lastTime");
		params.setOrderValue("desc");
		params.setCurrentPage(-1);
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
		return RemoteInvoke.getInstance().callNoTx(params);
	}
	
	/***
	 * 命中多头借贷列表导出
	 * @param params
	 * @return
	 */
	public AppResult queryRiskAllOrder(AppParam params ,HttpServletRequest request){
		//处理条件
		String customerId = StoreUserUtil.getCustomerId(request);
		StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
		String custLabel = StringUtil.getString(params.getAttr("custLabel"));
		String orderStatus = StringUtil.getString(params.getAttr("orderStatus"));
		if("0".equals(custLabel)){
			params.addAttr("custLabel","0");
		}
		if("0".equals(orderStatus)){
			params.addAttr("orderStatus","0");
		}
		params.setService("storeHandleExtService");
		params.setMethod("queryRiskAllList");
		params.setOrderBy("lastTime");
		params.setOrderValue("desc");
		params.setCurrentPage(-1);
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
		return RemoteInvoke.getInstance().callNoTx(params);
	}
	
	/***
	 * 预约中导出
	 * @param params
	 * @return
	 */
	public AppResult queryReserving(AppParam params ,HttpServletRequest request){
		//处理条件
		String customerId = StoreUserUtil.getCustomerId(request);
		StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
		String custLabel = StringUtil.getString(params.getAttr("custLabel"));
		if("0".equals(custLabel)){
			params.addAttr("custLabel","0");
		}
		params.setService("storeHandleExtService");
		params.setMethod("queryBookOrderList");
		params.addAttr("orderStatusNot", 7);//不查询无效客户
		params.addAttr("bookStatus", 1);
		params.setOrderBy("bookTime");
		params.setOrderValue("desc");
		params.setCurrentPage(-1);
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
		return RemoteInvoke.getInstance().callNoTx(params);
	}
	
	/***
	 * 已上门导出
	 * @param params
	 * @return
	 */
	public AppResult visitOrder(AppParam params ,HttpServletRequest request){
		//处理条件
		String customerId = StoreUserUtil.getCustomerId(request);
		StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
		String custLabel = StringUtil.getString(params.getAttr("custLabel"));
		if("0".equals(custLabel)){
			params.addAttr("custLabel","0");
		}
		params.setService("storeHandleExtService");
		params.setMethod("queryBookOrderList");
		params.addAttr("bookStatus", 3);
		params.setOrderBy("visitTime");
		params.setOrderValue("desc");
		params.setCurrentPage(-1);
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
		return RemoteInvoke.getInstance().callNoTx(params);
	}
	
	/***
	 * 签单中导出
	 * @param params
	 * @return
	 */
	public AppResult querySigning(AppParam params ,HttpServletRequest request){
		//处理条件
		String customerId = StoreUserUtil.getCustomerId(request);
		StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
		String custLabel = StringUtil.getString(params.getAttr("custLabel"));
		if("0".equals(custLabel)){
			params.addAttr("custLabel","0");
		}
		params.setService("storeListOptExtService");
		params.setMethod("querySigned");
		params.setOrderBy("t.createTime");
		params.setOrderValue("desc");
		params.setCurrentPage(-1);
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
		return RemoteInvoke.getInstance().callNoTx(params);
	}
	/***
	 * 回款处理导出
	 * @param params
	 * @return
	 */
	public AppResult querybackDeal(AppParam params ,HttpServletRequest request){
		//处理条件
		String customerId = StoreUserUtil.getCustomerId(request);
		StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
		String custLabel = StringUtil.getString(params.getAttr("custLabel"));
		if("0".equals(custLabel)){
			params.addAttr("custLabel","0");
		}
		params.setService("storeListOptExtService");
		params.setMethod("queryReLoan");
		params.setOrderBy("feeAmountDate");
		params.setOrderValue("desc");
		params.setCurrentPage(-1);
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
		return RemoteInvoke.getInstance().callNoTx(params);
	}
	
	/***
	 * 门店人员今日统计列表导出
	 * @param params
	 * @return
	 */
	public AppResult queryStoreToDay(AppParam params ,HttpServletRequest request){
		//处理条件
		String customerId = StoreUserUtil.getCustomerId(request);
		RequestUtil.setAttr(params, request);
		StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
		params.setCurrentPage(-1);
		params.setService("sumUtilExtService");
		params.setMethod("queryStoreToDay");
		params.setOrderBy(StringUtil.getString(params.getAttr("orderBy")));
		params.setOrderValue(StringUtil.getString(params.getAttr("orderValue")));
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
		return RemoteInvoke.getInstance().callNoTx(params);
	}
	
	

	/***
	 * 门店人员日统计列表导出
	 * @param params
	 * @return
	 */
	public AppResult queryStoreDay(AppParam params ,HttpServletRequest request){
		//处理条件
		String customerId = StoreUserUtil.getCustomerId(request);
		RequestUtil.setAttr(params, request);
		StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
		params.setCurrentPage(-1);
		params.setService("sumStoreBaseService");
		params.setMethod("queryShow");
		params.setOrderBy(StringUtil.getString(params.getAttr("orderBy")));
		params.setOrderValue(StringUtil.getString(params.getAttr("orderValue")));
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
		return RemoteInvoke.getInstance().callNoTx(params);
	}
	
	
	/***
	 * 门店人员月统计列表导出
	 * @param params
	 * @return
	 */
	public AppResult queryStoreMonth(AppParam params ,HttpServletRequest request){
		//处理条件
		String customerId = StoreUserUtil.getCustomerId(request);
		RequestUtil.setAttr(params, request);
		StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
		params.setCurrentPage(-1);
		params.setService("sumStoreBaseMonthService");
		params.setMethod("queryStoreBaseMonth");
		params.setOrderBy(StringUtil.getString(params.getAttr("orderBy")));
		params.setOrderValue(StringUtil.getString(params.getAttr("orderValue")));
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
		return RemoteInvoke.getInstance().callNoTx(params);
	}
	
	/***
	 * 门店今日统计列表导出
	 * @param params
	 * @return
	 */
	public AppResult queryStoreOrgToDay(AppParam params ,HttpServletRequest request){
		//处理条件
		String customerId = StoreUserUtil.getCustomerId(request);
		RequestUtil.setAttr(params, request);
		StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
		params.setCurrentPage(-1);
		params.setService("sumUtilExtService");
		params.setMethod("queryOrgToDay");
		params.setOrderBy(StringUtil.getString(params.getAttr("orderBy")));
		params.setOrderValue(StringUtil.getString(params.getAttr("orderValue")));
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
		return RemoteInvoke.getInstance().callNoTx(params);
	}
	
	
	/***
	 * 门店日统计列表导出
	 * @param params
	 * @return
	 */
	public AppResult queryStoreOrgDay(AppParam params ,HttpServletRequest request){
		//处理条件
		String customerId = StoreUserUtil.getCustomerId(request);
		RequestUtil.setAttr(params, request);
		StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
		params.setCurrentPage(-1);
		params.setService("sumOrgBaseService");
		params.setMethod("queryByStore");
		params.setOrderBy(StringUtil.getString(params.getAttr("orderBy")));
		params.setOrderValue(StringUtil.getString(params.getAttr("orderValue")));
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
		return RemoteInvoke.getInstance().callNoTx(params);
	}
	
	
	/***
	 * 门店月统计列表导出
	 * @param params
	 * @return
	 */
	public AppResult queryStoreOrgMonth(AppParam params ,HttpServletRequest request){
		//处理条件
		String customerId = StoreUserUtil.getCustomerId(request);
		RequestUtil.setAttr(params, request);
		StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
		params.setCurrentPage(-1);
		params.setService("sumOrgBaseService");
		params.setMethod("queryStoreMonth");
		params.setOrderBy(StringUtil.getString(params.getAttr("orderBy")));
		params.setOrderValue(StringUtil.getString(params.getAttr("orderValue")));
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
		return RemoteInvoke.getInstance().callNoTx(params);
	}
	
	/***
	 * 总的今日统计列表导出
	 * @param params
	 * @return
	 */
	public AppResult queryStoreAllToDay(AppParam params ,HttpServletRequest request){
		//处理条件
		String customerId = StoreUserUtil.getCustomerId(request);
		StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
		params.setCurrentPage(-1);
		params.setService("sumUtilExtService");
		params.setMethod("queryTotalToDay");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
		return RemoteInvoke.getInstance().callNoTx(params);
	}
	
	
	/***
	 * 总的日统计列表导出
	 * @param params
	 * @return
	 */
	public AppResult queryStoreAllDay(AppParam params ,HttpServletRequest request){
		//处理条件
		String customerId = StoreUserUtil.getCustomerId(request);
		StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
		params.setCurrentPage(-1);
		params.setService("sumTotalBaseService");
		params.setMethod("queryDay");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
		return RemoteInvoke.getInstance().callNoTx(params);
	}
	
	
	/***
	 * 总的月统计列表导出
	 * @param params
	 * @return
	 */
	public AppResult queryStoreAllMonth(AppParam params ,HttpServletRequest request){
		//处理条件
		String customerId = StoreUserUtil.getCustomerId(request);
		StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
		params.setCurrentPage(-1);
		params.setService("sumTotalBaseService");
		params.setMethod("queryMonth");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
		return RemoteInvoke.getInstance().callNoTx(params);
	}
	
	
	/***
	 * 门店人员通话日统计列表导出
	 * @param params
	 * @return
	 */
	public AppResult queryCallDay(AppParam params ,HttpServletRequest request){
		RequestUtil.setAttr(params, request);
		//处理条件
		String customerId = StoreUserUtil.getCustomerId(request);
		StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
		params.addAttr("recordDate", DateUtil.toStringByParttern(new Date(),
				DateUtil.DATE_PATTERN_YYYY_MM_DD));
		params.addAttr("realFlag", 1);
		params.setCurrentPage(-1);
		String realName = StringUtil.getString(params.getAttr("realName"));
		if(ValidUtils.validateTelephone(realName)){ //验证是否是手机号
			params.addAttr("telephone", params.removeAttr(realName));
		}
		params.setService("storeCallRecordService");
		params.setMethod("sumStoreCallByPage");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
		return RemoteInvoke.getInstance().callNoTx(params);
	}
	
	
	
	
	/***
	 * 门店人员通话本月统计列表导出
	 * @param params
	 * @return
	 */
	public AppResult queryCallMonth(AppParam params ,HttpServletRequest request){
		//处理条件
		String customerId = StoreUserUtil.getCustomerId(request);
		StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
		params.setCurrentPage(-1);
		params.setService("sumStoreCallService");
		params.setMethod("queryShow");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
		return RemoteInvoke.getInstance().callNoTx(params);
	}
	
	
	/***
	 * 门店人员通话月度统计列表导出
	 * @param params
	 * @return
	 */
	public AppResult queryCallMonthly(AppParam params ,HttpServletRequest request){
		//处理条件
		String customerId = StoreUserUtil.getCustomerId(request);
		StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
		params.setCurrentPage(-1);
		params.setService("sumStoreCallService");
		params.setMethod("queryMonth");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
		return RemoteInvoke.getInstance().callNoTx(params);
	}
	
	/***
	 * 新申请（未跟进）导出
	 * @param params
	 * @return
	 */
	public AppResult queryWaitDeal2(AppParam params ,HttpServletRequest request){
		//处理条件
		String customerId = StoreUserUtil.getCustomerId(request);
		if (StringUtils.isEmpty(customerId)) {
			throw new SysException("用户ID不能为空");
		}
		params.setService("storeHandleExtService");
		params.setMethod("queryAllList");
		params.addAttr("orderStatus", "-1");
		params.addAttr("orderType", "1");//新单
		RequestUtil.setAttr(params, request);
		StoreUserUtil.dealUserAuthParam(params, customerId, "lastStore");
		String searchKey = request.getParameter("searchKey");
		if(!StringUtils.isEmpty(searchKey)){
			if(ValidUtils.validateTelephone(searchKey)){//加快查询效率
				params.addAttr("telephone", searchKey);
				params.removeAttr("searchKey");
			}else{
				params.addAttr("applyName", searchKey);
				params.removeAttr("searchKey");
			}
		}
		String storeSearchKey = request.getParameter("storeSearchKey");
		if(!StringUtils.isEmpty(storeSearchKey)){
			if(ValidUtils.validateTelephone(storeSearchKey)){//加快查询效率
				params.addAttr("mobile", storeSearchKey);
				params.removeAttr("storeSearchKey");
			}else{
				params.addAttr("realName", storeSearchKey);
				params.removeAttr("storeSearchKey");
			}
		}
		params.setOrderBy("lastTime");
		params.setOrderValue("desc");
		params.setCurrentPage(-1);
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
		return RemoteInvoke.getInstance().callNoTx(params);
	}
	
	
	/***
	 * 再分配列表导出
	 * @param params
	 * @return
	 */
	public AppResult queryAgainAllot(AppParam params ,HttpServletRequest request){
		//处理条件
		String customerId = StoreUserUtil.getCustomerId(request);
		StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
		params.addAttr("orderType", 2);
		params.setService("storeHandleExtService");
		params.setMethod("queryAllList");
		params.setOrderBy("lastTime");
		params.setOrderValue("desc");
		params.setCurrentPage(-1);
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
		return RemoteInvoke.getInstance().callNoTx(params);
	}
	
	
	/***
	 * 对外分配列表导出
	 * @param params
	 * @return
	 */
	public AppResult queryForeignAllotList(AppParam params ,HttpServletRequest request){
		params.setService("thirdExportPoolService");
		params.setMethod("queryForeignAllotList");
		params.setOrderBy("createTime");
		params.setOrderValue("desc");
		params.setCurrentPage(-1);
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
		return RemoteInvoke.getInstance().callNoTx(params);
	}

	
	/***
	 * 门店通话日统计列表导出
	 * @param params
	 * @return
	 */
	public AppResult queryStoreCallToday(AppParam params ,HttpServletRequest request){
		RequestUtil.setAttr(params, request);
		//处理条件
		String customerId = StoreUserUtil.getCustomerId(request);
		StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
		params.addAttr("recordDate", DateUtil.toStringByParttern(new Date(),
				DateUtil.DATE_PATTERN_YYYY_MM_DD));
		params.addAttr("realFlag", 1);
		params.setCurrentPage(-1);
		params.setService("storeCallRecordService");
		params.setMethod("sumOrgCallByPage");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
		return RemoteInvoke.getInstance().callNoTx(params);
	}
	
	
	
	
	/***
	 * 门店通话本月统计列表导出
	 * @param params
	 * @return
	 */
	public AppResult queryStoreCallDay(AppParam params ,HttpServletRequest request){
		//处理条件
		String customerId = StoreUserUtil.getCustomerId(request);
		StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
		params.setCurrentPage(-1);
		params.setService("sumStoreCallService");
		params.setMethod("queryStoreCallDay");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
		return RemoteInvoke.getInstance().callNoTx(params);
	}
	
	
	/***
	 * 门店通话月度统计列表导出
	 * @param params
	 * @return
	 */
	public AppResult queryStoreCallMonth(AppParam params ,HttpServletRequest request){
		//处理条件
		String customerId = StoreUserUtil.getCustomerId(request);
		StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
		params.setCurrentPage(-1);
		params.setService("sumStoreCallService");
		params.setMethod("queryStoreCallMonth");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
		return RemoteInvoke.getInstance().callNoTx(params);
	}
	
	
}
