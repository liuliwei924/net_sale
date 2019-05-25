package org.xxjr.busi.util.push;

import java.io.IOException;
import java.io.Serializable;
import java.security.Key;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

import org.apache.ibatis.ognl.Ognl;
import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.SpringAppContext;
import org.ddq.common.core.service.RemoteInvoke;
import org.ddq.common.core.service.SoaManager;
import org.ddq.common.util.DateUtil;
import org.ddq.common.util.JsonUtil;
import org.ddq.common.util.StringUtil;
import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.helper.HttpConnection;
import org.llw.model.cache.RedisUtils;
import org.springframework.util.StringUtils;
import org.xxjr.busi.util.BorrowChannelUtil;
import org.xxjr.sys.util.NumberUtil;
import org.xxjr.sys.util.ServiceKey;
import org.xxjr.sys.util.SysParamsUtil;

import lombok.extern.slf4j.Slf4j;

/***
 * 推送至其它平台工具类
 * 
 * @author ZQH
 *
 */
@Slf4j
public class PushPlatformUtils {
	
	//验证过的表达式
	private static Map<String, Integer> sucExpression = new ConcurrentHashMap<String, Integer>();
	
	private static final String[] USERAGENTS = {
		"Mozilla/5.0 (iPhone; U; CPU iPhone OS 4_3_3 like Mac OS X; en-us) AppleWebKit/533.17.9 (KHTML, like Gecko) Version/5.0.2 Mobile/8J2 Safari/6533.18.5",
		"Mozilla/5.0 (iPod; U; CPU iPhone OS 4_3_3 like Mac OS X; en-us) AppleWebKit/533.17.9 (KHTML, like Gecko) Version/5.0.2 Mobile/8J2 Safari/6533.18.5",
		"Mozilla/5.0 (iPad; U; CPU OS 4_3_3 like Mac OS X; en-us) AppleWebKit/533.17.9 (KHTML, like Gecko) Version/5.0.2 Mobile/8J2 Safari/6533.18.5",
		"Mozilla/5.0 (Linux; U; Android 2.3.7; en-us; Nexus One Build/FRF91) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1",
		"MQQBrowser/26 Mozilla/5.0 (Linux; U; Android 2.3.7; zh-cn; MB200 Build/GRJ22; CyanogenMod-7) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1",
		"Opera/9.80 (Android 2.3.4; Linux; Opera Mobi/build-1107180945; U; en-GB) Presto/2.8.149 Version/11.10",
		"Mozilla/5.0 (Linux; U; Android 3.0; en-us; Xoom Build/HRI39) AppleWebKit/534.13 (KHTML, like Gecko) Version/4.0 Safari/534.13",
		"Mozilla/5.0 (BlackBerry; U; BlackBerry 9800; en) AppleWebKit/534.1+ (KHTML, like Gecko) Version/6.0.0.337 Mobile Safari/534.1+",
		"Mozilla/5.0 (hp-tablet; Linux; hpwOS/3.0.0; U; en-US) AppleWebKit/534.6 (KHTML, like Gecko) wOSBrowser/233.70 Safari/534.6 TouchPad/1.0",
		"Mozilla/5.0 (SymbianOS/9.4; Series60/5.0 NokiaN97-1/20.0.019; Profile/MIDP-2.1 Configuration/CLDC-1.1) AppleWebKit/525 (KHTML, like Gecko) BrowserNG/7.1.18124",
		"Mozilla/5.0 (compatible; MSIE 9.0; Windows Phone OS 7.5; Trident/5.0; IEMobile/9.0; HTC; Titan)"
	};

	/** 类型:保险 */
	public static final int TYPE_INSURE = 1;

	/** 类型:小贷 **/
	public static final int TYPE_LOAN = 2;
	
	/** 获取推送配置缓存 **/
	public static final String PUSH_CFG_LIST_KEY = "push_cfg_list_key";
	
	/** 获取某个渠道当天是否推送完成的key前缀 ***/
	public static final String REDIS_MAX_COUNT_PREFIX = "redis_max_count_prefix";
	
	/***获取某个渠道api的数据是不是到最大了的key***/
	public static final String REDIS_MAX_API_COUNT_PREFIX = "redis_max_api_count_prefix";
	
	/***获取某个渠道信息流的数据是不是到最大了的key***/
	public static final String REDIS_MAX_OTHER_COUNT_PREFIX = "redis_max_other_count_prefix";
	
	/** 获取一个需要平均推送数量的key ***/
	public static final String REDIS_AVG_THRID_COUNT = "redis_avg_third_count";

	/** 1 小钱包推送 **/
	public static final int PUST_TYPE_XQB = 1;
	/** 2平安保险推送推送 */
	public static final int PUST_TYPE_BX = 2;

	/** 3 那家推送 **/
	public static final int PUST_TYPE_NJ = 3;

	/** 4 宜信推送 **/
	public static final int PUST_TYPE_YX = 4;

	/** 5 青峰推送 **/
	public static final int PUST_TYPE_QB = 5;

	public static final int base_cache_time = 60 * 60 * 24 * 7;

	public static final String cfg_zipCode = "push_third_cfg_zipCode";
	
	public static final String THIR_ALLOT_PUSH_START = "third_allot_push_start";
	
	
	private static int getCfgTimeOut() {
		return SysParamsUtil.getIntParamByKey("push_cfg_timeout", 10000);
	}
	
