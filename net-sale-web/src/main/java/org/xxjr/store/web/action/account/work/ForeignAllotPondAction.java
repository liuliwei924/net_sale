package org.xxjr.store.web.action.account.work;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.exception.ExceptionUtil;
import org.ddq.common.exception.SysException;
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
import org.xxjr.busi.util.store.StoreUserUtil;
import org.xxjr.cust.util.CustConstant;
import org.xxjr.cust.util.info.CustomerIdentify;
import org.xxjr.sys.util.ServiceKey;

/**
 * 对外分配池
 * @author zenghw
 *
 */
@Controller()
@RequestMapping("/account/work/foreignAllotPond")
public class ForeignAllotPondAction {
	/**
	 * 查询对外分配池列表
	 * @param request
	 * @return
	 */
	@RequestMapping("/queryForeignAllotPond")
	@ResponseBody
	public AppResult queryForeignAllotPond(HttpServletRequest request) {
		AppResult result = new AppResult();
		String customerId = StoreUserUtil.getCustomerId(request);
		try {
			AppParam param = new AppParam("thirdExportPoolService", "queryForeignAllotPond");
//			param.addAttr("downloadStatus", "0");//未下载
			RequestUtil.setAttr(param, request);
			StoreUserUtil.dealUserAuthParam(param, customerId, "customerId");
			Map<String, Object> custInfo = CustomerIdentify.getCustIdentify(customerId);
			if(custInfo != null){
				String authType =   StringUtil.getString(custInfo.get("roleType").toString());
				if(CustConstant.CUST_ROLETYPE_2.equals(authType)){//推广人员查看全部
					param.removeAttr("customerId");
				}
			}
			param.setOrderBy("createTime");
			param.setOrderValue("desc");
			param.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(param);
			return result;
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "queryForeignAllotPond error");
			ExceptionUtil.setExceptionMessage(e, result,
					DuoduoSession.getShowLog());
		}
		return result;
	}
	
	
	/**
	 * 加入第三方
	 * @param request
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping("/checkJoinThreeDate")
	@ResponseBody
	public AppResult checkJoinThreeDate(HttpServletRequest request) {
		AppResult result = new AppResult();
		try {
			Map<String, Object> orderInfo = JsonUtil.getInstance().json2Object(
					request.getParameter("str"), Map.class);
			if (StringUtils.isEmpty(orderInfo)) {
				throw new SysException("订单的基本信息不能为空");
			}
			List<Map<String, Object>> orders = (List<Map<String, Object>>) orderInfo
					.get("orders");
			if (StringUtils.isEmpty(orders)) {
				throw new SysException("请传入订单的基本信息");
			}
			String custId = StoreUserUtil.getCustomerId(request);
			if (StringUtils.isEmpty(custId)) {
				throw new SysException("用户ID不能为空");
			}
			String customerId = request.getParameter("customerId");
			if (StringUtils.isEmpty(customerId)) {
				throw new SysException("外部渠道人员不能为空");
			}
			
			int totalSucSize = 0;
			int totalFailSize = 0;
			StringBuffer strBuff = new StringBuffer();
			AppParam updateParam = new AppParam();
			updateParam.setService("thirdExportPoolService");
			updateParam.setMethod("checkJoinThreeDate");
			updateParam.addAttr("customerId", customerId);
			for (Map<String, Object> orderMap : orders) {
				updateParam.addAttr("orderMap", orderMap);
				updateParam.setRmiServiceName(AppProperties
						.getProperties(DuoduoConstant.RMI_SERVICE_START
								+ ServiceKey.Key_busi_in));
				result = RemoteInvoke.getInstance().call(updateParam);
				if(result.isSuccess()){
					totalSucSize ++;
				}else{
					totalFailSize ++;
					strBuff.append("applyId:"+orderMap.get("applyId"))
					.append("->")
					.append(result.getMessage());
				}
			}
			
			result.setMessage("总共:"+orders.size()+"笔,成功:"+totalSucSize+"笔,失败:"+totalFailSize+"笔，失败原因:"+strBuff.toString());
		} catch (Exception e) {
			LogerUtil.error(WaitDealAction.class, e, "joinThreeDate error");
			ExceptionUtil.setExceptionMessage(e, result,
					DuoduoSession.getShowLog());
		}
		return result;
	}
}
