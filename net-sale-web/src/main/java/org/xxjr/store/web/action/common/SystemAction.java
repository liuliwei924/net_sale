package org.xxjr.store.web.action.common;

import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.service.RemoteInvoke;
import org.ddq.common.exception.ExceptionUtil;
import org.ddq.common.util.LogerUtil;
import org.ddq.common.web.session.DuoduoSession;
import org.llw.common.web.util.QRCodeUtils;
import org.llw.model.cache.RedisUtils;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.xxjr.busi.util.store.StoreUserUtil;
import org.xxjr.cust.util.info.CustomerUtil;
import org.xxjr.sys.util.ServiceKey;
import org.xxjr.sys.util.SysParamsUtil;

import lombok.extern.slf4j.Slf4j;


/***
 * 初始化页面使用
 * @author qinxcb
 *
 */
@Controller
@RequestMapping("/sysAction")
@Slf4j
public class SystemAction{
	
	/**
	 * sessionId
	 */
	@RequestMapping("/sessionIdDeal")
	@ResponseBody
	public AppResult sessionIdDeal(HttpServletRequest request, HttpServletResponse response) throws IOException {
		AppResult result =  new AppResult();
		String sessionId = request.getSession().getId();
		RedisUtils.getRedisService().set(sessionId +"scanLogin", sessionId,5*60);
		response.setHeader("signId", sessionId);
		if(AppProperties.isDebug()){
			result.putAttr("url","http://192.168.10.196/store/sysAction/scanLogin");
		}else{
			result.putAttr("url","https://kefu.xxjr.com/store/sysAction/scanLogin");
		}
		result.putAttr("signId", sessionId);
		return result;
	}
	
	/**
	 * 门店登录二维码
	 * @param id
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	@RequestMapping("/scanLogin")
	public void scanLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
		ServletOutputStream ot = response.getOutputStream();
		String sessionId = request.getParameter("sessionId");
		String url = "";
		if(AppProperties.isDebug()){
			url = "http://192.168.10.196/store/sysAction/scanDeal/"+ sessionId;
		}else{
			url = "https://kefu.xxjr.com/store/sysAction/scanDeal/" + sessionId;
		}
		RedisUtils.getRedisService().set(sessionId +"scanLogin", sessionId,5*60);
		String logoUrl = "classpath:/config/logo.jpg";
		log.info("logoUrl:" + logoUrl);
		QRCodeUtils.generateQRCode(ot, url, logoUrl, 300);
		response.setHeader("signId", sessionId);
		ot.flush();
		ot.close();
	}

	/**
	 * 扫描处理
	 */
	@RequestMapping("/scanDeal/{key}")
	@ResponseBody
	public AppResult scanDeal(@PathVariable("key") String sessionId) throws IOException {
		AppResult result =  new AppResult();
		String pcSessionId = (String) RedisUtils.getRedisService().get(sessionId +"scanLogin");
		if(StringUtils.isEmpty(pcSessionId) || !sessionId.equals(pcSessionId)){
			result.setMessage("扫码失败,sessionId为空或已失效,请刷新界面重试");
			result.setSuccess(Boolean.FALSE);
			return result;
		}
		
		//返回给APP
		result.putAttr("message" , "扫码成功,请确认登录");
		result.putAttr("sessionId" , sessionId);
		return result;
	}
	
