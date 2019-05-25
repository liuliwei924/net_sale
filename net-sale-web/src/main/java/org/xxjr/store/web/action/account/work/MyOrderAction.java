package org.xxjr.store.web.action.account.work;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.exception.ExceptionUtil;
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
import org.xxjr.cust.util.info.CustomerUtil;
import org.xxjr.store.util.StoreApplyUtils;
import org.xxjr.sys.util.NumberUtil;
import org.xxjr.sys.util.ServiceKey;
import org.xxjr.sys.util.ValidUtils;

@Controller()
@RequestMapping("/account/work")
/**
 * 我的订单
 * @author Administrator
 *
 */
public class MyOrderAction {
	/**
	 * 查询我的订单预约中列表
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("/reserving/queryReservList")
	@ResponseBody
	public AppResult queryReservList(HttpServletRequest request) {
		AppResult result = new AppResult();
		try {
			String customerId = StoreUserUtil.getCustomerId(request);
			if (StringUtils.isEmpty(customerId)) {
				return CustomerUtil.retErrorMsg("用户ID不能为空");
			}
			AppParam param = new AppParam("storeHandleExtService", "queryBookOrderList");
			RequestUtil.setAttr(param, request);
			StoreUserUtil.dealUserAuthParam(param, customerId, "lastStore");
			String searchKey = request.getParameter("searchKey");
			if(!StringUtils.isEmpty(searchKey)){
				if(ValidUtils.validateTelephone(searchKey)){//加快查询效率
					param.addAttr("telephone", searchKey);
					param.removeAttr("searchKey");
				}else{
					param.addAttr("applyName", searchKey);
					param.removeAttr("searchKey");
				}
			}
			String storeSearchKey = request.getParameter("storeSearchKey");
			if(!StringUtils.isEmpty(storeSearchKey)){
				if(ValidUtils.validateTelephone(storeSearchKey)){//加快查询效率
					param.addAttr("mobile", storeSearchKey);
					param.removeAttr("storeSearchKey");
				}else{
					param.addAttr("realName", storeSearchKey);
					param.removeAttr("storeSearchKey");
				}
			}
			param.addAttr("orderStatusNotIn", "7,8");//不查询无效客户和空号/错号客户
			param.addAttr("bookStatus", 1);
			param.setOrderBy("bookTime");
			param.setOrderValue("desc");
			param.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(param);
			return result;
		} catch (Exception e) {
			LogerUtil.error(MyOrderAction.class, e, "queryReservList error");
			ExceptionUtil.setExceptionMessage(e, result,
					DuoduoSession.getShowLog());
		}
		return result;
	}
	
	
	/**
	 * 查询工作订单已上门列表
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("/visitOrder/queryVisitOrderList")
	@ResponseBody
	public AppResult queryVisitOrderList(HttpServletRequest request) {
		AppResult result = new AppResult();
		try {
			String customerId = StoreUserUtil.getCustomerId(request);
			if (StringUtils.isEmpty(customerId)) {
				return CustomerUtil.retErrorMsg("用户ID不能为空");
			}
			AppParam param = new AppParam("storeHandleExtService", "queryBookOrderList");
			RequestUtil.setAttr(param, request);
			StoreUserUtil.dealUserAuthParam(param, customerId, "lastStore");
			String searchKey = request.getParameter("searchKey");
			if(!StringUtils.isEmpty(searchKey)){
				if(ValidUtils.validateTelephone(searchKey)){//加快查询效率
					param.addAttr("telephone", searchKey);
					param.removeAttr("searchKey");
				}else{
					param.addAttr("applyName", searchKey);
					param.removeAttr("searchKey");
				}
			}
			String storeSearchKey = request.getParameter("storeSearchKey");
			if(!StringUtils.isEmpty(storeSearchKey)){
				if(ValidUtils.validateTelephone(storeSearchKey)){//加快查询效率
					param.addAttr("mobile", storeSearchKey);
					param.removeAttr("storeSearchKey");
				}else{
					param.addAttr("realName", storeSearchKey);
					param.removeAttr("storeSearchKey");
				}
			}
			param.addAttr("bookStatus", 3);
			param.setOrderBy("visitTime");
			param.setOrderValue("desc");
			param.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(param);
			return result;
		} catch (Exception e) {
			LogerUtil.error(MyOrderAction.class, e, "queryVisitOrderList error");
			ExceptionUtil.setExceptionMessage(e, result,
					DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 查询我的订单签单中列表
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("/signing/querySigningList")
	@ResponseBody
	public AppResult querySigningList(HttpServletRequest request) {
		AppResult result = new AppResult();
		try {
			String customerId = StoreUserUtil.getCustomerId(request);
			if (StringUtils.isEmpty(customerId)) {
				return CustomerUtil.retErrorMsg("用户ID不能为空");
			}
			AppParam param = new AppParam("storeListOptExtService", "querySigned");
			RequestUtil.setAttr(param, request);
			StoreUserUtil.dealUserAuthParam(param, customerId, "customerId");
			String searchKey = request.getParameter("searchKey");
			if(!StringUtils.isEmpty(searchKey)){
				if(ValidUtils.validateTelephone(searchKey)){//加快查询效率
					param.addAttr("telephone", searchKey);
					param.removeAttr("searchKey");
				}else{
					param.addAttr("applyName", searchKey);
					param.removeAttr("searchKey");
				}
			}
			String storeSearchKey = request.getParameter("storeSearchKey");
			if(!StringUtils.isEmpty(storeSearchKey)){
				if(ValidUtils.validateTelephone(storeSearchKey)){//加快查询效率
					param.addAttr("mobile", storeSearchKey);
					param.removeAttr("storeSearchKey");
				}else{
					param.addAttr("realName", storeSearchKey);
					param.removeAttr("storeSearchKey");
				}
			}
			String signSearchKey = request.getParameter("signSearchKey");
			if(!StringUtils.isEmpty(signSearchKey)){
				if(ValidUtils.validateTelephone(signSearchKey)){//加快查询效率
					param.addAttr("signMobile", signSearchKey);
					param.removeAttr("signSearchKey");
				}else{
					param.addAttr("signOrderName", signSearchKey);
					param.removeAttr("signSearchKey");
				}
			}
			String roleType = StoreUserUtil.getCustomerRole(customerId);
			param.addAttr("roleType",roleType);
			param.setOrderBy("t.createTime");
			param.setOrderValue("desc");
			param.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(param);
			if(result.getRows().size() > 0){
				for(Map<String,Object> map : result.getRows()){
					boolean orgCFSFlag = StoreApplyUtils.isHaveAuthUpCFS(StringUtil.getString(map.get("orgId")));
					map.put("orgCFSFlag", orgCFSFlag);
				}
			}
			return result;
		} catch (Exception e) {
			LogerUtil.error(MyOrderAction.class, e, "querySigningList error");
			ExceptionUtil.setExceptionMessage(e, result,
					DuoduoSession.getShowLog());
		}
		return result;
	}
	/**
	 * 查询签单结案列表
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("/signEnd/querySignEnd")
	@ResponseBody
	public AppResult querySignEnd(HttpServletRequest request) {
		AppResult result = new AppResult();
		try {
			String customerId = StoreUserUtil.getCustomerId(request);
			if (StringUtils.isEmpty(customerId)) {
				return CustomerUtil.retErrorMsg("用户ID不能为空");
			}
			AppParam param = new AppParam("storeListOptExtService", "querySigned");
			RequestUtil.setAttr(param, request);
			StoreUserUtil.dealUserAuthParam(param, customerId, "customerId");
			String searchKey = request.getParameter("searchKey");
			if(!StringUtils.isEmpty(searchKey)){
				if(ValidUtils.validateTelephone(searchKey)){//加快查询效率
					param.addAttr("telephone", searchKey);
					param.removeAttr("searchKey");
				}else{
					param.addAttr("applyName", searchKey);
					param.removeAttr("searchKey");
				}
			}
			String storeSearchKey = request.getParameter("storeSearchKey");
			if(!StringUtils.isEmpty(storeSearchKey)){
				if(ValidUtils.validateTelephone(storeSearchKey)){//加快查询效率
					param.addAttr("mobile", storeSearchKey);
					param.removeAttr("storeSearchKey");
				}else{
					param.addAttr("realName", storeSearchKey);
					param.removeAttr("storeSearchKey");
				}
			}
			String signSearchKey = request.getParameter("signSearchKey");
			if(!StringUtils.isEmpty(signSearchKey)){
				if(ValidUtils.validateTelephone(signSearchKey)){//加快查询效率
					param.addAttr("signMobile", signSearchKey);
					param.removeAttr("signSearchKey");
				}else{
					param.addAttr("signOrderName", signSearchKey);
					param.removeAttr("signSearchKey");
				}
			}
			String roleType = StoreUserUtil.getCustomerRole(customerId);
			param.addAttr("roleType",roleType);
			param.setOrderBy("t.createTime");
			param.setOrderValue("desc");
			param.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(param);
			if(result.getRows().size() > 0){
				for(Map<String,Object> map : result.getRows()){
					boolean orgCFSFlag = StoreApplyUtils.isHaveAuthUpCFS(StringUtil.getString(map.get("orgId")));
					map.put("orgCFSFlag", orgCFSFlag);
				}
			}
			return result;
		} catch (Exception e) {
			LogerUtil.error(MyOrderAction.class, e, "querySignEnd error");
			ExceptionUtil.setExceptionMessage(e, result,
					DuoduoSession.getShowLog());
		}
		return result;
	}
	/**
	 * 查询我的订单回款处理列表
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("/backDeal/queryBackDealList")
	@ResponseBody
	public AppResult queryBackDealList(HttpServletRequest request) {
		AppResult result = new AppResult();
		try {
			String customerId = StoreUserUtil.getCustomerId(request);
			if (StringUtils.isEmpty(customerId)) {
				return CustomerUtil.retErrorMsg("用户ID不能为空");
			}
			AppParam param = new AppParam("storeListOptExtService", "queryReLoan");			
			RequestUtil.setAttr(param, request);
			StoreUserUtil.dealUserAuthParam(param, customerId, "customerId");
			String searchKey = request.getParameter("searchKey");
			if(!StringUtils.isEmpty(searchKey)){
				if(ValidUtils.validateTelephone(searchKey)){//加快查询效率
					param.addAttr("telephone", searchKey);
					param.removeAttr("searchKey");
				}else{
					param.addAttr("applyName", searchKey);
					param.removeAttr("searchKey");
				}
			}
			String storeSearchKey = request.getParameter("storeSearchKey");
			if(!StringUtils.isEmpty(storeSearchKey)){
				if(ValidUtils.validateTelephone(storeSearchKey)){//加快查询效率
					param.addAttr("mobile", storeSearchKey);
					param.removeAttr("storeSearchKey");
				}else{
					param.addAttr("realName", storeSearchKey);
					param.removeAttr("storeSearchKey");
				}
			}
			String reLoanSearchKey = request.getParameter("reLoanSearchKey");
			if(!StringUtils.isEmpty(reLoanSearchKey)){
				if(ValidUtils.validateTelephone(reLoanSearchKey)){//加快查询效率
					param.addAttr("reLoanMobile", reLoanSearchKey);
					param.removeAttr("reLoanSearchKey");
				}else{
					param.addAttr("reLoanName", reLoanSearchKey);
					param.removeAttr("reLoanSearchKey");
				}
			}
			String roleType = StoreUserUtil.getCustomerRole(customerId);
			param.addAttr("roleType",roleType);
			param.setOrderBy("t.feeAmountDate,t.recordId");
			param.setOrderValue("desc,asc");
			param.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(param);
			return result;
		} catch (Exception e) {
			LogerUtil.error(MyOrderAction.class, e, "queryBackDealList error");
			ExceptionUtil.setExceptionMessage(e, result,
					DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 查询专属订单列表
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("/excOrder/queryExeOrderList")
	@ResponseBody
	public AppResult queryExeOrderList(HttpServletRequest request) {
		AppResult result = new AppResult();
		try {
			String customerId = StoreUserUtil.getCustomerId(request);
			if (StringUtils.isEmpty(customerId)) {
				return CustomerUtil.retErrorMsg("用户ID不能为空");
			}
			AppParam param = new AppParam("exclusiveOrderService", "queryExcOrder");
			RequestUtil.setAttr(param, request);
			param.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			StoreUserUtil.dealUserAuthParam(param, customerId, "customerId");
			String searchKey = request.getParameter("searchKey");
			if(!StringUtils.isEmpty(searchKey)){
				if(ValidUtils.validateTelephone(searchKey)){//加快查询效率
					param.addAttr("telephone", searchKey);
					param.removeAttr("searchKey");
				}else{
					param.addAttr("applyName", searchKey);
					param.removeAttr("searchKey");
				}
			}
			String storeSearchKey = request.getParameter("storeSearchKey");
			if(!StringUtils.isEmpty(storeSearchKey)){
				if(ValidUtils.validateTelephone(storeSearchKey)){//加快查询效率
					param.addAttr("mobile", storeSearchKey);
					param.removeAttr("storeSearchKey");
				}else{
					param.addAttr("realName", storeSearchKey);
					param.removeAttr("storeSearchKey");
				}
			}
			//资产信息
			String assetInfoIn = request.getParameter("assetInfoIn");
			if(!StringUtils.isEmpty(assetInfoIn)){
				String[] assetInfoArr = assetInfoIn.split(",");
				List<String> assetInfoList = Arrays.asList(assetInfoArr);
				for(String str : assetInfoList){
					// 0-本地房 1-外地房 2-车产 3-保单 4-社保5-公积金 6-微粒贷
					if("0".equals(str)){
						param.addAttr("houseType","1");
						param.addAttr("housePlace1","1");
					}else if("1".equals(str)){
						param.addAttr("houseType","1");
						param.addAttr("housePlace2","2");
					}else if("2".equals(str)){
						param.addAttr("carType","2");
					}else if("3".equals(str)){
						param.addAttr("insure","3");
					}else if("4".equals(str)){
						param.addAttr("socialType","4");
					}else if("5".equals(str)){
						param.addAttr("fundType","5");
					}else if("6".equals(str)){
						param.addAttr("havePinan","6");
					}
				}
			}
			param.setOrderBy("createTime");
			param.setOrderValue("desc");
			result = RemoteInvoke.getInstance().callNoTx(param);
			return result;
		} catch (Exception e) {
			LogerUtil.error(MyOrderAction.class, e, "queryExeOrderList error");
			ExceptionUtil.setExceptionMessage(e, result,
					DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 查询我的所有订单中统计数据
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("/allOrder/queryAllSummary")
	@ResponseBody
	public AppResult queryAllSummary(HttpServletRequest request) {
		AppResult result = new AppResult();
		try {
			String customerId = StoreUserUtil.getCustomerId(request);
			if (StringUtils.isEmpty(customerId)) {
				return CustomerUtil.retErrorMsg("用户ID不能为空");
			}
			AppParam param = new AppParam();
			param.setService("storeHandleExtService");
			param.setMethod("queryAllSummary");
			param.addAttr("status", "2");//门店锁定
			RequestUtil.setAttr(param, request);
			StoreUserUtil.dealUserAuthParam(param, customerId, "lastStore");
			String searchKey = request.getParameter("searchKey");
			boolean searchFlag = false;
			if(!StringUtils.isEmpty(searchKey)){
				if(ValidUtils.validateTelephone(searchKey)){//加快查询效率
					param.addAttr("telephone", searchKey);
					param.removeAttr("searchKey");
					if(StringUtils.isEmpty(param.getAttr("orderStatus"))
							&& StringUtils.isEmpty(param.getAttr("startHandleDate"))
							&& StringUtils.isEmpty(param.getAttr("endHandleDate"))){
						searchFlag = true;
					}
				}else{
					param.addAttr("applyName", searchKey);
					param.removeAttr("searchKey");
				}
			}
			String storeSearchKey = request.getParameter("storeSearchKey");
			if(!StringUtils.isEmpty(storeSearchKey)){
				if(ValidUtils.validateTelephone(storeSearchKey)){//加快查询效率
					param.addAttr("mobile", storeSearchKey);
					param.removeAttr("storeSearchKey");
				}else{
					param.addAttr("realName", storeSearchKey);
					param.removeAttr("storeSearchKey");
				}
			}
			//资产信息
			String assetInfoIn = request.getParameter("assetInfoIn");
			if(!StringUtils.isEmpty(assetInfoIn)){
				String[] assetInfoArr = assetInfoIn.split(",");
				List<String> assetInfoList = Arrays.asList(assetInfoArr);
				for(String str : assetInfoList){
					// 0-本地房 1-外地房 2-车产 3-保单 4-社保5-公积金 6-微粒贷
					if("0".equals(str)){
						param.addAttr("houseType","1");
						param.addAttr("housePlace1","1");
					}else if("1".equals(str)){
						param.addAttr("houseType","1");
						param.addAttr("housePlace2","2");
					}else if("2".equals(str)){
						param.addAttr("carType","2");
					}else if("3".equals(str)){
						param.addAttr("insure","3");
					}else if("4".equals(str)){
						param.addAttr("socialType","4");
					}else if("5".equals(str)){
						param.addAttr("fundType","5");
					}else if("6".equals(str)){
						param.addAttr("havePinan","6");
					}
				}
			}
			String curDuration = request.getParameter("curDuration");
			if(!StringUtils.isEmpty(curDuration)){
				boolean numberFlag = ValidUtils.validValue(ValidUtils.Valide_double2, curDuration);
				if(!numberFlag){
					return CustomerUtil.retErrorMsg("最近成功通话时长必须是数字，请重新输入！");
				}
				param.addAttr("curDuration",NumberUtil.getInt(NumberUtil.getDouble(curDuration) * 60));
			}
			param.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(param);
			if(result.getRows().size() > 0 && !StringUtils.isEmpty(result.getRow(0))){
				int allOrderCount = NumberUtil.getInt(result.getRow(0).get("allOrderCount"));
				if(allOrderCount == 0 && searchFlag){
					param.setService("storeListOptExtService");
					param.setMethod("queryBorrowAllSummary");
					result = RemoteInvoke.getInstance().callNoTx(param);
				}
			}
		} catch (Exception e) {
			LogerUtil.error(MyOrderAction.class, e, "queryAllSummary error");
			ExceptionUtil.setExceptionMessage(e, result,
					DuoduoSession.getShowLog());
		}
		return result;
	}

	
	/**
	 * 查询我的所有订单
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("/allOrder/queryAllList")
	@ResponseBody
	public AppResult queryAllList(HttpServletRequest request) {
		AppResult result = new AppResult();
		try {
			String customerId = StoreUserUtil.getCustomerId(request);
			if (StringUtils.isEmpty(customerId)) {
				return CustomerUtil.retErrorMsg("用户ID不能为空");
			}
			AppParam param = new AppParam();
			param.setService("storeHandleExtService");
			param.setMethod("queryAllList");
			RequestUtil.setAttr(param, request);
			param.addAttr("status", "2");//门店锁定
			StoreUserUtil.dealUserAuthParam(param, customerId, "lastStore");
			String searchKey = request.getParameter("searchKey");
			boolean searchFlag = false;
			if(!StringUtils.isEmpty(searchKey)){
				if(ValidUtils.validateTelephone(searchKey)){//加快查询效率
					param.addAttr("telephone", searchKey);
					param.removeAttr("searchKey");
					if(StringUtils.isEmpty(param.getAttr("orderStatus")) 
							&& StringUtils.isEmpty(param.getAttr("startHandleDate"))
							&& StringUtils.isEmpty(param.getAttr("endHandleDate"))){
						searchFlag = true;
					}
				}else{
					param.addAttr("applyName", searchKey);
					param.removeAttr("searchKey");
				}
			}
			String storeSearchKey = request.getParameter("storeSearchKey");
			if(!StringUtils.isEmpty(storeSearchKey)){
				if(ValidUtils.validateTelephone(storeSearchKey)){//加快查询效率
					param.addAttr("mobile", storeSearchKey);
					param.removeAttr("storeSearchKey");
				}else{
					param.addAttr("realName", storeSearchKey);
					param.removeAttr("storeSearchKey");
				}
			}
			String roleType = StoreUserUtil.getCustomerRole(customerId);
			param.addAttr("roleType",roleType);
			//资产信息
			String assetInfoIn = request.getParameter("assetInfoIn");
			if(!StringUtils.isEmpty(assetInfoIn)){
				String[] assetInfoArr = assetInfoIn.split(",");
				List<String> assetInfoList = Arrays.asList(assetInfoArr);
				for(String str : assetInfoList){
					// 0-本地房 1-外地房 2-车产 3-保单 4-社保5-公积金 6-微粒贷
					if("0".equals(str)){
						param.addAttr("houseType","1");
						param.addAttr("housePlace1","1");
					}else if("1".equals(str)){
						param.addAttr("houseType","1");
						param.addAttr("housePlace2","2");
					}else if("2".equals(str)){
						param.addAttr("carType","2");
					}else if("3".equals(str)){
						param.addAttr("insure","3");
					}else if("4".equals(str)){
						param.addAttr("socialType","4");
					}else if("5".equals(str)){
						param.addAttr("fundType","5");
					}else if("6".equals(str)){
						param.addAttr("havePinan","6");
					}
				}
			}
			String curDuration = request.getParameter("curDuration");
			if(!StringUtils.isEmpty(curDuration)){
				boolean numberFlag = ValidUtils.validValue(ValidUtils.Valide_double2, curDuration);
				if(!numberFlag){
					return CustomerUtil.retErrorMsg("最近成功通话时长必须是数字，请重新输入!");
				}
				param.addAttr("curDuration",NumberUtil.getInt(NumberUtil.getDouble(curDuration) * 60));
			}
			param.setOrderBy("lastUpdateTime");
			param.setOrderValue("desc");
			param.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(param);
			if(result.getRows().size() == 0 && searchFlag){
				param.setService("storeListOptExtService");
				param.setMethod("queryBorrowAllList");
				param.setOrderBy("lastTime");
				result = RemoteInvoke.getInstance().callNoTx(param);
				if(result.getRows().size() > 0){
					result.putAttr("searchDetailFlag", "1");
				}
			}
		} catch (Exception e) {
			LogerUtil.error(MyOrderAction.class, e, "queryAllList error");
			ExceptionUtil.setExceptionMessage(e, result,
					DuoduoSession.getShowLog());
		}
		return result;
	}
	/**
	 * 查询未上门待签约
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("/notWaitSignOrder/queryNotWaitSignOrder")
	@ResponseBody
	public AppResult queryNotWaitSignOrder(HttpServletRequest request) {
		AppResult result = new AppResult();
		try {
			String customerId = StoreUserUtil.getCustomerId(request);
			if (StringUtils.isEmpty(customerId)) {
				return CustomerUtil.retErrorMsg("用户ID不能为空");
			}
			AppParam param = new AppParam();
			param.setService("storeListOptExtService");
			param.setMethod("queryStoreApplyList");
			param.addAttr("orderStatus", "1");
			param.addAttr("status", "2");//门店锁定
			RequestUtil.setAttr(param, request);
			StoreUserUtil.dealUserAuthParam(param, customerId, "lastStore");
			String searchKey = request.getParameter("searchKey");
			if(!StringUtils.isEmpty(searchKey)){
				if(ValidUtils.validateTelephone(searchKey)){//加快查询效率
					param.addAttr("telephone", searchKey);
					param.removeAttr("searchKey");
				}else{
					param.addAttr("applyName", searchKey);
					param.removeAttr("searchKey");
				}
			}
			String storeSearchKey = request.getParameter("storeSearchKey");
			if(!StringUtils.isEmpty(storeSearchKey)){
				if(ValidUtils.validateTelephone(storeSearchKey)){//加快查询效率
					param.addAttr("mobile", storeSearchKey);
					param.removeAttr("storeSearchKey");
				}else{
					param.addAttr("realName", storeSearchKey);
					param.removeAttr("storeSearchKey");
				}
			}
			param.setOrderBy("lastUpdateTime");
			param.setOrderValue("desc");
			param.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(param);
		} catch (Exception e) {
			LogerUtil.error(MyOrderAction.class, e, "queryNotWaitSignOrder error");
			ExceptionUtil.setExceptionMessage(e, result,
					DuoduoSession.getShowLog());
		}
		return result;
	}
	/**
	 * 查询已上门待签约
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("/waitSignOrder/queryWaitSignOrder")
	@ResponseBody
	public AppResult queryWaitSignOrder(HttpServletRequest request) {
		AppResult result = new AppResult();
		try {
			String customerId = StoreUserUtil.getCustomerId(request);
			if (StringUtils.isEmpty(customerId)) {
				return CustomerUtil.retErrorMsg("用户ID不能为空");
			}
			AppParam param = new AppParam();
			param.setService("storeListOptExtService");
			param.setMethod("queryStoreApplyList");
			param.addAttr("orderStatus", "2");
			param.addAttr("status", "2");//门店锁定
			RequestUtil.setAttr(param, request);
			StoreUserUtil.dealUserAuthParam(param, customerId, "lastStore");
			String searchKey = request.getParameter("searchKey");
			if(!StringUtils.isEmpty(searchKey)){
				if(ValidUtils.validateTelephone(searchKey)){//加快查询效率
					param.addAttr("telephone", searchKey);
					param.removeAttr("searchKey");
				}else{
					param.addAttr("applyName", searchKey);
					param.removeAttr("searchKey");
				}
			}
			String storeSearchKey = request.getParameter("storeSearchKey");
			if(!StringUtils.isEmpty(storeSearchKey)){
				if(ValidUtils.validateTelephone(storeSearchKey)){//加快查询效率
					param.addAttr("mobile", storeSearchKey);
					param.removeAttr("storeSearchKey");
				}else{
					param.addAttr("realName", storeSearchKey);
					param.removeAttr("storeSearchKey");
				}
			}
			param.setOrderBy("lastUpdateTime");
			param.setOrderValue("desc");
			param.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(param);
		} catch (Exception e) {
			LogerUtil.error(MyOrderAction.class, e, "queryWaitSignOrder error");
			ExceptionUtil.setExceptionMessage(e, result,
					DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 查询未了解
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("/waitRecognized/queryWaitRecognized")
	@ResponseBody
	public AppResult queryWaitRecognized(HttpServletRequest request) {
		AppResult result = new AppResult();
		try {
			String customerId = StoreUserUtil.getCustomerId(request);
			if (StringUtils.isEmpty(customerId)) {
				return CustomerUtil.retErrorMsg("用户ID不能为空");
			}
			AppParam param = new AppParam();
			param.setService("storeListOptExtService");
			param.setMethod("queryStoreApplyList");
			param.addAttr("orderStatus", "0");
			param.addAttr("status", "2");//门店锁定
			RequestUtil.setAttr(param, request);
			StoreUserUtil.dealUserAuthParam(param, customerId, "lastStore");
			String searchKey = request.getParameter("searchKey");
			if(!StringUtils.isEmpty(searchKey)){
				if(ValidUtils.validateTelephone(searchKey)){//加快查询效率
					param.addAttr("telephone", searchKey);
					param.removeAttr("searchKey");
				}else{
					param.addAttr("applyName", searchKey);
					param.removeAttr("searchKey");
				}
			}
			String storeSearchKey = request.getParameter("storeSearchKey");
			if(!StringUtils.isEmpty(storeSearchKey)){
				if(ValidUtils.validateTelephone(storeSearchKey)){//加快查询效率
					param.addAttr("mobile", storeSearchKey);
					param.removeAttr("storeSearchKey");
				}else{
					param.addAttr("realName", storeSearchKey);
					param.removeAttr("storeSearchKey");
				}
			}
			param.setOrderBy("lastUpdateTime");
			param.setOrderValue("desc");
			param.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(param);
		} catch (Exception e) {
			LogerUtil.error(MyOrderAction.class, e, "queryWaitRecognized error");
			ExceptionUtil.setExceptionMessage(e, result,
					DuoduoSession.getShowLog());
		}
		return result;
	}
	/**
	 * 查询未了解总数
	 * @param request
	 * @return
	 */
	@RequestMapping("/waitRecognized/queryWaitRecognCount")
	@ResponseBody
	public AppResult queryWaitRecognCount(HttpServletRequest request) {
		AppResult result = new AppResult();
		try {
			String customerId = StoreUserUtil.getCustomerId(request);
			if (StringUtils.isEmpty(customerId)) {
				return CustomerUtil.retErrorMsg("用户ID不能为空");
			}
			AppParam param = new AppParam();
			param.setService("storeListOptExtService");
			param.setMethod("queryApplayCount");
			param.addAttr("orderStatus", "0");
			param.addAttr("status", "2");//门店锁定
			RequestUtil.setAttr(param, request);
			StoreUserUtil.dealUserAuthParam(param, customerId, "lastStore");
			param.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(param);
		} catch (Exception e) {
			LogerUtil.error(MyOrderAction.class, e, "queryWaitRecognCount error");
			ExceptionUtil.setExceptionMessage(e, result,
					DuoduoSession.getShowLog());
		}
		return result;
	}
	/**
	 * 查询进件项目
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("/contractItem/queryContractList")
	@ResponseBody
	public AppResult queryContractItemList(HttpServletRequest request) {
		AppResult result = new AppResult();
		try {
			String customerId = StoreUserUtil.getCustomerId(request);
			if (StringUtils.isEmpty(customerId)) {
				return CustomerUtil.retErrorMsg("用户ID不能为空");
			}
			AppParam param = new AppParam();
			param.setService("storeHandleExtService");
			param.setMethod("queryContractList");
			RequestUtil.setAttr(param, request);
			StoreUserUtil.dealUserAuthParam(param, customerId, "lastStore");
			String searchKey = request.getParameter("searchKey");
			if(!StringUtils.isEmpty(searchKey)){
				if(ValidUtils.validateTelephone(searchKey)){//加快查询效率
					param.addAttr("telephone", searchKey);
					param.removeAttr("searchKey");
				}else{
					param.addAttr("applyName", searchKey);
					param.removeAttr("searchKey");
				}
			}
			String storeSearchKey = request.getParameter("storeSearchKey");
			if(!StringUtils.isEmpty(storeSearchKey)){
				if(ValidUtils.validateTelephone(storeSearchKey)){//加快查询效率
					param.addAttr("mobile", storeSearchKey);
					param.removeAttr("storeSearchKey");
				}else{
					param.addAttr("realName", storeSearchKey);
					param.removeAttr("storeSearchKey");
				}
			}
			param.setOrderBy("t.approvalTime");
			param.setOrderValue("desc");
			param.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(param);
		} catch (Exception e) {
			LogerUtil.error(MyOrderAction.class, e, "queryContractList error");
			ExceptionUtil.setExceptionMessage(e, result,
					DuoduoSession.getShowLog());
		}
		return result;
	}
	
	
	/**
	 * 查询再分配列表
	 * @param request
	 * @return
	 */
	@RequestMapping("/againAllot/queryAgainAllot")
	@ResponseBody
	public AppResult queryAgainAllot(HttpServletRequest request) {
		AppResult result = new AppResult();
		try {
			String customerId = StoreUserUtil.getCustomerId(request);
			if (StringUtils.isEmpty(customerId)) {
				return CustomerUtil.retErrorMsg("用户ID不能为空");
			}
			AppParam param = new AppParam();
			param.setService("storeListOptExtService");
			param.setMethod("queryStoreApplyList");
			param.addAttr("orderStatus", "-1");
			param.addAttr("orderType", "2");//再分配
			param.addAttr("status", "2");//门店锁定中
			RequestUtil.setAttr(param, request);
			StoreUserUtil.dealUserAuthParam(param, customerId, "lastStore");
			String searchKey = request.getParameter("searchKey");
			if(!StringUtils.isEmpty(searchKey)){
				if(ValidUtils.validateTelephone(searchKey)){//加快查询效率
					param.addAttr("telephone", searchKey);
					param.removeAttr("searchKey");
				}else{
					param.addAttr("applyName", searchKey);
					param.removeAttr("searchKey");
				}
			}
			String storeSearchKey = request.getParameter("storeSearchKey");
			if(!StringUtils.isEmpty(storeSearchKey)){
				if(ValidUtils.validateTelephone(storeSearchKey)){//加快查询效率
					param.addAttr("mobile", storeSearchKey);
					param.removeAttr("storeSearchKey");
				}else{
					param.addAttr("realName", storeSearchKey);
					param.removeAttr("storeSearchKey");
				}
			}
			param.setOrderBy("applyTime");
			param.setOrderValue("desc");
			param.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(param);
		} catch (Exception e) {
			LogerUtil.error(MyOrderAction.class, e, "queryAgainAllot error");
			ExceptionUtil.setExceptionMessage(e, result,
					DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 查询再分配总数
	 * @param request
	 * @return
	 */
	@RequestMapping("/againAllot/queryAgainAllotCount")
	@ResponseBody
	public AppResult queryAgainAllotCount(HttpServletRequest request) {
		AppResult result = new AppResult();
		try {
			String customerId = StoreUserUtil.getCustomerId(request);
			if (StringUtils.isEmpty(customerId)) {
				return CustomerUtil.retErrorMsg("用户ID不能为空");
			}
			AppParam param = new AppParam();
			param.setService("storeListOptExtService");
			param.setMethod("queryApplayCount");
			param.addAttr("orderStatus", "-1");
			param.addAttr("orderType", "2");//再分配
			RequestUtil.setAttr(param, request);
			StoreUserUtil.dealUserAuthParam(param, customerId, "lastStore");
			param.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(param);
		} catch (Exception e) {
			LogerUtil.error(MyOrderAction.class, e, "queryAgainAllotCount error");
			ExceptionUtil.setExceptionMessage(e, result,
					DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 查询新申请总数
	 * @param request
	 * @return
	 */
	@RequestMapping("/waitDeal/queryNewApplayCount")
	@ResponseBody
	public AppResult queryNewApplayCount(HttpServletRequest request) {
		AppResult result = new AppResult();
		try {
			String customerId = StoreUserUtil.getCustomerId(request);
			if (StringUtils.isEmpty(customerId)) {
				return CustomerUtil.retErrorMsg("用户ID不能为空");
			}
			AppParam param = new AppParam();
			param.setService("storeListOptExtService");
			param.setMethod("queryApplayCount");
			param.addAttr("orderStatus", "-1");
			param.addAttr("orderType", "1");//新申请
			RequestUtil.setAttr(param, request);
			StoreUserUtil.dealUserAuthParam(param, customerId, "lastStore");
			param.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(param);
		} catch (Exception e) {
			LogerUtil.error(MyOrderAction.class, e, "queryNewApplayCount error");
			ExceptionUtil.setExceptionMessage(e, result,
					DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 查询新申请（未跟进）数据
	 * @param request
	 * @return
	 */
	@RequestMapping("/waitDeal/queryNewApplay")
	@ResponseBody
	public AppResult queryNewApplay(HttpServletRequest request) {
		AppResult result = new AppResult();
		try {
			String customerId = StoreUserUtil.getCustomerId(request);
			if (StringUtils.isEmpty(customerId)) {
				return CustomerUtil.retErrorMsg("用户ID不能为空");
			}
			AppParam param = new AppParam();
			param.setService("storeListOptExtService");
			param.setMethod("queryStoreApplyList");
			param.addAttr("orderStatus", "-1");
			param.addAttr("orderType", "1");//新单
			param.addAttr("status", "2");//门店锁定
			RequestUtil.setAttr(param, request);
			StoreUserUtil.dealUserAuthParam(param, customerId, "lastStore");
			String searchKey = request.getParameter("searchKey");
			if(!StringUtils.isEmpty(searchKey)){
				if(ValidUtils.validateTelephone(searchKey)){//加快查询效率
					param.addAttr("telephone", searchKey);
					param.removeAttr("searchKey");
				}else{
					param.addAttr("applyName", searchKey);
					param.removeAttr("searchKey");
				}
			}
			String storeSearchKey = request.getParameter("storeSearchKey");
			if(!StringUtils.isEmpty(storeSearchKey)){
				if(ValidUtils.validateTelephone(storeSearchKey)){//加快查询效率
					param.addAttr("mobile", storeSearchKey);
					param.removeAttr("storeSearchKey");
				}else{
					param.addAttr("realName", storeSearchKey);
					param.removeAttr("storeSearchKey");
				}
			}
			param.setOrderBy("applyTime");
			param.setOrderValue("desc");
			param.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(param);
		} catch (Exception e) {
			LogerUtil.error(MyOrderAction.class, e, "queryNewApplay error");
			ExceptionUtil.setExceptionMessage(e, result,
					DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 查询CFS签单列表
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("/cfsSign/queryCfsSignList")
	@ResponseBody
	public AppResult queryCfsSignList(HttpServletRequest request) {
		AppResult result = new AppResult();
		try {
			String customerId = StoreUserUtil.getCustomerId(request);
			if (StringUtils.isEmpty(customerId)) {
				return CustomerUtil.retErrorMsg("用户ID不能为空");
			}
			AppParam param = new AppParam("storeListOptExtService", "queryCfsSignList");			
			RequestUtil.setAttr(param, request);
			StoreUserUtil.dealUserAuthParam(param, customerId, "customerId");
			String searchKey = request.getParameter("searchKey");
			if(!StringUtils.isEmpty(searchKey)){
				if(ValidUtils.validateTelephone(searchKey)){//加快查询效率
					param.addAttr("telephone", searchKey);
					param.removeAttr("searchKey");
				}else{
					param.addAttr("applyName", searchKey);
					param.removeAttr("searchKey");
				}
			}
			String storeSearchKey = request.getParameter("storeSearchKey");
			if(!StringUtils.isEmpty(storeSearchKey)){
				if(ValidUtils.validateTelephone(storeSearchKey)){//加快查询效率
					param.addAttr("mobile", storeSearchKey);
					param.removeAttr("storeSearchKey");
				}else{
					param.addAttr("customerName", storeSearchKey);
					param.removeAttr("storeSearchKey");
				}
			}
			param.setOrderBy("queryDate");
			param.setOrderValue("desc");
			param.addAttr("isNet", 1); // 1 网销
			param.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(param);
		} catch (Exception e) {
			LogerUtil.error(MyOrderAction.class, e, "queryCfsSignList error");
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
	@RequestMapping("/allOrder/checkTransOtherXDJL")
	@ResponseBody
	public AppResult checkTransOtherXDJL(HttpServletRequest request) {
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
			AppParam applyParam = new AppParam();
			applyParam.addAttr("orders", orders);
			applyParam.addAttr("orgId", orgId);
			applyParam.addAttr("custId", custId);
			applyParam.addAttr("excOrderFlag", request.getParameter("excOrderFlag"));
			applyParam.addAttr("customerId", customerId);
			applyParam.setService("storeOptExtService");
			applyParam.setMethod("transOtherXDJL");
			applyParam.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().call(applyParam);
			if(result.isSuccess()){
				StringBuffer strBuffer = new StringBuffer();
				strBuffer.append("总共:").append(orders.size()).append("笔,成功:");
				strBuffer.append(result.getAttr("sucSize")).append("笔,失败");
				strBuffer.append(result.getAttr("failSize")).append("笔");
				if(!StringUtils.isEmpty(result.getAttr("failDesc"))){
					strBuffer.append(",").append(result.getAttr("failDesc"));
				}
				result.setMessage(strBuffer.toString());	
			}
		} catch (Exception e) {
			LogerUtil.error(MyOrderAction.class, e, "transOtherXDJL error");
			ExceptionUtil.setExceptionMessage(e, result,
					DuoduoSession.getShowLog());
		}
		return result;
	}
	
	
	/**
	 * 获取系统菜单对应权限
	 * @param request
	 * @return
	 */
	@RequestMapping("querySysMenuRole")
	@ResponseBody
	public AppResult querySysMenuRole(HttpServletRequest request){
		AppResult result = new AppResult();
		String menuId = request.getParameter("menuId");
		if(StringUtils.isEmpty(menuId)){
			result.setMessage("菜单ID不能为空！");
			result.setSuccess(false);
			return result;
		}
		String roleId = request.getParameter("roleId");
		if(StringUtils.isEmpty(roleId)){
			result.setMessage("用户权限不能为空！");
			result.setSuccess(false);
			return result;
		}
		try{	
			AppParam param = new AppParam();
			param.setService("sysRoleMenuService");
			param.setMethod("query");
			param.addAttr("menuId", menuId);
			param.addAttr("roleId", roleId);
			param.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_cust));
			result = RemoteInvoke.getInstance().call(param);
		}catch(Exception e){
			LogerUtil.error(this.getClass(), e, "querySysMenuRole");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 查询还款提醒列表
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("/storeRepayRemind/queryRepayRemindList")
	@ResponseBody
	public AppResult queryRepayRemindList(HttpServletRequest request) {
		AppResult result = new AppResult();
		try {
			String customerId = StoreUserUtil.getCustomerId(request);
			if (StringUtils.isEmpty(customerId)) {
				return CustomerUtil.retErrorMsg("用户ID不能为空");
			}
			AppParam param = new AppParam();
			param.setService("storeRepayRemindService");
			param.setMethod("queryRepayRemindList");
			RequestUtil.setAttr(param, request);
			StoreUserUtil.dealUserAuthParam(param, customerId, "customerId");
			String searchKey = request.getParameter("searchKey");
			if(!StringUtils.isEmpty(searchKey)){
				if(ValidUtils.validateTelephone(searchKey)){//加快查询效率
					param.addAttr("telephone", searchKey);
					param.removeAttr("searchKey");
				}else{
					param.addAttr("applyName", searchKey);
					param.removeAttr("searchKey");
				}
			}
			String storeSearchKey = request.getParameter("storeSearchKey");
			if(!StringUtils.isEmpty(storeSearchKey)){
				if(ValidUtils.validateTelephone(storeSearchKey)){//加快查询效率
					param.addAttr("mobile", storeSearchKey);
					param.removeAttr("storeSearchKey");
				}else{
					param.addAttr("realName", storeSearchKey);
					param.removeAttr("storeSearchKey");
				}
			}
			param.setOrderBy("t.createTime");
			param.setOrderValue("desc");
			param.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(param);
		} catch (Exception e) {
			LogerUtil.error(MyOrderAction.class, e, "queryRepayRemindList error");
			ExceptionUtil.setExceptionMessage(e, result,
					DuoduoSession.getShowLog());
		}
		return result;
	}
}