	/**
	 * 查询所有打开的渠道
	 * @param type 1.保险， 2贷款
	 * @param isDelay type=2可用，筛选当前的渠道是需要延迟推送的渠道还是普通推送
	 * @param isAllot true 是获取参与分单的 	false获取不参与分单的
	 * @return
	 */
	public static List<Map<String, Object>> getOpenChannelList (Integer type, boolean isDelay, boolean isAllot) {
		List<Map<String, Object>> configBytype = getConfigBytype(type);
		List<Map<String, Object>> newList = new ArrayList<Map<String,Object>>();
		for (int i = 0; i < configBytype.size(); i++) {
			if (!"1".equals(StringUtil.getString(configBytype.get(i).get("enable")))) {
				continue;
			}
			if (!isAllot && !"0".equals(StringUtil.getString(configBytype.get(i).get("isAllot")))) {
				continue;
			}else if (isAllot && !"1".equals(StringUtil.getString(configBytype.get(i).get("isAllot")))) {
				continue;
			}
			
			boolean check = ("2".equals(StringUtil.objectToStr(configBytype.get(i).get("type"))) && !StringUtils.isEmpty(configBytype.get(i).get("endDay")));
			if (isDelay) {//判断需要获取的贷款渠道是否有延迟功能
				if (!check) {
					continue;
				}
			}else {
				if (check) {
					continue;
				}	
			}
			newList.add(configBytype.get(i));
		}
		return newList;
	}
	
	/**
	 * 获取能推送对应系统用户数据的渠道
	 * @param sourceType
	 * @return
	 */
	public static List<Map<String, Object>> getInsureBySourceType (String sourceType){
		List<Map<String, Object>> configs = getOpenChannelList(PushPlatformUtils.TYPE_INSURE, false, false);
		List<Map<String, Object>> newList = new ArrayList<Map<String,Object>>();
		for (int i = 0; i < configs.size(); i++) {
			String sourceTypes = StringUtils.isEmpty(configs.get(i).get("sourceTypes")) ? "0" : StringUtil.objectToStr(configs.get(i).get("sourceTypes"));
			sourceTypes+=",";
			if (sourceTypes.contains((sourceType+","))) {
				newList.add(configs.get(i));
			}
		}
		return newList;
	}
	
	/**
	 * 检查推送渠道是否开启 false为未开启 true开启
	 * 
	 * @param pushType
	 * @return
	 */
	public static boolean checkChannelOpen(Map<String, Object> pushType) {
		boolean isPushPlatformCheck = false;
		if (pushType != null) {
			if ("1".equals(StringUtil.getString(pushType.get("enable")))) {
				isPushPlatformCheck = true;
			}
		}
		return isPushPlatformCheck;
	}

	/**
	 * 判断是否达到推送上限 true未达 false已达上限
	 * 
	 * @param pushType
	 * @return
	 */
	public static boolean checkChannelMaxCount(Map<String, Object> configInfo) {
		boolean isMaxCount = true;

		Integer pushType = NumberUtil.getInt(configInfo.get("pushCode"));
		
		//加速查询,如果当天已经达到推送最大数，存一个缓存，除非修改了配置或者到了下一天，缓存会删除
		Serializable key = RedisUtils.getRedisService().get(REDIS_MAX_COUNT_PREFIX+pushType);
		if (key != null) {
			return false;
		}
		
		if (configInfo == null || configInfo.isEmpty()) {
			return false;
		}
		
		int isAllot = NumberUtil.getInt(configInfo.get("isAllot"), 0);
		int cfgSucCount = NumberUtil.getInt(configInfo.get("maxCount"), 500);
		int cfgTotalCount = NumberUtil.getInt(configInfo.get("totalCount"), 500);
		if (isAllot == 0 && cfgSucCount == -1) {
			return true;
		}else if (isAllot == 1 && cfgTotalCount == -1) {
			return true;
		}
		
		AppParam queryParam = new AppParam("borrowApplyPushService", "queryPushCount");
		queryParam.addAttr("pushType", pushType);
		queryParam.addAttr("startRecordTime", DateUtil.toStringByParttern(new Date(), DateUtil.DATE_PATTERN_YYYY_MM_DD));
		queryParam.addAttr("endRecordTime", DateUtil.toStringByParttern(new Date(), DateUtil.DATE_PATTERN_YYYY_MM_DD) + " 23:59:59");
		AppResult result = new AppResult();
		if (SpringAppContext.getBean("borrowApplyPushService") != null) {
			result = SoaManager.getInstance().callNoTx(queryParam);
		} else {
			queryParam.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(queryParam);
		}
		
		if (isAllot == 0) {//不需要参与分单的还是按以前的一样查询当天推送成功数，未到就可以继续
			int sucCount = Integer.parseInt(result.getAttr("sucCount").toString());
			isMaxCount = (sucCount < cfgSucCount);
			if (!isMaxCount) {
				RedisUtils.getRedisService().set(REDIS_MAX_COUNT_PREFIX+pushType, isMaxCount, lastNowSeconds());
			}
		} else {//参与分单的需要先查询如果达到推送成功数量就停止或者是达到推送推送最大量也停止
			int sucCount = Integer.parseInt(result.getAttr("sucCount").toString());
			int totalCount = Integer.parseInt(result.getAttr("totalCount").toString());
			isMaxCount = (sucCount < cfgSucCount);
			if (isMaxCount) {
				isMaxCount = (totalCount < cfgTotalCount);
			}
			if (!isMaxCount) {
				RedisUtils.getRedisService().set(REDIS_MAX_COUNT_PREFIX+pushType, isMaxCount, lastNowSeconds());
				RedisUtils.getRedisService().del(PushPlatformUtils.REDIS_AVG_THRID_COUNT);//删除数量重新计算
			}
		}
		return isMaxCount;
	}
	
