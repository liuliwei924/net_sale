package org.xxjr.store.web.action.system;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import org.ddq.common.context.AppResult;
import org.ddq.common.exception.ExceptionUtil;
import org.ddq.common.util.LogerUtil;
import org.ddq.common.util.StringUtil;
import org.ddq.common.web.session.DuoduoSession;
import org.llw.common.web.util.IdentifyUtil;
import org.llw.model.cache.RedisUtils;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.xxjr.cust.util.info.CustomerUtil;
import org.xxjr.sms.SmsTenXunSendUtil;
import org.xxjr.store.web.util.Key_SMS;
import org.xxjr.sys.util.SysParamsUtil;
import org.xxjr.sys.util.ValidUtils;


@Controller
@RequestMapping("/smsAction")
public class SmsSendAction {
	
	/**
	 * 不需要用户登录的短信发送
	 * @param request
	 * @return
	 */
	@RequestMapping("/nologin/{type}")
	@ResponseBody
	public AppResult sendNotLoginSms(@PathVariable String type,HttpServletRequest request){
		AppResult context = new AppResult();
		try{
			String telephone = request.getParameter("telephone");
			String key = null;
			if(StringUtils.isEmpty(telephone)){
				context.setSuccess(Boolean.FALSE);
				context.setMessage("请先输入手机号码！");
				return context;
			}
			if(type.equals("kjlogin")){
				String customerId = CustomerUtil.queryCustId(telephone, null);
				if(StringUtils.isEmpty(customerId)){
					context.setSuccess(Boolean.FALSE);
					context.setMessage("系统中没有相应的手机号码！");
					return context;
				}
				key = SysParamsUtil.getParamByKey(Key_SMS.Key_SMS_STORE_KJ_LOGIN, true)+telephone;
			}else{
				context.setSuccess(Boolean.FALSE);
				context.setMessage("当前参数不正确");
				return context;
			}
			context = sendSms(key, type, telephone, null);
			if(context.isSuccess()){
				context.setMessage("手机动态码已经发送到:"+StringUtil.getHideTelphone(telephone));
			}
		}catch(Throwable e){
			LogerUtil.error(SmsSendAction.class, e, "send sms error!");
			ExceptionUtil.setExceptionMessage(e, context, DuoduoSession.getShowLog());
		}
		return context;
	}
	
	/**
	 * 不需要用户登录的短信验证
	 * @param request
	 * @return
	 */
	@RequestMapping("/valid/nologin/{type}")
	@ResponseBody
	public AppResult nologinValid(@PathVariable String type,HttpServletRequest request){
		AppResult context = new AppResult();
		try{
			String telephone = request.getParameter("telephone");
			String smsNo = request.getParameter("smsNo");
			String key = null;
			if(type.equals("login")){
				key = Key_SMS.Key_SMS_STORE_KJ_LOGIN;
			}else{
				context.setSuccess(Boolean.FALSE);
				context.setMessage("您请求的参数不正确");
				return context;
			}
			if (!ValidUtils.validateRandomNo(smsNo, key, telephone)) {
				context.setSuccess(Boolean.FALSE);
				context.setMessage("当前手机动态码不正确");
			}
		}catch(Throwable e){
			LogerUtil.error(SmsSendAction.class, e, "nologinValid sms error!");
			ExceptionUtil.setExceptionMessage(e, context, DuoduoSession.getShowLog());
		}
		return context;
	}
	
	/***
	 * 发送短信处理
	 * @param key
	 * @param type
	 * @param telephone
	 */
	public AppResult sendSms(String key,String type,String telephone,String isVoice){

		AppResult result = new AppResult();
 		if (!SysParamsUtil.getBoleanByKey(SysParamsUtil.KEY_sendStatus, false)) {// 不发送短信验证码
			RedisUtils.getRedisService().set(
					key,
					"4321",
					SysParamsUtil.getIntParamByKey(
							SysParamsUtil.Key_SMS_CACHE_TIME, 300));
			return result;
		}
		String random = null;
		random = (String) RedisUtils.getRedisService().get(key);
		if (random == null) {
			random = IdentifyUtil.getRandNum(6);
			RedisUtils.getRedisService().set(key, random,SysParamsUtil.getIntParamByKey(SysParamsUtil.Key_SMS_CACHE_TIME, 300));
		}
		//小小金融
		ArrayList<String> params = new ArrayList<String>();
    	params.add(random);
		return SmsTenXunSendUtil.sendTplSms(
				SysParamsUtil.getIntParamByKey("TX_SMS_APPID", 1400023280) , 
				SysParamsUtil.getStringParamByKey("TX_SMS_APPKEY",  "c63653ba04c29fc0826c1ec8a5af6e5d"), "小小金融", telephone, 
				SysParamsUtil.getStringParamByKey("TXxmsTemplate", "146548"), params);
	}
	
}
