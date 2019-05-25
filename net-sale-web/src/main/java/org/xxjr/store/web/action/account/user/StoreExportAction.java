package org.xxjr.store.web.action.account.user;


import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ddq.active.mq.message.ExportSend;
import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.SpringAppContext;
import org.ddq.common.exception.ExceptionUtil;
import org.ddq.common.util.DateUtil;
import org.ddq.common.util.JsonUtil;
import org.ddq.common.util.LogerUtil;
import org.ddq.common.util.StringUtil;
import org.ddq.common.web.session.DuoduoSession;
import org.ddq.common.web.session.RequestUtil;
import org.ddq.common.core.service.RemoteInvoke;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.xxjr.busi.util.store.StoreUserUtil;
import org.xxjr.cust.util.info.CustomerIdentify;
import org.xxjr.store.web.util.ExportCountUtil;
import org.xxjr.sys.util.ServiceKey;
import org.xxjr.sys.util.SysParamsUtil;

@Controller()
@RequestMapping("/account/user/export")
public class StoreExportAction {
	/*** 
	 * 将导出人记录数据插入表中
	 * @param request
	 * @param response
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping("insert")
	@ResponseBody
	public AppResult  insert(HttpServletRequest request,HttpServletResponse response){
		AppResult result = new AppResult();
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			String exportParams = StringUtil.getString(params.removeAttr("exportParams"));
			Map<String,Object> queryParams = JsonUtil.getInstance().json2Object(exportParams, Map.class);
			params.addAttrs(queryParams);
			int totalSize = ExportCountUtil.count(params, result, request);
			if(!result.isSuccess()) return result;
			if(totalSize <= 0){
				result.setMessage("无数据导出");
				result.setSuccess(false);
				return result;
			}
			
			String customerId = StoreUserUtil.getCustomerId(request);
			Map<String,Object> custInfo = CustomerIdentify.getCustIdentify(customerId);
			String userName = StringUtil.getString(custInfo.get("userName"));
			String fileName = request.getParameter("fileName");
			String desCosPath = "/upfile/export/";
			String todayStr = DateUtil.toStringByParttern(new Date(),DateUtil.DATE_PATTERN_YYYYMMDD);
			String nowStr = DateUtil.toStringByParttern(new Date(),DateUtil.DATE_PATTERNYYYYMMDDHHMMSSSSS);
			String newFileName = fileName + nowStr + ".xls";
			
			AppParam exportRecordParam = new AppParam("exportRecordService","insert");
			exportRecordParam.addAttr("sysType", "2");
			exportRecordParam.addAttr("exportMan", userName);
			exportRecordParam.addAttr("customerId", customerId);
			exportRecordParam.addAttr("fileName", fileName);
			exportRecordParam.addAttr("parameter", params.toJson());
		    exportRecordParam.addAttr("exportUrl", SysParamsUtil.getStringParamByKey("EXPORT_URL", "https://sys.xxjr.com")
					+desCosPath+todayStr+"/"+newFileName);
		    exportRecordParam.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
			result = RemoteInvoke.getInstance().call(exportRecordParam);
	
			try {
				AppParam exprotParam = params;
				exprotParam.addAttr("recordId", result.getAttr("recordId"));
				exprotParam.addAttr("fileName", newFileName);
				exprotParam.addAttr("totalSize", totalSize);
				//调mq生成文件
				SpringAppContext.getBean(ExportSend.class).export(exprotParam);
			} catch (Exception e) {
				LogerUtil.error(ExportSend.class, e, "store export error");
			}
			
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "导出失败");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
}