	/**
	 * 获取当天剩余的秒数
	 * @return
	 */
	public static int lastNowSeconds(){
		LocalTime time = LocalTime.now();
		LocalTime lastTime = time.with(LocalTime.MAX);
		return (int) Duration.between(time, lastTime).getSeconds();
	}
	
	public static boolean getInsureCondition (Map<String, Object> row, Map<String, Object> configInfo) {
		if (configInfo != null) {
			String pushName = StringUtil.objectToStr(configInfo.get("pushName"));
			StringBuilder expression = new StringBuilder();//后台默认校验表达式
			expression.append("(#notChannel != null and #notChannel != '' and channelCode != null and #notChannel.indexOf(channelCode) > -1)");
			expression.append(" or (#needCitys != null and #needCitys != '' and cityName != null and #needCitys.indexOf(cityName) <= -1)");
			expression.append(" or (#notCitys != null and #notCitys != '' and cityName != null and #notCitys.indexOf(cityName) > -1)");
			expression.append(" or (#sourceType == 0 and #minApplyAmount > 0 and #minApplyAmount > loanAmount)");
			expression.append(" or (#sourceType == 0 and #maxApplyAmount > 0 and loanAmount > #minApplyAmount)");
			expression.append(" or (#minAge > 0 and #minAge > age)");
			expression.append(" or (#maxAge > 0 and age > #maxAge)");
			expression.append(" or (#isStop == 1 and userAgent == null)");
			
			if ("29".equals(StringUtil.objectToStr(configInfo.get("pushCode")))) {
				expression.append(SysParamsUtil.getStringParamByKey("push_yunmu_cfg_trem", " or (identifyNo == null)"));
			}
			
			try {
				Object value = Ognl.getValue(expression.toString(), configInfo, row);
				if (value instanceof Boolean) {//会返回布尔值，如果是其他则为失败
					if ((Boolean)value) {//如果为true代表错误
						return false;
					}
				}else {
					return false;
				}
			} catch (Exception e) {
				log.error(pushName + " ognl resolve error expression:"+expression, e);
			}
			
			String expressions = StringUtil.objectToStr(configInfo.get("condition"));// 前端配置的校验表达式
			int checkCondition = checkCondition(expressions , row, pushName);
			if (checkCondition > 0) {
				return false; 
			}
		}
		return true;
	}
	
	/**
	 * 检查整改渠道是否需要暂停
	 * @param pushType
	 * @return
	 */
	public static boolean checkStopDate (Map<String, Object> configInfo) {
		int isStop = NumberUtil.getInt(configInfo.get("isStop"), 0);
		int type = NumberUtil.getInt(configInfo.get("type"), 1);
		try {
			if (isStop == 0 || type == 1) {//type=1是保险，保险渠道不需要暂停功能,保险一般不会调用这个方法，这个判断是为了保证保险渠道里面有这个方法也不会有事
				return true;
			}
			if (StringUtils.isEmpty(configInfo.get("stopStartDate")) || StringUtils.isEmpty(configInfo.get("stopEndDate"))) {
				return true;
			}
			Date now = new Date();
			Date stopStartDate = DateUtil.toDateByString(StringUtil.objectToStr(configInfo.get("stopStartDate")), DateUtil.DATE_PATTERN_YYYY_MM_DD_HHMMSS);
			Date stopEndDate = DateUtil.toDateByString(StringUtil.objectToStr(configInfo.get("stopEndDate")), DateUtil.DATE_PATTERN_YYYY_MM_DD_HHMMSS);
			if (now.getTime() > stopStartDate.getTime() && now.getTime() < stopEndDate.getTime()) {
				return false;
			}
		} catch (Exception e) {
			log.error("checkStopDate error", e);
		}
		return true;
	}

	/**
	 * 检查渠道配置的限制
	 * 
	 * @param row
	 * @param pushType
	 * @return
	 */
	public static boolean checkChannelLimit(Map<String, Object> row, Map<String, Object> configInfo) {
		int err = 0;
		if (configInfo != null) {
			String pushName = StringUtil.objectToStr(configInfo.get("pushName"));
			StringBuilder expression = new StringBuilder();//后台默认校验表达式
			try {
				expression.append("(#notChannel != null and #notChannel != '' and channelCode != null and #notChannel.indexOf(channelCode) > -1)");
				expression.append(" or (#needCitys != null and #needCitys != '' and cityName != null and #needCitys.indexOf(cityName) <= -1)");
				expression.append(" or (#notCitys != null and #notCitys != '' and cityName != null and #notCitys.indexOf(cityName) > -1)");
				expression.append(" or (#minApplyAmount > 0 and #minApplyAmount > loanAmount)");
				expression.append(" or (#maxApplyAmount > 0 and loanAmount > #minApplyAmount)");
				expression.append(" or (#minAge > 0 and #minAge > age)");
				expression.append(" or (#maxAge > 0 and age > #maxAge)");
				expression.append(" or (#grade != null and #grade != '' and grade != null and grade != '' and #grade.indexOf(grade) <= -1)");
				Object value = Ognl.getValue(expression.toString(), configInfo, row);
				if (value instanceof Boolean) {//会返回布尔值，如果是其他则为失败
					if ((Boolean)value) {//如果为true代表错误
						err++;
					}
				}else {
					err++;
				}
			} catch (Exception e) {
				log.error( pushName + " ognl resolve error expression:"+expression, e);
			}
			String expressions = StringUtil.objectToStr(configInfo.get("condition"));// 前端配置的校验表达式
			err+=checkCondition(expressions , row, pushName);
		}
		return (err == 0);
	}
	
