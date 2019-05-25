package org.xxjr.store.web.util;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.service.RemoteInvoke;
import org.ddq.common.util.StringUtil;
import org.springframework.util.StringUtils;
import org.xxjr.busi.util.kefu.ExportCfgUtil;
import org.xxjr.sys.util.NumberUtil;
import org.xxjr.sys.util.SysParamsUtil;
/**
 * 数据导出计数
 * @author LHS
 *
 */
public class ExportCountUtil {
	/**
	 * 导出时每页行数 1000
	 */
	public static int EVERY_PAGE_SIZE = 1000;
	/**
	 * 计数
	 * @return
	 * @throws Exception 
	 */
	public static int count(AppParam params, AppResult result, HttpServletRequest request) throws Exception {
		String exportType = StringUtil.getString(params.getAttr("exportType"));
		//查询导出配置信息
		Map<String, Object> exportCfg = ExportCfgUtil.queryExportCfg(exportType);
		String countMethod = StringUtil.getString(exportCfg.get("countMethod"));
		String serviceKey = StringUtil.getString(exportCfg.get("serviceKey"));
		String serviceName = StringUtil.getString(exportCfg.get("serviceName"));
		int countSize = 0;
		if (!StringUtils.isEmpty(countMethod) && 
				!StringUtils.isEmpty(serviceKey) && 
				!StringUtils.isEmpty(serviceName)) {
			
			params.setMethod(countMethod);
			params.setService(serviceName);
			params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + serviceKey));
			result =  RemoteInvoke.getInstance().callNoTx(params);
			countSize = NumberUtil.getInt(result.getAttr(DuoduoConstant.TOTAL_SIZE),0);
		
		}else{
			//封装参数
	        ExportParamUtil.packParams(exportType, params, result, request);
	        //查询列表的方法
	        params.addAttr("pageMethod", params.getMethod());
	        params.setMethod(StringUtil.getString(params.getAttr(ExportParamUtil.P_COUNT_METHOD)));
	        result = RemoteInvoke.getInstance().callNoTx(params);
	        countSize = NumberUtil.getInt(result.getAttr(DuoduoConstant.TOTAL_SIZE));
		}
		
		if(result.isSuccess()){
			int maxExportCount = SysParamsUtil.getIntParamByKey("KF_Export_Count", 5000);
			if(countSize > maxExportCount){
				result.setSuccess(false);
				result.setMessage("导出数据过多【最大导出" + maxExportCount + "】，请缩小时间区间，分段导出！");
			}
		}
        
		return countSize;
	}
}
