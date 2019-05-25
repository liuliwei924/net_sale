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
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.xxjr.busi.util.StoreSeparateUtils;
import org.xxjr.sys.util.ServiceKey;
/***
 * 分单配置相关
 * @author ZQH
 *
 */
@Controller
@RequestMapping("/account/config/sepaconf/")
public class StoreSeparateAction {

	/***
	 * 获取分单配置信息
	 * @param request
	 * @return
	 */
	@RequestMapping("querySepaInfo")
	@ResponseBody
	public AppResult querySepaInfo(HttpServletRequest request){
		AppResult result = new AppResult();
		try{
			result.putAttr("baseConfig", StoreSeparateUtils.getBaseConfig());
			result.putAttr("orderConfig", StoreSeparateUtils.getOrderConfig());
			result.putAttr("abilityConfig", StoreSeparateUtils.getAbilityConfig());
		
		}catch(Exception e){
			LogerUtil.error(StoreSeparateAction.class, e, "querySepaInfo error");
			ExceptionUtil.setExceptionMessage(e, result,DuoduoSession.getShowLog());
		}
		
		return result;
	}
	
	/***
	 * 修改全局配置
	 * @param request
	 * @return
	 */
	@RequestMapping("updateBaseInfo")
	@ResponseBody
	public AppResult updateBaseInfo(HttpServletRequest request){
		AppResult result = new AppResult();
		try{
			String allotCitys = request.getParameter("allotCitys");
			if (StringUtils.isEmpty(allotCitys)) {
				result.setSuccess(false);
				result.setMessage("需要分单的城市不能为空");
				return result;
			}
			if(allotCitys.contains("，")){
				result.setSuccess(false);
				result.setMessage("需要分单的城市中包含中文逗号，请修改成英文逗号！");
				return result;
			}
			String allotPrice = request.getParameter("allotPrice");
			if (StringUtils.isEmpty(allotPrice)) {
				result.setSuccess(false);
				result.setMessage("新单分配成本不能为空");
				return result;
			}
			
			AppParam params = new AppParam("baseCfgService","update");
			RequestUtil.setAttr(params, request);
			params.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START+ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().call(params);
			if(result.isSuccess()){
				RedisUtils.getRedisService().del(StoreSeparateUtils.STORE_BASE_CONFIG_KEY);
			}
			
		}catch(Exception e){
			LogerUtil.error(StoreSeparateAction.class, e, "updateBaseInfo error");
			ExceptionUtil.setExceptionMessage(e, result,DuoduoSession.getShowLog());
		}
		
		return result;
	}
	
	
	/***
	 * 修改分单情况配置
	 * @param request
	 * @return
	 */
	@RequestMapping("updateOrderInfo")
	@ResponseBody
	public AppResult updateOrderInfo(HttpServletRequest request){
		AppResult result = new AppResult();
		try{
			AppParam params = new AppParam("transOrderCfgService","update");
			RequestUtil.setAttr(params, request);
			params.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START+ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().call(params);
			if(result.isSuccess()){
				RedisUtils.getRedisService().del(StoreSeparateUtils.STORE_ORDER_CONFIG_KEY);
			}
			
		}catch(Exception e){
			LogerUtil.error(StoreSeparateAction.class, e, "updateOrderInfo error");
			ExceptionUtil.setExceptionMessage(e, result,DuoduoSession.getShowLog());
		}
		
		return result;
	}
	
	
	/***
	 * 修改能力值情况配置
	 * @param request
	 * @return
	 */
	@RequestMapping("updateAbilityInfo")
	@ResponseBody
	public AppResult updateAbilityInfo(HttpServletRequest request){
		AppResult result = new AppResult();
		try{
			AppParam params = new AppParam("abilityValueCfgService","update");
			RequestUtil.setAttr(params, request);
			params.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START+ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().call(params);
			if(result.isSuccess()){
				RedisUtils.getRedisService().del(StoreSeparateUtils.STORE_ABILITY_CONFIG_KEY);
			}
			
		}catch(Exception e){
			LogerUtil.error(StoreSeparateAction.class, e, "updateAbilityInfo error");
			ExceptionUtil.setExceptionMessage(e, result,DuoduoSession.getShowLog());
		}
		
		return result;
	}
	
}
