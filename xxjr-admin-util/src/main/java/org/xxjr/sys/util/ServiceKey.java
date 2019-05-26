package org.xxjr.sys.util;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.SpringAppContext;
import org.ddq.common.core.service.RemoteInvoke;
import org.ddq.common.core.service.SoaManager;
import org.ddq.common.util.StringUtil;



/***
 * 系统服务 关健字
 * @author qinxcb
 *
 */
public class ServiceKey {

	/**业务服务相关*/
//	public static final String Key_busi="busi";
	/**系统服务相关*/
	public static final String Key_sys="sys";
	/**客户信息相关*/
	public static final String Key_cust="cust";
	/**扩展业务服务相关*/
	public static final String Key_busi_in="busiIn";
	/**统计服务服务相关*/
	public static final String Key_sum="summer";
	
	/**
	 * 用事务调用
	 * @param params
	 * @param rimKey
	 */
	public static AppResult doCall(AppParam params, String rimKey){
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + rimKey));
		
		String service = StringUtil.getString(params.getService());
		AppResult result = null;
		
		if (SpringAppContext.getBean(service) == null) {
			result = RemoteInvoke.getInstance().call(params);
		}else{
			result = SoaManager.getInstance().invoke(params);
		}
		
		return result ;
		
	}
	
	/**无事务调用
	 * 
	 * @param params
	 * @param rimKey
	 */
	public static AppResult doCallNoTx(AppParam params, String rimKey){
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + rimKey));
		
		String service = StringUtil.getString(params.getService());
		AppResult result = null;
		
		if (SpringAppContext.getBean(service) == null) {
			result = RemoteInvoke.getInstance().callNoTx(params);
		}else{
			result = SoaManager.getInstance().callNoTx(params);
		}
		
		return result ;
		
	}
}
