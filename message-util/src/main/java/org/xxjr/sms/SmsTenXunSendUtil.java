package org.xxjr.sms;

import java.util.ArrayList;

import org.ddq.common.context.AppResult;
import org.ddq.common.util.LogerUtil;

import com.github.qcloudsms.SmsSingleSender;
import com.github.qcloudsms.SmsSingleSenderResult;


/***
 * 腾讯发送短信
 * 
 * @author Administrator
 *
 */
public class SmsTenXunSendUtil {

	/**
	 * 发送短信的基本方法
	 * 
	 * @param appid
	 *            发短信的第三方平台（腾讯云）appid
	 * @param appkey
	 *            发短信的第三方平台（腾讯云）appkey
	 * @param signName
	 *            签名如（小小金融，小小交租）
	 * @param telephone
	 *            手机号
	 * @param tempId
	 *            模板ID
	 * @param params
	 *            其他参数
	 * @return
	 */
	public static AppResult sendTplSms(int appid, String appkey, String signName, String telephone, String tempId,
			ArrayList<String> params) {
		AppResult result = new AppResult();

		try {
			int tmplId = Integer.parseInt(tempId);
			// 初始化单发
			SmsSingleSender singleSender = new SmsSingleSender(appid, appkey);
			// 指定模板单发
			// 假设短信模板内容为：测试短信，{1}，{2}，{3}，上学。
			SmsSingleSenderResult singleSenderResult = singleSender.sendWithParam("86", telephone, tmplId, params,
					signName, "", "");
			int ret = singleSenderResult.result;
			if (ret == 0) {
				return result;
			}
			result.setSuccess(Boolean.FALSE);
			result.setMessage(singleSenderResult.errMsg);
			// 异常记录错误码和错误信息
			LogerUtil.error(SmsTenXunSendUtil.class, "Send SMS error: telephone=" + telephone + " errorCode="
					+ singleSenderResult.result + " messsage:" + singleSenderResult.errMsg);
		} catch (Exception e) {
			LogerUtil.error(SmsTenXunSendUtil.class, e, "Send SMS error: telephone=" + telephone);
			result.setSuccess(Boolean.FALSE);
			result.setMessage("验证码发送失败，请重试");
		}
		return result;
	}

	
	public static void main(String[] args) {
		String telepgone ="18670787211";
		int appid =1400218540;
		String appkey = "11f744b94e50666be11b89198668be8f";
		String signName = "orzizro";
		
		ArrayList<String> params = new ArrayList<String>(1);
    	params.add("123456");
    	
    	AppResult result = sendTplSms(appid,appkey,signName,telepgone,"351310",params);
    	
    	System.out.println(result.toJson());
	}
}
