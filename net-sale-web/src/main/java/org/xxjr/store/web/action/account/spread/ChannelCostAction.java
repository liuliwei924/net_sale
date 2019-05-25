package org.xxjr.store.web.action.account.spread;

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
import org.ddq.common.web.session.DuoduoSession;
import org.ddq.common.web.session.RequestUtil;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.xxjr.busi.util.kf.ExportUtil;
import org.xxjr.cust.util.info.CustomerUtil;
import org.xxjr.sys.util.ServiceKey;

@Controller
@RequestMapping("/account/spread/cost/")
public class ChannelCostAction {
	/***
	 * 查询渠道花费列表
	 * @param request
	 * @return
	 */
	@RequestMapping("queryList")
	@ResponseBody
	public AppResult queryList(HttpServletRequest request){
		AppResult result = new AppResult();
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			params.setService("borrowChannelCostService");
			params.setMethod("queryByPage");
			params.setOrderBy("recordDate");
			params.setOrderValue("desc");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().call(params);	
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "queryCostList error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/***
	 * 批量查询渠道花费
	 * @param request
	 * @return
	 */
	@RequestMapping("queryBatch")
	@ResponseBody
	public AppResult queryBatch(HttpServletRequest request){
		AppResult result = new AppResult();
		try {
			String channelCode = request.getParameter("channelCode");
			String dateMonth = request.getParameter("dateMonth");
			if(StringUtils.isEmpty(channelCode) || StringUtils.isEmpty(dateMonth)){
				return CustomerUtil.retErrorMsg("缺少必传参数!");
			}
			AppParam params = new AppParam();
			params.addAttr("channelCode", channelCode);
			params.addAttr("dateMonth", dateMonth);
			params.setService("borrowChannelCostService");
			params.setMethod("query");
			params.setOrderBy("recordDate");
			params.setOrderValue("asc");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().call(params);	
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "queryBatch error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/***
	 * 添加渠道花费信息
	 * @param request
	 * @return
	 */
	@RequestMapping("add")
	@ResponseBody
	public AppResult add(HttpServletRequest request){
		AppResult result = new AppResult();
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			params.setService("borrowChannelCostService");
			params.setMethod("insert");
			Map<String,Object> custInfo = DuoduoSession.getUser().getSessionData();
			params.addAttr("createBy", custInfo.get("realName"));
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().call(params);	
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "add channel cost error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/***
	 * 修改渠道花费信息
	 * @param request
	 * @return
	 */
	@RequestMapping("edit")
	@ResponseBody
	public AppResult edit(HttpServletRequest request){
		AppResult result = new AppResult();
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			params.setService("borrowChannelCostService");
			params.setMethod("update");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().call(params);	
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "edit channel cost error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/***
	 * 批量修改渠道花费信息
	 * @param request
	 * @return
	 */
	@RequestMapping("batchEdit")
	@ResponseBody
	public AppResult batchEdit(HttpServletRequest request){
		AppResult result = new AppResult();
		try {
			AppParam params = new AppParam();
			String channelCostListStr = request.getParameter("channelCostList");
			if(StringUtils.isEmpty(channelCostListStr)){
				return CustomerUtil.retErrorMsg("缺少必传参数!");
			}
			@SuppressWarnings("unchecked")
			List<Map<String,Object>> channelCostList = JsonUtil.getInstance().json2Object(channelCostListStr, List.class);
			params.addAttr("channelCostList", channelCostList);
			params.setService("borrowChannelCostService");
			params.setMethod("batchEdit");
			Map<String,Object> custInfo = DuoduoSession.getUser().getSessionData();
			params.addAttr("createBy", custInfo.get("realName"));
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().call(params);	
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "edit batchEdit cost error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/***
	 * 更新花费，点击量
	 * @param request
	 * @return
	 */
	@RequestMapping("updateCostAmt")
	@ResponseBody
	public AppResult updateCostAmt(HttpServletRequest request){
		AppResult result = new AppResult();
		try {
			String optDate = request.getParameter("optDate");
			if(StringUtils.isEmpty(optDate)){
				result.setSuccess(false);
				result.setMessage("请输入操作日期!");
				return result;
			}
			
			//查询总的花费
			AppParam queryTotal = new AppParam("sumUtilExtService","queryTotalCost");
			queryTotal.addAttr("recordDate", optDate);
			queryTotal.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START+ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(queryTotal);
			List<Map<String,Object>> dataList = result.getRows();
			if(dataList.size()>0){
				//更新总的花费
				AppParam updateTotalCost = new AppParam("sumTotalBaseService","updateTotalCost");
				updateTotalCost.addAttr("recordDate", optDate);
				updateTotalCost.addAttr("costAmount", dataList.get(0).get("costAmount"));
				updateTotalCost.addAttr("browseCount", dataList.get(0).get("browseCount"));
				updateTotalCost.setRmiServiceName(AppProperties
						.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
				result = RemoteInvoke.getInstance().call(updateTotalCost);
				
				AppParam updateTimeCost = new AppParam("sumTotalTimeService","updateTotalCost");
				updateTimeCost.addAttr("recordDate", optDate);
				updateTimeCost.addAttr("costAmount", dataList.get(0).get("costAmount"));
				updateTimeCost.addAttr("browseCount", dataList.get(0).get("browseCount"));
				updateTimeCost.setRmiServiceName(AppProperties
						.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
				result = RemoteInvoke.getInstance().call(updateTimeCost);
			}
			
			//查询渠道花费
			AppParam queryChannel = new AppParam("sumUtilExtService","queryChannelCost");
			queryChannel.addAttr("recordDate", optDate);
			queryChannel.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START+ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(queryChannel);
			List<Map<String,Object>> channelCostList = result.getRows();
			if(dataList.size()>0){
				//更新渠道花费
				AppParam updateChannelCost = new AppParam("sumChannelBaseService","updateChannelCost");
				updateChannelCost.addAttr("list", channelCostList);
				updateChannelCost.setRmiServiceName(AppProperties
						.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
				result = RemoteInvoke.getInstance().call(updateChannelCost);
				
				AppParam updateTimeCost = new AppParam("sumChannelTimeService","updateChannelCost");
				updateTimeCost.addAttr("list", channelCostList);
				updateTimeCost.setRmiServiceName(AppProperties
						.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
				result = RemoteInvoke.getInstance().call(updateTimeCost);
			}
			
			//查询各个团队的花费
			AppParam queryTeam = new AppParam("sumTeamExtService","queryTeamCost");
			queryTeam.addAttr("recordDate", optDate);
			queryTeam.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START+ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(queryTeam);
			List<Map<String,Object>> teamList = result.getRows();
			if(teamList.size()>0){
				//更新总的花费
				AppParam updateTotalCost = new AppParam("sumTotalTeamService","updateTeamCost");
				updateTotalCost.addAttr("dataList", teamList);
				updateTotalCost.setRmiServiceName(AppProperties
						.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
				result = RemoteInvoke.getInstance().call(updateTotalCost);
			}
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "updateCostAmt channel cost error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/***
	 * 删除渠道花费信息
	 * @param request
	 * @return
	 */
	@RequestMapping("del")
	@ResponseBody
	public AppResult del(HttpServletRequest request){
		AppResult result = new AppResult();
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			params.setService("borrowChannelCostService");
			params.setMethod("delete");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().call(params);	
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "delete channel cost error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	/***
	 *批量导入数据
	 * @param request
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@RequestMapping("batchImportData")
	@ResponseBody
	public AppResult batchImportData(MultipartHttpServletRequest request){
		AppResult result = new AppResult();
		String[] keys = {"channelCode","recordDate","amount","browseCount"};//每列的key
		try {
			List<Map<String,Object>> dataList = ExportUtil.readExcel(keys,request);
//			int errCount = 0;
//			int sucCount = 0;
//			StringBuilder errSb = new StringBuilder();
			if(dataList != null && !dataList.isEmpty()){
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

					List<Map<String,Object>> subDataList = new ArrayList(dataList.subList(formIndex, toIndex));
					AppParam addParam = new AppParam();
					addParam.addAttr("dataList", subDataList);
					addParam.setService("borrowChannelCostService");
					addParam.setMethod("batchImportData");
					addParam.setRmiServiceName(AppProperties
							.getProperties(DuoduoConstant.RMI_SERVICE_START
									+ ServiceKey.Key_busi_in));
					result = RemoteInvoke.getInstance().call(addParam);
					xhSize = xhSize + subDataList.size();
					xhCount ++;
//					errCount = errCount + NumberUtil.getInt(result.getAttr("errCount"), 0);
//					sucCount = sucCount + NumberUtil.getInt(result.getAttr("sucCount"), 0);
//					errSb.append(result.getMessage());
				}

//				String msg = "成功导入:" + sucCount + "条，导入失败:" + errCount + "条。错误行数【" +  errSb.toString() + "】";
//				result.setMessage(msg);
				result.setSuccess(true);
			}else{
				result.setSuccess(false);
				result.setMessage("文件中没有数据记录!");
			}
			
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "batchImportData error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
}
