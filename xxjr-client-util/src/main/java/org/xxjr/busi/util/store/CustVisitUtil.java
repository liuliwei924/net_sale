package org.xxjr.busi.util.store;

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.service.RemoteInvoke;
import org.ddq.common.util.StringUtil;
import org.xxjr.cust.util.info.CustomerUtil;
import org.xxjr.sys.util.ServiceKey;

/***
 * 用户上门登记工具类
 * @author sty
 *
 */
public class CustVisitUtil {

	/***
	 * 查询用户的当天的登记记录
	 * @param telephone
	 * @return
	 */
	public static AppResult getCustVisitList(String telephone){
		AppResult result = new AppResult();
		AppParam params = new AppParam();
		params.setService("custVisitService");
		params.setMethod("query");
		params.addAttr("custTel", telephone);
		params.addAttr("today", new Date());
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
		result = RemoteInvoke.getInstance().call(params);
		
		return result;
	}
	
	/***
	 * 查询门店人员基本信息（分销使用）
	 * @param telephone
	 * @return
	 */
	public static AppResult getStoreCustBaseInfo(AppParam params){
		String telephone = StringUtil.getString(params.getAttr("telephone"));
		if(StringUtils.isEmpty(telephone)){
			return CustomerUtil.retErrorMsg("缺少必要参数,手机号码不能为空");
		}
		params.setService("borrowStoreApplyService");
		params.setMethod("queryStoreCustBaseInfo");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
		AppResult result = RemoteInvoke.getInstance().callNoTx(params);
		return result;
	}
}
