package org.xxjr.store.util;

import java.util.HashMap;
import java.util.Map;

import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.ddq.common.util.JsonUtil;
import org.ddq.common.util.LogerUtil;
import org.ddq.common.util.StringUtil;
import org.springframework.util.StringUtils;
import org.xxjr.sys.util.HttpClientUtil;
import org.xxjr.sys.util.NumberUtil;

/***
 * 优分多头借贷工具类
 * @author loys
 *
 */
public class AcedataUtil {
	private static final String aceAccount = "xiaoxiao163";
	private static final String accessName = "/oreo/personal/creditInfo?";
	private static final String aceUrl = "https://api.acedata.com.cn:2443";

	/***
	 * 获取优分查询数据
	 * @param params
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static  AppResult getAceData(AppParam params){
		AppResult result = new AppResult();
		String telephone = StringUtil.getString(params.getAttr("telephone"));
		if(StringUtils.isEmpty(telephone)){
			result.setSuccess(false);
			result.setMessage("手机号不能为空");
			LogerUtil.log("手机号为空");
			return result;
		}
		Map<String, Object> returnMap = new HashMap<String, Object>();
		StringBuffer httpUrl = new StringBuffer();
		httpUrl.append(aceUrl).append(accessName)
		.append("account=").append(aceAccount)
		.append("&cellphone=").append(telephone).append("&cycle=6")
		.append("&type=S001,S003,S005,S006,S007,S009,S010");
		try{
			// 解决中文乱码问题
			String resString =  HttpClientUtil.getInstance().sendHttpGet(httpUrl.toString());
			LogerUtil.log("优分多头借贷返回结果:" + resString);
			returnMap = JsonUtil.getInstance().json2Object(resString, Map.class);
			String resCode = StringUtil.getString(returnMap.get("resCode"));
			String resMsg = StringUtil.getString(returnMap.get("resMsg"));
			if("0000".equals(resCode)){
				String data = StringUtil.getString(returnMap.get("data"));
				//存在数据
				if(!StringUtils.isEmpty(data)){
					result.putAttr("jsonText", resString);
					Map<String,Object> dataMap = JsonUtil.getInstance().json2Object(data, Map.class);
					if(dataMap != null){
						String statusCode = StringUtil.getString(dataMap.get("statusCode"));
						if("2012".equals(statusCode)){
							result.setMessage("命中多头借贷");
							String dataResult = StringUtil.getString(dataMap.get("result"));
							if(!StringUtils.isEmpty(dataResult)){
								Map<String,Object> resultMap = JsonUtil.getInstance().json2Object(dataResult, Map.class);
								String s003Str = StringUtil.getString(resultMap.get("S003")); // 申请次数
								if(!StringUtils.isEmpty(s003Str)){
									Map<String,Object> s003Map = JsonUtil.getInstance().json2Object(s003Str, Map.class);
									String respcode = StringUtil.getString(s003Map.get("code"));
									if("200".equals(respcode)){
										int loanNum = NumberUtil.getInt(s003Map.get("loanNum"),0);
										LogerUtil.log("180天命中多头借贷申请次数:" + loanNum + "次");
									}
								}
								String s006Str = StringUtil.getString(resultMap.get("S006")); // 放款次数
								if(!StringUtils.isEmpty(s006Str)){
									Map<String,Object> s006Map = JsonUtil.getInstance().json2Object(s006Str, Map.class);
									String respcode = StringUtil.getString(s006Map.get("code"));
									if("200".equals(respcode)){
										int loanLendersNum = NumberUtil.getInt(s006Map.get("loanLendersNum"),0);
										LogerUtil.log("180天命中多头借贷放款次数:" + loanLendersNum + "次");
										result.putAttr("respcode", respcode);
										result.putAttr("day180appTimes", s006Map.get("loanLendersNum"));
									}
								}
							}
						}else{
							result.putAttr("respcode", statusCode);
							result.setMessage("未命中");
						}
						return result;
					}
				}
			}else{
				result.putAttr("respcode", resCode);
				result.setMessage("未命中");
				result.setSuccess(false);
				LogerUtil.log("请求失败：" + resMsg);
			}
			return result;
		}catch(Exception e){
			LogerUtil.error(AcedataUtil.class,e,"获取优分接口失败");
		}
		return result;
	}
}
