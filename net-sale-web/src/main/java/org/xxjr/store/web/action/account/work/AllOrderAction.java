package org.xxjr.store.web.action.account.work;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.exception.ExceptionUtil;
import org.ddq.common.exception.SysException;
import org.ddq.common.util.LogerUtil;
import org.ddq.common.web.session.DuoduoSession;
import org.ddq.common.web.session.RequestUtil;
import org.ddq.common.core.service.RemoteInvoke;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xxjr.busi.util.store.StoreUserUtil;
import org.xxjr.store.web.action.BaseController;
import org.xxjr.sys.util.ServiceKey;
import org.xxjr.sys.util.ValidUtils;

@RestController
@RequestMapping("/account/work/")
/**
 * 所有
 * @author Administrator
 *
 */
public class AllOrderAction extends BaseController {

	/**
	 * 查询待分配处理订单列表
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("waitAssign/queryAssignList")
	public AppResult queryAssignList() {
		AppResult result = new AppResult();
		try {
			String orderFlag = request.getParameter("orderFlag");
			if(StringUtils.isEmpty(orderFlag)){
				throw new SysException("单子类型不能为空");
			}
			AppParam param = new AppParam("storeListOptExtService", "queryAssignOrder");
			RequestUtil.setAttr(param, request);
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
			param.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			param.setOrderBy("applyTime");
			param.setOrderValue("desc");
			result = RemoteInvoke.getInstance().callNoTx(param);
			return result;
		} catch (Exception e) {
			LogerUtil.error(AllOrderAction.class, e, "queryAssignOrder error");
			ExceptionUtil.setExceptionMessage(e, result,
					DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 查询转小小金融订单列表
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("transferXxjr/queryTransXxjr")
	public AppResult queryTransXxjr() {
		AppResult result = new AppResult();
		try {
			AppParam param = new AppParam("storeListOptExtService", "queryTransXxjr");
			RequestUtil.setAttr(param, request);
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
			param.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			param.setOrderBy("applyTime");
			param.setOrderValue("desc");
			result = RemoteInvoke.getInstance().callNoTx(param);
			return result;
		} catch (Exception e) {
			LogerUtil.error(AllOrderAction.class, e, "queryTransXxjr error");
			ExceptionUtil.setExceptionMessage(e, result,
					DuoduoSession.getShowLog());
		}
		return result;
	}
	
	
	/**
	 *  查看门店人员接单记录
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("storeRecord/queryStoreRecRecord")
	public AppResult queryStoreRecRecord() {
		AppResult result = new AppResult();
		String customerId = StoreUserUtil.getCustomerId(request);
		String applyName = request.getParameter("applyName");
		if (StringUtils.isEmpty(customerId)) {
			throw new SysException("用户ID不能为空");
		}
		try {
			AppParam param = new AppParam("borrowStoreRecordService", "queryStoreRecRecord");
			RequestUtil.setAttr(param, request);
			StoreUserUtil.dealUserAuthParam(param, customerId, "customerId");
			
			if(ValidUtils.validateTelephone(applyName)){//加快查询效率
				param.addAttr("telephone", param.removeAttr("applyName"));
			}
		
			param.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(param);
		} catch (Exception e) {
			LogerUtil.error(AllOrderAction.class, e, "queryStoreRecRecord error");
			ExceptionUtil.setExceptionMessage(e, result,
					DuoduoSession.getShowLog());
		}
		return result;
	}
}