	/**
	 * 确认登录
	 */
	@RequestMapping("/confLogin")
	public String confLogin(HttpServletRequest request) throws IOException {
		String sessionId = request.getParameter("sessionId");
		String uuid =  request.getParameter("UUID");
		String appSignId = request.getParameter("signId");
		String pcSessionId = (String) RedisUtils.getRedisService().get(sessionId +"scanLogin");
		if(StringUtils.isEmpty(pcSessionId) || !sessionId.equals(pcSessionId)){
			String url = "https://kefu.xxjr.com"  + "/wxInfo?errorCode=1&project=" + 1;
			return "redirect:" + url;
		}else if(StringUtils.isEmpty(uuid)){
			String url = "https://kefu.xxjr.com" + "/wxInfo?errorCode=1&project=" + 1;
			return "redirect:" + url;
		}
		
		//绑定app与pc端的关系
		RedisUtils.getRedisService().set(StoreUserUtil.APP_PC_CON + sessionId,uuid,30*60);
		RedisUtils.getRedisService().set(StoreUserUtil.APP_PC_CON + uuid, sessionId,30*60);
		String url = "";
		if(AppProperties.isDebug()){
			url = "http://192.168.10.196/";
		}else{
			url = "https://kefu.xxjr.com/";
		}
		//进行跳转
		return "redirect:" + url+"store/user/scanLogin?sessionId="+ sessionId+"&appSignId="+appSignId;
	}
	
	
	/**
	 * 小小云APP下载二维码
	 * @param id
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	@RequestMapping("/downloadXxyunApp")
	public void downloadXxyunApp(HttpServletRequest request, HttpServletResponse response) throws IOException {
		ServletOutputStream ot = response.getOutputStream();
		String minVersion =SysParamsUtil.getStringParamByKey("xxyunVersion","xxyun1.0.1").substring("xxyun".length());
		String url = "https://static.xxjr.com/xxyun/"+"com.xxjr.xxyun."+minVersion+".apk";
		String logoUrl = "classpath:/config/logo.jpg";
		QRCodeUtils.generateQRCode(ot, url, logoUrl, 300);
		ot.flush();
		ot.close();
	}
	
	/**
	 * ACR下载二维码
	 * @param id
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	@RequestMapping("/downloadACRApp")
	public void downloadACRApp(HttpServletRequest request, HttpServletResponse response) throws IOException {
		ServletOutputStream ot = response.getOutputStream();
		String minVersion =SysParamsUtil.getStringParamByKey("acrVersion","acr29").substring("acr".length());
		String url = "https://static.xxjr.com/xxyun/"+"ACR"+minVersion+".apk";
		String logoUrl = "classpath:/config/logo.jpg";
		QRCodeUtils.generateQRCode(ot, url, logoUrl, 300);
		ot.flush();
		ot.close();
	}
	
	/**
	 * 查询门店渠道类型开关
	 */
	@RequestMapping("/queryChannelSwitch")
	@ResponseBody
	public AppResult queryChannelSwitch(HttpServletRequest request){
		AppResult result =  new AppResult();
		try {
			//开启渠道类型标识
			int storeChannelFlag = SysParamsUtil.getIntParamByKey("openStoreChannelFlag", 0);
			result.putAttr("storeChannelFlag", storeChannelFlag);
		}catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "queryChannelSwitch error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 修改门店渠道类型开关
	 */
	@RequestMapping("/updateChannelSwitch")
	@ResponseBody
	public AppResult updateChannelSwitch(HttpServletRequest request){
		AppResult result =  new AppResult();
		try {
			String storeChannelFlag = request.getParameter("storeChannelFlag");
			if (StringUtils.isEmpty(storeChannelFlag)) {
				return CustomerUtil.retErrorMsg("开启渠道类型标识不能为空");
			}
			AppParam param = new AppParam("sysParamsService", "update");
			param.addAttr("paramCode", "openStoreChannelFlag");
			param.addAttr("paramValue", storeChannelFlag);
			param.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_cust));
			result = RemoteInvoke.getInstance().call(param);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "updateChannelSwitch error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/***
	 * 根据key及 删除缓存
	 * @return
	 */
	@RequestMapping("/delKey")
	@ResponseBody
	public AppResult delKey(HttpServletRequest request){
		AppResult result = new AppResult();
		try{
			String storeKey = request.getParameter("storeKey");
			String keyVal = request.getParameter("keyVal");
			if(StringUtils.isEmpty(storeKey)){
				result.setSuccess(false);
				result.setMessage("storeKey不能为空");
				return result;
			}
			if(StringUtils.isEmpty(keyVal)){
				result.setSuccess(false);
				result.setMessage("keyVal不能为空");
				return result;
			}
			
			String []applyIds = keyVal.split(",");
			for(String applyId: applyIds){
				// 删除缓存
				String delKey = storeKey + applyId; 
				RedisUtils.getRedisService().del(delKey);
			}
		}catch(Exception e){
			log.error("删除缓存失败");
		}
		return result;
	}
	
}