	/**
	 * 限制房、车、保单、社保、公积金、微粒贷,是否有身份证,判断能推送的渠道类型api或h5或全部
	 * @param condition
	 * @param row
	 * @return
	 */
	public static int checkCondition(String expressions, Map<String, Object> row, String pushName){
		int err = 0;
		if (StringUtils.isEmpty(expressions)) {
			return err;
		}
		
		String[] expressionArray = expressions.split(";");
		for (String expression : expressionArray) {
			try {
				if (validateOgnl(expression)) {//先验证表达式安全性
					Object value = Ognl.getValue(expression.toString(), row);
					if (value instanceof Boolean) {//会返回布尔值，如果是其他则为失败
						if (!((Boolean)value)) {//这边如果为false为错误
							err++;
						}
					}else {
						err++;
					}
				}else {
					err++;
				}
			} catch (Exception e) {
				log.error(pushName + " ognl resolve error expression:"+expression, e);
			}
		}
		return err;
	}
	
	public static String getHouBenCityCode(String cityName) {
		String code = "2619";
		AppResult result = new AppResult();
		AppParam param = new AppParam("borrowPushCityService", "query");
		param.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START
						+ ServiceKey.Key_busi_in));
		param.addAttr("cityName", cityName);
		if (SpringAppContext.getBean("borrowPushCityService") == null) {
			result = RemoteInvoke.getInstance().callNoTx(param);
		} else {
			result = SoaManager.getInstance().invoke(param);
		}
		if (result.getRows().size() > 0) {
			code = StringUtil.getString(result.getRow(0).get("cityCode"));
		}
		return code;
	}

	/**
	 * 获取所有的推送渠道
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<Map<String, Object>> getPushConfig() {
		List<Map<String, Object>> configs = (List<Map<String, Object>>) RedisUtils
				.getRedisService().get(PUSH_CFG_LIST_KEY);
		if (configs == null) {
			configs = refreshPushConfig();
		}
		return configs;
	}

	/**
	 * 刷新缓存
	 * 
	 * @return
	 */
	public static List<Map<String, Object>> refreshPushConfig() {
		AppParam param = new AppParam("borrowPushCfgService", "query");
		param.setOrderBy("`index`");
		param.setOrderValue("ASC");
		param.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START
						+ ServiceKey.Key_busi_in));
		AppResult result = new AppResult();
		if (SpringAppContext.getBean("borrowPushCfgService") == null) {
			result = RemoteInvoke.getInstance().callNoTx(param);
		} else {
			result = SoaManager.getInstance().invoke(param);
		}
		if (result.getRows().size() > 0) {
			List<Map<String, Object>> rows = result.getRows();
			RedisUtils.getRedisService().set(PUSH_CFG_LIST_KEY,
					(Serializable) rows, base_cache_time);
			return rows;
		}
		return null;
	}

	/**
	 * 根据id获取推送渠道
	 * 
	 * @param pushType
	 * @return
	 */
	public static Map<String, Object> getConfigByCode(int pushType) {
		for (Map<String, Object> item : getPushConfig()) {
			if (pushType == NumberUtil.getInt(item.get("pushCode"))) {
				return item;
			}
		}
		return null;
	}

	/**
	 * 根据类型获取推送渠道(多个)
	 * 
	 * @param type
	 * @return
	 */
	public static List<Map<String, Object>> getConfigBytype(int type) {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		for (Map<String, Object> item : getPushConfig()) {
			if (type == NumberUtil.getInt(item.get("type"))) {
				list.add(item);
			}
		}
		return list;
	}

	/**
	 * 删除推送池里面的数据
	 * @param pushId 
	 * @param type 是保险还是小贷池
	 * @param insureSourceType 是否是其他系统接收的数据, -1为否
	 */
	public static void delete(Object pushId, Integer type, Integer insureSourceType) {
		AppParam param = new AppParam("insurancePushPoolService", "delete");
		param.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START
						+ ServiceKey.Key_busi_in));
		param.addAttr("pushId", pushId);
		if (PushPlatformUtils.TYPE_LOAN == type) {
			param.setService("thirdPushPoolService");
		}
		
		if (insureSourceType > 0) {
			param.setService("pushdataPoolService");
		}
		
		if (SpringAppContext.getBean(param.getService()) != null) {
			SoaManager.getInstance().invoke(param);
		} else {
			RemoteInvoke.getInstance().call(param);
		}
	}

	/**
	 * http请求,原始数据返回
	 * 
	 * @param url
	 * @param connParam
	 * @param contentType
	 * @return
	 * @throws IOException
	 */
	public static String origHttp(String url,
			Map<String, String> connParam, boolean contentType,Method method)
			throws IOException {
		log.info("PushPlatformUtils request url:" + url + " params:" + connParam);
		String body = null;
		if (url.indexOf("https") >= 0) {
			trustEveryone();
		}
		Connection connect = HttpConnection.connect(url);
		if (connParam != null && (!connParam.isEmpty())) {
			for (Entry<String, String> entry : connParam.entrySet()) {
				connect.data(entry.getKey(), entry.getValue());
			}
		}
		connect.method(method);
		connect.timeout(getCfgTimeOut());
		connect.ignoreContentType(contentType);
		Response response = connect.execute();
		body = response.body();
		log.info("PushPlatformUtils response result" + body);
		return body;
	}
	
	

	/**
	 * http请求,封装成Map返回
	 * 
	 * @param url
	 * @param connParam
	 * @param contentType
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, Object> httpPost(String url,
			Map<String, String> connParam, boolean contentType)
			throws Exception {
		String body = origHttp(url, connParam, contentType, Method.POST);
		Map<String, Object> res = JsonUtil.getInstance().json2Object(body,
				Map.class);
		if (res == null) {
			res = new HashMap<String, Object>();
		}
		return res;
	}

	/**
	 * Json格式的http请求
	 * 
	 * @param url
	 * @param connParam
	 * @param json
	 * @param contentType
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, Object> httpPost(String url,
			Map<String, String> connParam, String json, boolean contentType,
			Map<String, String> headers) throws Exception {
		log.info("PushPlatformUtils request url:" + url + " params:" + connParam + " json:" + json);
		String body = null;
		if (url.indexOf("https") >= 0) {
				trustEveryone();
		}
		Connection connect = HttpConnection.connect(url);
		if (headers != null && (!headers.isEmpty())) {
			connect.headers(headers);
		} else {
			connect.header("Content-Type", "application/json; charset=UTF-8");
		}
		connect.timeout(getCfgTimeOut());
		connect.method(Method.POST);
		connect.requestBody(json);
		if (connParam != null && (!connParam.isEmpty())) {
			for (Entry<String, String> entry : connParam.entrySet()) {
				connect.data(entry.getKey(), entry.getValue());
			}
		}
		connect.ignoreContentType(contentType);
		Response response = connect.execute();
		body = response.body();
		Map<String, Object> res = JsonUtil.getInstance().json2Object(body,
				Map.class);
		if (res == null) {
			res = new HashMap<String, Object>();
		}
		log.info("PushPlatformUtils response result" + body);
		return res;
	}

	/**
	 * 推送数据统计方法
	 * 
	 * @param startDate
	 * @return
	 */
	public static int sumPushData(String startDate) {
		AppResult result = new AppResult();
		AppParam queryParam = new AppParam("borrowApplyPushService",
				"sumPushData");
		queryParam.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START
						+ ServiceKey.Key_busi_in));
		String endDate = startDate + " 23:59:59";
		queryParam.addAttr("startDate", startDate);
		queryParam.addAttr("endDate", endDate);
		AppResult queryResult = null;
		List<Map<String, Object>> rows = null;
		try {
			queryResult = RemoteInvoke.getInstance().callNoTx(queryParam);
			rows = queryResult.getRows();
			if (rows.size() > 0) {
				AppParam insertParam = new AppParam("sumBorrowPushService",
						"saveOrUpdate");
				insertParam.setRmiServiceName(AppProperties
						.getProperties(DuoduoConstant.RMI_SERVICE_START
								+ ServiceKey.Key_sum));
				insertParam.addAttr("list", queryResult.getRows());
				result = RemoteInvoke.getInstance().call(insertParam);
			}
		} catch (Exception e) {
			log.error("PushPlatformUtils sumPushData error", e);
		}
		
		try {
			queryParam = new AppParam("borrowApplyPushService",
					"sumDistinctData");
			queryParam.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			queryParam.addAttr("startDate", startDate);
			queryParam.addAttr("endDate", endDate);
			queryResult = RemoteInvoke.getInstance().callNoTx(queryParam);
			rows = queryResult.getRows();
			if (rows.size() > 0) {
				AppParam insertParam = new AppParam("sumBorrowPushService",
						"saveOrUpdate");
				insertParam.setRmiServiceName(AppProperties
						.getProperties(DuoduoConstant.RMI_SERVICE_START
								+ ServiceKey.Key_sum));
				insertParam.addAttr("list", queryResult.getRows());
				result = RemoteInvoke.getInstance().call(insertParam);
			}
		} catch (Exception e) {
			log.error("PushPlatformUtils sumDistinctData error", e);
		}
		
		
		int count = NumberUtil.getInt(
				result.getAttr(DuoduoConstant.DAO_Insert_SIZE), 0);
		return count;
	}
	/**
	 * 推送数据统计方法(按渠道统计)
	 * 
	 * @param startDate
	 * @return
	 */
	public static void sumChannelPushData(String startDate) {
		try {
			AppParam queryParam = new AppParam("borrowApplyPushService",
					"sumChannelPushData");
			queryParam.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			String endDate = startDate + " 23:59:59";
			queryParam.addAttr("startDate", startDate);
			queryParam.addAttr("endDate", endDate);
			AppResult queryResult = RemoteInvoke.getInstance().callNoTx(queryParam);
			List<Map<String, Object>> rows = queryResult.getRows();
			if (rows.size() > 0) {
				AppParam insertParam = new AppParam("sumChannelPushService",
						"saveOrUpdate");
				insertParam.setRmiServiceName(AppProperties
						.getProperties(DuoduoConstant.RMI_SERVICE_START
								+ ServiceKey.Key_sum));
				insertParam.addAttr("list", queryResult.getRows());
				RemoteInvoke.getInstance().call(insertParam);
			}
		} catch (Exception e) {
			log.error("PushPlatformUtils sumChannelPushData error", e);
		}
	}
	@SuppressWarnings("unchecked")
	public static String getZipCodeByCityName(String cityName) {
		Map<String, Object> zipCodes = (HashMap<String, Object>) RedisUtils.getRedisService().get(cfg_zipCode);
		if (zipCodes == null || zipCodes.isEmpty()) {
			zipCodes = refreshZipCode();
		}
		return StringUtil.getString(zipCodes.get(cityName));
	}
	
	@SuppressWarnings("unchecked")
	public static Map<String, Object> refreshZipCode() {
		HashMap<String, Object> zipCodes= new HashMap<String, Object>();
		try {
			Connection connect = HttpConnection.connect("http://cnis.7east.com/widget.do?type=servicebyid&ajax=yes&action=serchbyid");
			connect.method(Method.POST);
			connect.timeout(5000);
			Response execute = connect.execute();
			String provinceRes = execute.body();
			if (!StringUtils.isEmpty(provinceRes)) {
				Map<String, Object> provinces = JsonUtil.getInstance().json2Object(provinceRes, Map.class);
				List<Map<String, Object>> provinceList = (List<Map<String, Object>>)provinces.get("rows");
				for (Map<String, Object> item : provinceList) {
					//zipCodes.put(StringUtil.getString(item.get("name")), item.get("shuzi"));
					Connection chirdConn = HttpConnection.connect("http://cnis.7east.com/widget.do?type=service&action=cnischildlist&a=2&ajax=yes&pid=" + item.get("region_id"));
					chirdConn.method(Method.POST);
					chirdConn.timeout(5000);
					Response chirdExecute = chirdConn.execute();
					String cityStr = chirdExecute.body();
					if (StringUtils.isEmpty(cityStr)) {
						continue;
					}
					Map<String, Object> cityInfo = JsonUtil.getInstance().json2Object(cityStr, Map.class);
					List<Map<String, Object>> citys = (List<Map<String, Object>>)cityInfo.get("rows");
					if (citys.size() == 1 && "市辖区".equals(StringUtil.getString(citys.get(0).get("local_name")))) {
						zipCodes.put(StringUtil.getString(item.get("name")), citys.get(0).get("code"));
						continue;
					}
					for (Map<String, Object> item1 : citys) {
						zipCodes.put(StringUtil.getString(item1.get("local_name")), item1.get("code"));
					}
					RedisUtils.getRedisService().set(cfg_zipCode, zipCodes);
				}
			}
		} catch (Exception e) {
			log.error("refreshZipCode", e);
		}
		return zipCodes;
	}

	/**
	 * 生日生成，有传原来的，没有随机生成
	 * 
	 * @param birthday
	 * @param age
	 * @return
	 */
	public static String getBirthDay(String birthday, int age) {
		if (StringUtils.hasText(birthday)) {
			return birthday;
		} else if (age > 0) {
			Random random = new Random();
			Calendar ca = Calendar.getInstance();
			int year = ca.get(Calendar.YEAR) - age;
			
			int month = random.nextInt(12) % (12) + 1;
			int day = random.nextInt(30) % (30) + 1;
			birthday = year + "-" + (month < 10 ? "0" + month : month + "")
					+ "-" + (day < 10 ? "0" + day : day + "");
		} else {
			Random random = new Random();
			Calendar ca = Calendar.getInstance();
			int minYear = 1968;
			int maxYear = ca.get(Calendar.YEAR) - 25;
			int year = random.nextInt(maxYear) % (maxYear - minYear + 1)
					+ minYear;
			int month = random.nextInt(12) % (12) + 1;
			int day = random.nextInt(30) % (30) + 1;
			birthday = year + "-" + (month < 10 ? "0" + month : month + "")
					+ "-" + (day < 10 ? "0" + day : day + "");
		}
		return birthday;
	}

	/**
	 * ECB加密,不要IV
	 * 
	 * @param key
	 *            密钥
	 * @param data
	 *            明文
	 * @return Base64编码的密文
	 * @throws Exception
	 */
	public static byte[] des3EncodeECB(byte[] key, byte[] data)
			throws Exception {
		Key deskey = null;
		DESedeKeySpec spec = new DESedeKeySpec(key);
		SecretKeyFactory keyfactory = SecretKeyFactory.getInstance("desede");
		deskey = keyfactory.generateSecret(spec);
		Cipher cipher = Cipher.getInstance("desede" + "/ECB/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, deskey);
		byte[] bOut = cipher.doFinal(data);
		return bOut;
	}

	/**
	 * ECB解密,不要IV
	 * 
	 * @param key
	 *            密钥
	 * @param data
	 *            Base64编码的密文
	 * @return 明文
	 * @throws Exception
	 */
	public static byte[] ees3DecodeECB(byte[] key, byte[] data)
			throws Exception {
		Key deskey = null;
		DESedeKeySpec spec = new DESedeKeySpec(key);
		SecretKeyFactory keyfactory = SecretKeyFactory.getInstance("desede");
		deskey = keyfactory.generateSecret(spec);
		Cipher cipher = Cipher.getInstance("desede" + "/ECB/PKCS5Padding");
		cipher.init(Cipher.DECRYPT_MODE, deskey);
		byte[] bOut = cipher.doFinal(data);
		return bOut;
	}
	
	public static boolean isPushTest(){
		return RedisUtils.getRedisService().get("push_isDebug") != null;
	}
	
	private static final Random r = new Random();
	
	/**
	 * 获取浏览器用户代理，如果参数不为空，直接返回
	 * @return
	 */
	public static String getUserAgent(Object userAgent){
		if (!StringUtils.isEmpty(userAgent)) {
			return StringUtil.objectToStr(userAgent);
		}else {
			int i = r.nextInt(USERAGENTS.length);
			return USERAGENTS[i];
		}
	}
	
	/**
	 * 验证ognl表达式，除去缓存中存入的
	 * 特定字符，如果表达式是还存在其他字符，则不执行也不保存。
	 * 后台显示的ognl表达式不需要验证，主要是前端页面可配置的表达式需要验证
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static boolean validateOgnl (String expression){
		int err = 0;
		if (StringUtils.isEmpty(expression)) {
			return false;
		}
		
		if (sucExpression.get(expression) != null) {//如果这个表达式验证过则直接返回
			return true;
		}
		
		List<String> terms = (List<String>) RedisUtils.getRedisService().get("third_pushData_ognlTerms");
		if (terms == null || terms.size() == 0) {
			String termStr = StringUtil.getString(RedisUtils.getRedisService().get("third_pushData_ognlStr"));
			if (!StringUtils.isEmpty(termStr)) {
				terms = Arrays.asList(termStr.split(";"));
				RedisUtils.getRedisService().set("third_pushData_ognlTerms", (Serializable)terms);
			}
		}
		if (terms == null || terms.size() == 0) {
			return false;
		}
		
		String[] keys = expression.split(" ");
		for (String key : keys) {
			if (!terms.contains(key)) {
				err++;
			}
		}
		if (err == 0) {//表达式通过验证
			sucExpression.put(expression, 1);
			return true;
		}
		return false;
	}
	
	/**
	 * 根据某个key去掉重复数据
	 * @param list 
	 * @param key 
	 * @param type 保险还是贷款
	 * @param insureSourceType 其他平台数据，默认或小贷-1
	 * @return
	 */
	public static void distinctByKey(List<Map<String, Object>> list, String key, Integer type, Integer insureSourceType){
		if (list == null || list.isEmpty()) {
			return;
		}
		List<Object> l1 = new ArrayList<Object>();
		Iterator<Map<String, Object>> it = list.iterator();
		while (it.hasNext()) {
			Map<String, Object> now = it.next();
			Object field = now.get(key);
			if (l1.contains(field)) {
				Object pushId = now.get("pushId");
				delete(pushId, type, insureSourceType);
				it.remove();
			}else {
				l1.add(field);
			}
		}
	}
	
	
	/**
	 * 判断申请是否可以进入第三方分配池
	 * @param now 申请数据
	 * @return boolean(true 可以  false不行)
	 */
	public static boolean isAllotThird (Map<String, Object> now) {
		boolean flag = false;
		try {
			if (NumberUtil.getInt(now.get("pushStatus"), 0) == 1) {//代表已经推送过了
				return flag;
			}
			
			if (!SysParamsUtil.getBoleanByKey("third_channel_allot_enable", false)) {
				return flag;
			}
			
			boolean pushStart = SysParamsUtil.getBoleanByKey(THIR_ALLOT_PUSH_START, false);
			if (pushStart) {//true代表处理推送状态
				return flag;
			}
			
			List<Map<String, Object>> channels = getOpenChannelList(TYPE_LOAN, false, true);
			Iterator<Map<String, Object>> iterator = channels.iterator();
			while (iterator.hasNext()) {
				Map<String, Object> item = iterator.next();
				if (!checkChannelMaxCount(item) 
					|| !checkChannelLimit(now, item)
					|| !checkChannelOpen(item)
					|| !isPushDateTime(StringUtil.objectToStr(item.get("pushCode")))
					|| !isApiOrOtherMaxCount(now.get("channelCode"), item.get("pushCode"))
					|| !checkOffDayStopPush(item.get("pushCode"))) {
					iterator.remove();
				}
			}
			if (channels.size() == 0) {
				return flag;
			}
			Integer avgCount = getAvgCount(channels);
			if (avgCount == 0) {
				return flag;
			}else {
				AppParam param = new AppParam("thirdPushPoolService", "queryCount");
				param.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
				param.addAttr("immediate", 4);
				param.addAttr("nowDate", LocalDate.now().toString());
				AppResult countResult = null;
				if (SpringAppContext.getBean("thirdPushPoolService") != null) {
					countResult = SoaManager.getInstance().callNoTx(param);
				}else {
					countResult = RemoteInvoke.getInstance().callNoTx(param);
				}
				int poolCount = NumberUtil.getInt(countResult.getAttr(DuoduoConstant.TOTAL_SIZE), 0);
				flag = (poolCount == 0 || poolCount < avgCount);
			}
		} catch (Exception e) {
			log.error("isAllotThird error", e);
			flag = false;
		}
		return flag;
	}
	
	
	/**
	 * 根据传入的多个渠道配置返回一个平均需要推送的数量,当池子里面的数据量到达这个上限停止继续入池
	 * @param channels
	 * @return
	 */
	public static Integer getAvgCount (List<Map<String, Object>> channels) {
		int avgCount = NumberUtil.getInt(RedisUtils.getRedisService().get(REDIS_AVG_THRID_COUNT), 0);
		if (avgCount == 0) {
			int allSucCount = 0;
			for (Map<String, Object> item : channels) {
				if (checkChannelMaxCount(item) || checkChannelOpen(item)) {//判断这个渠道没到上限
					allSucCount+=NumberUtil.getInt(item.get("maxCount"), 0);
				}
			}
			if (allSucCount > 0) {
				avgCount = (allSucCount / channels.size());
			}
			RedisUtils.getRedisService().set(REDIS_AVG_THRID_COUNT, avgCount, lastNowSeconds());
		}
		return avgCount;
	}
	
	/**
	 * 判断是否已经到了推送的时间，默认都是不限制
	 * @param pushType
	 * @return
	 */
	public static boolean isPushDateTime (String pushType) {
		boolean isCheckMinute = SysParamsUtil.getBoleanByKey("pushData_isCheckMinute", true);
		if (isCheckMinute) {
			int startHour = SysParamsUtil.getIntParamByKey(String.format("pushData_start_time_%s", pushType), 0);
			int endHour = SysParamsUtil.getIntParamByKey(String.format("pushData_end_time_%s", pushType), 23);
			LocalTime time = LocalTime.now();
			int nowHour = time.getHour();
			if (nowHour >= startHour && nowHour <= endHour) {
				return true;
			}
		} else {
			LocalTime startTime = LocalTime.parse(SysParamsUtil.getStringParamByKey(String.format("pushData_start_strTime_%s", pushType), "00:00:00"), DateTimeFormatter.ISO_LOCAL_TIME);
			LocalTime endTime = LocalTime.parse(SysParamsUtil.getStringParamByKey(String.format("pushData_end_strTime_%s", pushType), "23:00:00"), DateTimeFormatter.ISO_LOCAL_TIME);
			LocalTime time = LocalTime.now();
			if (time.isAfter(startTime) && time.isBefore(endTime)) {
				return true;
			}
		}
		return false;
	}
	
	
	/**
	 * 判断该渠道是否休息日停止推送
	 * @param pushType
	 * @return
	 */
	public static boolean checkOffDayStopPush (Object pushType) {
		boolean flag = SysParamsUtil.getBoleanByKey(String.format("pushData_off_day_stop_%s", pushType), false);
		if (flag) {
			LocalDate now = LocalDate.now();
			if (now.getDayOfWeek() == DayOfWeek.SATURDAY || now.getDayOfWeek() == DayOfWeek.SUNDAY) {
				return false;
			}
		}
		return true;
	}
	
	
	/**
	 * 判断某个渠道pai或者信息流的数据是否是达到上限了
	 * @param channelCode
	 * @param pushType
	 * @return
	 */
	public static boolean isApiOrOtherMaxCount(Object channelCode, Object pushType){
		boolean flag = true;
		Map<String, Object> channel = BorrowChannelUtil.getChannelByCode(StringUtil.objectToStr(channelCode));
		if (channel == null || channel.isEmpty()) {
			return flag;
		}
		
		boolean isApi = (NumberUtil.getInt(channel.get("type"), 3) == 3);
		Serializable otherKey = RedisUtils.getRedisService().get((REDIS_MAX_OTHER_COUNT_PREFIX + pushType));
		
		if (isApi && otherKey != null) {//是api数据并且信息流到上限了
			Serializable key = RedisUtils.getRedisService().get((REDIS_MAX_API_COUNT_PREFIX + pushType));
			flag = (key == null);
		} else if(isApi && otherKey == null) {//是api数据信息流没到上限，不推api的
			flag = false;
		} else {//是信息流数据,再判断有没有到上限，没有继续推
			flag = (otherKey == null);
		}
		return flag;
	}

	
	/***
	 * https连接处理
	 */
	public static void trustEveryone() {  
	        try {  
	            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() { 
					@Override
					public boolean verify(String arg0, SSLSession arg1) {
						return true;
					}  
	            }); 
	            SSLContext context = SSLContext.getInstance("TLS");  
	            context.init(null, new X509TrustManager[] { new X509TrustManager() {

					@Override
					public void checkClientTrusted(X509Certificate[] chain,
							String authType) throws CertificateException {
						
					}
					@Override
					public void checkServerTrusted(X509Certificate[] chain,
							String authType) throws CertificateException {
						
					}

					@Override
					public X509Certificate[] getAcceptedIssuers() {
						return null;
					}  
	                
	            } }, new SecureRandom());  
	            HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());  
	        } catch (Exception e) {  
	        }  
	    } 
}