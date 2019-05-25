package org.xxjr.busi.util.push;

import java.util.Date;
import java.util.Map;

import org.ddq.common.util.JsonUtil;
import org.ddq.common.util.StringUtil;
import org.jsoup.Connection.Method;
import org.llw.model.cache.RedisUtils;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import lombok.extern.slf4j.Slf4j;

/**
 * 第三方统计功能
 * @author Administrator
 *
 */
@Slf4j
public class ThirdDataCount {

	
	/**
	 * 微信ocpa转化
	 * @param params
	 * @return
	 */
	public static void wxConvertor (Map<String, Object> params) {
		try {
			String userId = StringUtil.objectToStr(params.get("userId"));
			String clickId = StringUtil.objectToStr(params.get("clickId"));
			if (StringUtils.isEmpty(clickId) || StringUtils.isEmpty(userId)) {
				log.error("缺少必传参数！");
				return;
			}
			String wxAccessToken = getWXAccessToken(userId);
			if (StringUtils.isEmpty(wxAccessToken)) {
				log.error("缺少wxAccessToken！");
				return;
			}
			String url = "https://api.weixin.qq.com/marketing/user_actions/add?version=v1.0&access_token="+wxAccessToken;
			JSONObject object = new JSONObject();
			JSONArray actions = new JSONArray();
			JSONObject content = new JSONObject();
			content.put("user_action_set_id", userId);
			String referer = StringUtil.objectToStr(params.get("referer"));
			if (!StringUtils.isEmpty(referer)) {
				content.put("url", referer.substring(0, referer.indexOf("?")));
			}else {
				content.put("url", "https://m.xxjr.com/xxloan/csh8");
			}
			content.put("action_time", String.valueOf((int)(new Date().getTime() / 1000)));
			content.put("action_type", "RESERVATION");
			JSONObject trace = new JSONObject();
			trace.put("click_id", clickId);
			content.put("trace", trace);
			actions.add(content);
			object.put("actions", actions);
			/*PushDataServiceSend send = SpringAppContext.getBean(PushDataServiceSend.class);
			
			Map<String, Object> messageInfo = new HashMap<String, Object>();
			messageInfo.put("type", 4);
			messageInfo.put("url", url);
			messageInfo.put("httpMethod", "postJson");
			messageInfo.put("json", object.toJSONString());
			send.sendExecuteMessage(messageInfo);*/
			
			Map<String, Object> resMap = PushPlatformUtils.httpPost(url, null, object.toJSONString(), true, null);
			log.info("wx request resMap" + resMap);
		} catch (Exception e) {
			log.error("wxConvertor", e);
		}
	}
	
	private static String getWXAccessToken(String userId){
		try {
			String wxAccessToken = (String) RedisUtils.getRedisService().get(("Token" + userId));
			if (!StringUtils.isEmpty(wxAccessToken)) {
				return wxAccessToken;
			}
			
			String wxInfoStr = (String) RedisUtils.getRedisService().get(("wxUser" + userId));
			if (StringUtils.isEmpty(wxInfoStr)) {
				log.error("getWXAccessToken wxInfoStr is null");
				return null;
			}
			String[] wxInfo = wxInfoStr.split(",");
			String appId = wxInfo[0];
			String appSecret = wxInfo[1];
			String getTokenUrl = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid="+appId+"&secret="+appSecret;
			String tokenRes = PushPlatformUtils.origHttp(getTokenUrl, null, true, Method.GET);
			@SuppressWarnings("unchecked")
			Map<String, Object> tokenInfo = JsonUtil.getInstance().json2Object(tokenRes, Map.class);
			log.info("getWXAccessToken tokenInfo:" +tokenInfo);
			String accessToken = StringUtil.objectToStr(tokenInfo.get("access_token"));
			if (StringUtils.isEmpty(accessToken)) {
				log.error("getWXAccessToken accessToken is null");
				return null;
			}
			RedisUtils.getRedisService().set(("Token" + userId), accessToken, (60*60*1));
			return accessToken;
		} catch (Exception e) {
			log.error("getWXAccessToken error", e);
		}
		return null;
	}
}
