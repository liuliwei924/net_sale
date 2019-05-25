package org.xxjr.store.web.action.account.config;

import javax.servlet.http.HttpServletRequest;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.exception.ExceptionUtil;
import org.ddq.common.util.LogerUtil;
import org.ddq.common.web.session.DuoduoSession;
import org.ddq.common.web.session.RequestUtil;
import org.llw.model.cache.RedisUtils;
import org.ddq.common.core.service.RemoteInvoke;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.xxjr.busi.util.StoreAgainCfgUtils;
import org.xxjr.sys.util.ServiceKey;

/**
 * 再分配设置相关
 * @author zenghw
 *
 */
@Controller
@RequestMapping("/account/config/againConf/")
public class StoreAgainCfgAction {
	
	@RequestMapping("queryAgainInfo")
	@ResponseBody
	public AppResult queryAgainInfo(HttpServletRequest request){
		AppResult result = new AppResult();
		try {
			result.putAttr("newAllot", StoreAgainCfgUtils.getNewAllotCfg());
			result.putAttr("againAllot", StoreAgainCfgUtils.getAgainAllotCfg());
		} catch (Exception e) {
			LogerUtil.error(StoreSeparateAction.class, e, "queryAgainInfo error");
			ExceptionUtil.setExceptionMessage(e, result,DuoduoSession.getShowLog());
		}
		return result;
	}
	
	
	@RequestMapping("updateNewAlltoInfo")
	@ResponseBody
	public AppResult updateNewAlltoInfo(HttpServletRequest request){
		AppResult result = new AppResult();
		try{
			AppParam params = new AppParam("borrowAgainCfgService","update");
			RequestUtil.setAttr(params, request);
			params.addAttr("againType", 0);
			params.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START+ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().call(params);
			if(result.isSuccess()){
				RedisUtils.getRedisService().del(StoreAgainCfgUtils.STORE_NEW_ALLOT_CFGKEY);
			}
			
		}catch(Exception e){
			LogerUtil.error(StoreSeparateAction.class, e, "updateNewAlltoInfo error");
			ExceptionUtil.setExceptionMessage(e, result,DuoduoSession.getShowLog());
		}
		
		return result;
	}
	
	
	@RequestMapping("updateAgainAlltoInfo")
	@ResponseBody
	public AppResult updateAgainAlltoInfo(HttpServletRequest request){
		AppResult result = new AppResult();
		try{
			AppParam params = new AppParam("borrowAgainCfgService","update");
			RequestUtil.setAttr(params, request);
			params.addAttr("againType", 1);
			params.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START+ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().call(params);
			if(result.isSuccess()){
				RedisUtils.getRedisService().del(StoreAgainCfgUtils.STORE_AGAIN_ALLOT_CFGKEY);
			}
			
		}catch(Exception e){
			LogerUtil.error(StoreSeparateAction.class, e, "updateAgainAlltoInfo error");
			ExceptionUtil.setExceptionMessage(e, result,DuoduoSession.getShowLog());
		}
		
		return result;
	}
}
