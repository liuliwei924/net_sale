package org.xxjr.store.web.action.account.spread;

import javax.servlet.http.HttpServletRequest;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.service.RemoteInvoke;
import org.ddq.common.exception.ExceptionUtil;
import org.ddq.common.util.LogerUtil;
import org.ddq.common.web.session.DuoduoSession;
import org.ddq.common.web.session.RequestUtil;
import org.llw.model.cache.RedisUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.xxjr.busi.util.kf.KfUserUtil;
import org.xxjr.sys.util.ServiceKey;

@Controller
@RequestMapping("/account/spread/channelCode/")
public class ChannelCodeAction {

	/***
	 * 查询渠道大类列表
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
			params.setService("borrowChannelService");
			params.setMethod("queryByPage");
			params.setOrderBy("createTime");
			params.setOrderValue("desc");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().call(params);	
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "queryList error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/***
	 * 添加渠道大类信息
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
			String customerId = KfUserUtil.getCustomerId(request);
			params.addAttr("customerId", customerId);
			params.setService("borrowChannelService");
			params.setMethod("insert");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().call(params);
			if(result.isSuccess()){
				RedisUtils.getRedisService().del(KfUserUtil.KF_USER_RIGHT+customerId);
			}
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "add channel error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/***
	 * 修改渠道详细信息
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
			params.setService("borrowChannelService");
			params.setMethod("update");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().call(params);	
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "edit channel error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/***
	 * 删除渠道
	 * 传 channelCode
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
			params.setService("borrowChannelService");
			params.setMethod("delete");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().call(params);	
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "delete channel error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
}
