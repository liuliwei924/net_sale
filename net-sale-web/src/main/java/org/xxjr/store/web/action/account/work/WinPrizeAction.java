package org.xxjr.store.web.action.account.work;

import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.exception.ExceptionUtil;
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
import org.xxjr.sys.util.ServiceKey;
import org.xxjr.sys.util.ValidUtils;

/**
 * 上海幸运大转盘中奖记录
 * @author zenghw
 *
 */
@Controller()
@RequestMapping("/account/work/winPrize/")
public class WinPrizeAction {
	
	/**
	 * 查询中奖记录
	 * @param request
	 * @return
	 */
	@RequestMapping("queryWinPrize")
	@ResponseBody
	public AppResult queryWinPrize(HttpServletRequest request){
		AppResult result = new AppResult();
		String customerId = StoreUserUtil.getCustomerId(request);
		if (StringUtils.isEmpty(customerId)) {
			result.setSuccess(false);
			result.setMessage("用户ID不能为空");
			return result;
		}
		Map<String,Object> custMap = StoreUserUtil.getCustomerInfo(customerId);
		String orgId = "";
		String cityName = "";
		if(custMap !=null){
			orgId = StringUtil.getString(custMap.get("orgId"));
			AppParam orgParam = new AppParam("orgService","queryOrgList");
			orgParam.addAttr("orgId", orgId);
			orgParam.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
								+ ServiceKey.Key_cust));
			AppResult orgResult = RemoteInvoke.getInstance().callNoTx(orgParam);
			if(orgResult.getRows() != null && orgResult.getRows().size() > 0){
				cityName = StringUtil.getString(orgResult.getRow(0).get("cityName"));
				if(StringUtils.isEmpty(cityName)){
					return result;
				}
			}
		}
		try{
			//客户姓名或手机号
			AppParam param = new AppParam("receiveRewardRecordService","queryWinPrize");
			RequestUtil.setAttr(param, request);
			String nickNameAndTel = request.getParameter("searchKey");
			if(!StringUtils.isEmpty(nickNameAndTel)){
				if(ValidUtils.validateTelephone(nickNameAndTel)){//加快查询效率
					param.addAttr("telephone", nickNameAndTel);
					param.removeAttr("searchKey");
				}else{
					param.addAttr("applyName", nickNameAndTel);
					param.removeAttr("searchKey");
				}
			}
			//处理人姓名或手机号
			String handelSearchKey = request.getParameter("handelSearchKey");
			if(!StringUtils.isEmpty(handelSearchKey)){
				if(ValidUtils.validateTelephone(handelSearchKey)){//加快查询效率
					param.addAttr("updateTel", handelSearchKey);
					param.removeAttr("handelSearchKey");
				}else{
					param.addAttr("realName", handelSearchKey);
					param.removeAttr("handelSearchKey");
				}
			}
			String roleType = StringUtil.getString(custMap.get("roleType"));
			if(!CustConstant.CUST_ROLETYPE_1.equals(roleType)){
				param.addAttr("cityName", cityName);
			}
			param.setOrderBy("createTime");
			param.setOrderValue("desc");
			param.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
								+ ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(param);
		}catch(Exception e){
			LogerUtil.error(WinPrizeAction.class, e, "queryWinPrize error");
			ExceptionUtil.setExceptionMessage(e, result,
					DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 编辑中奖记录
	 * @param request
	 * @return
	 */
	@RequestMapping("checkWinPrize")
	@ResponseBody
	public AppResult checkWinPrize(HttpServletRequest request){
		AppResult result = new AppResult();
		String customerId = StoreUserUtil.getCustomerId(request);
		if (StringUtils.isEmpty(customerId)) {
			result.setSuccess(false);
			result.setMessage("用户ID不能为空");
			return result;
		}
		String receiveStatus = request.getParameter("receiveStatus");
		if (StringUtils.isEmpty(receiveStatus)) {
			result.setSuccess(false);
			result.setMessage("审核状态不能为空");
			return result;
		}
		try{
			AppParam param = new AppParam("receiveRewardRecordService","update");
			RequestUtil.setAttr(param, request);
			param.addAttr("updateBy", customerId);
			param.addAttr("updateTime", new Date());
			param.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
								+ ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(param);
		}catch(Exception e){
			LogerUtil.error(WinPrizeAction.class, e, "updateWinPrize error");
			ExceptionUtil.setExceptionMessage(e, result,
					DuoduoSession.getShowLog());
		}
		return result;
	}
}
