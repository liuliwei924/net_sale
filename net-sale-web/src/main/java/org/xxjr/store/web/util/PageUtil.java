package org.xxjr.store.web.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.ddq.active.mq.message.CustMessageSend;
import org.ddq.active.mq.message.JpushMessageSend;
import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.SpringAppContext;
import org.ddq.common.core.service.RemoteInvoke;
import org.ddq.common.util.DateUtil;
import org.ddq.common.util.LogerUtil;
import org.ddq.common.util.StringUtil;
import org.ddq.common.web.session.DuoduoSession;
import org.llw.model.cache.RedisUtils;
import org.springframework.util.StringUtils;
import org.xxjr.busi.util.kf.KfUserUtil;
import org.xxjr.cust.util.info.CustomerUtil;
import org.xxjr.sys.util.NumberUtil;
import org.xxjr.sys.util.ServiceKey;
import org.xxjr.sys.util.SysParamsUtil;

public class PageUtil {
	
	public static String cookieKey="XXJROPENID";
	/**找到所有的域名**/
	public static final String KEFU_VALID_YU = "KEFU_VALID_YU";

	/**找到所有的IP**/
	public static final String KEFU_VALID_IP = "KEFU_VALID_IP";
	
	/**上次登录的页面**/
	public static final String LastRequestUrl = "LastRequestUrl";
	
	/**防止重复提交的session**/
	public static final String PAGE_Token_session ="PAGE_TOKEN_SESSION"; 
	
	/**防止重复提交 前端Key**/
	public static final String PAGE_keyToken ="regToken";
	
	public static final int user_cache_time =60*30;
		
	public static void setEverPage(AppParam param, HttpServletRequest request){
		String everyPage = request.getParameter("everyPage");
		if(StringUtils.isEmpty(everyPage)){
			param.setEveryPage(10);
		}else{
			param.setEveryPage(Integer.valueOf(everyPage));
		}
	}
	
	/**
	 * 清除clearCustRedis 重新登录
	 * @param request
	 */
	public static void clearCustRedis(Object customerId){
		RedisUtils.getRedisService().del(CustomerUtil.KF_USER_SESSION + customerId);
	}
	/**
	 * 
	 * @param customerId
	 * @param kfLoginStatus 0-离线 1-在线
	 */
	public static void updateKfLoginStatus(Object customerId, int kfLoginStatus){
		//更新客服登陆状态
		AppParam updateParam = new AppParam("customerService", "newUpdate");
		updateParam.setRmiServiceName(AppProperties.getProperties(
				DuoduoConstant.RMI_SERVICE_START+ServiceKey.Key_cust));
		
		updateParam.addAttr("customerId", customerId);
		updateParam.addAttr("kfLoginStatus", kfLoginStatus);
		RemoteInvoke.getInstance().call(updateParam);
	}
	
	
	/***
	 *认证IP
	 * @param request
	 * @return
	 */
	public static boolean validIp(HttpServletRequest request){
		boolean kefuValidIp = SysParamsUtil.getBoleanByKey("kefuValidIp", false);
		if(!kefuValidIp){
			return true;
		}
		String url = request.getRequestURI();
		//登录处理URL可以直接使用
		if(url.indexOf("/cust/login")>=0 || url.indexOf("/cust/kjLogin")>=0 ||
				url.indexOf("/smsAction")>=0 ){
			return true;
		}
		
		//可登录IP 获取不成功，可以登录
		String[] canLoginIp = getCanLoginIp();
		if(canLoginIp==null || canLoginIp.length<=0){
			return true;
		}
		String signId = request.getParameter("signId");
		if(StringUtils.isEmpty(signId)){
			return false;
		}
		Object userId = RedisUtils.getRedisService().get(signId);
		if(!StringUtils.isEmpty(userId)){
			Map<String,Object> userInfo = KfUserUtil.getUserRight(userId.toString());
			if(userInfo.get("isLimitIp")!=null && "0".equals(userInfo.get("isLimitIp").toString())){
				return true;
			}
		}
		//获取用户IP
		String clientIp = DuoduoSession.getIpAddress(request);
		
		//判断用户IP是否一致
		for(String canIp:canLoginIp){
			if(clientIp.split(",")[0].equals(canIp)){
				return true;
			}
		}
		LogerUtil.log("No security Ip kfClientIp =" + clientIp +", canLoginIp:" + canLoginIp.toString());
		return false;
	}
	
	/***
	 * 获取可以使用的IP
	 * @return
	 */
	public static String[] getCanLoginIp(){
		//获取域名
		String validIps = (String) RedisUtils.getRedisService().get(KEFU_VALID_IP);
		if (validIps == null) {
			String defaultYu ="xxjrjt.f3322.net;xxjiaotong.f3322.net;xxjiaotong.f3322.net";
			validIps =  refreshIp((String)RedisUtils.getRedisService().get(KEFU_VALID_YU),defaultYu);
		}
		return validIps.split(";");
	}
	
	/***
	 * 刷新可以使用的IP
	 * @return
	 */
	private static String refreshIp(String hostNames, String defaultYu){
		if(hostNames==null || hostNames.trim().length()<10){
			hostNames = defaultYu;
		}
		InetAddress ip = null;
		StringBuffer ipAddress2 = new StringBuffer();
		for(String hostName:hostNames.split(";")){
			try {
				ip = InetAddress.getByName(hostName);
				if(ipAddress2.length()==0){
					ipAddress2.append(ip.getHostAddress());//113.104.199.147
				}else{
					ipAddress2.append(";"+ip.getHostAddress());
				}
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
		}
		//IP缓存10分钟
		try {
			RedisUtils.getRedisService().set(KEFU_VALID_IP, ipAddress2.toString(), 60 * 10);
		} catch (Exception e) {
		}
		return ipAddress2.toString();
	}
	
	/***
	 * 获取输入日期最后一天
	 * @return
	 */
	public static String getLastDay(String dateStr){
		Calendar c = Calendar.getInstance();
		c.setTime(DateUtil.toDateByString(dateStr, DateUtil.DATE_PATTERN_YYYY_MM_DD));
        c.set(Calendar.DATE, 1);
        c.add(Calendar.MONTH, 1);
        c.add(Calendar.DATE, -1);
        return DateUtil.toStringByParttern(c.getTime(),  DateUtil.DATE_PATTERN_YYYY_MM_DD);

	}
	/**
	 * 在结果集中添加访问数字段
	 * @return
	 */
	public static AppResult addRecordCount(AppResult result,String dateType, String startRecordDate, String endRecordDate) {
		AppParam params = new AppParam();
		DecimalFormat df = new DecimalFormat("######0.00");
		List<Map<String, Object>> list = result.getRows();
		if ("day".equals(dateType)) {
			for (Map<String, Object> map : list) {
				
				int dealStoreCount = NumberUtil.getInt(map.get("dealStoreCount"),0);//已更进数量
				int sucBookCount = NumberUtil.getInt(map.get("sucBookCount"),0);//上门数量
				int totalSignCount = NumberUtil.getInt(map.get("totalSignCount"),0);//签单数量
				int sucRetCount = NumberUtil.getInt(map.get("sucRetCount"),0);//回款数
				int repCount = NumberUtil.getInt(map.get("repCount"),0);//重复数量
				int applyCount = NumberUtil.getInt(map.get("applyCount"),0);//总申请数量
				
				double sucDeal = (sucBookCount*100.0)/(dealStoreCount == 0 ? 1:dealStoreCount);
				map.put("sucDeal", df.format(sucDeal) + "%");//上门率
				double totalDeal = (totalSignCount*100.0)/(dealStoreCount == 0 ? 1:dealStoreCount);//签单率
				map.put("totalDeal", df.format(totalDeal) + "%");
				double sucRetDeal = (sucRetCount*100.0)/(dealStoreCount == 0 ? 1:dealStoreCount);//回款率
				map.put("sucRetDeal", df.format(sucRetDeal) + "%");
				double sucTotal = (sucRetCount*100.0)/(totalSignCount == 0 ? 1:totalSignCount);//回款数/签单数
				map.put("sucTotal", df.format(sucTotal) + "%");
				
				double repRate = (repCount*100.0)/((applyCount + repCount)== 0 ? 1:(applyCount + repCount));//重复数量/总申请数量
				map.put("repRate", df.format(repRate) + "%");
				
				params.addAttr("channelCode", map.get("channelCode"));
				params.addAttr("countDate", map.get("recordDate"));
				params.addAttr("datePattern", "%Y-%m-%d");
				params.setService("pageCountService");
				params.setMethod("queryUVCount");
				params.setRmiServiceName(AppProperties
						.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
				AppResult result2 = RemoteInvoke.getInstance().callNoTx(params);
				if(result2.getRows().size()>0){
					if (result2.getRow(0)!=null) {
						int recordCount = NumberUtil.getInt(result2.getRow(0).get("recordCount"), 0);
						int regCount = NumberUtil.getInt(result2.getRow(0).get("regCount"), 0);
						double updateRate = ((applyCount-regCount)*100.0)/(applyCount== 0 ? 1:applyCount );
						map.put("updateRate", df.format(updateRate) + "%");
						map.put("recordCount", recordCount);
						double result1 = (applyCount*100.0)/((recordCount == 0 ? 1 : recordCount));
						map.put("recordCount1", df.format(result1) + "%");
					}else {
						map.put("recordCount", 0);
					}
				}else {
					map.put("recordCount", 0);
				}
			}
		}
		
		if ("range".equals(dateType)) {
			for (Map<String, Object> row : result.getRows()) {
				int dealStoreCount = NumberUtil.getInt(row.get("dealStoreCount"),0);//已更进数量
				int sucBookCount = NumberUtil.getInt(row.get("sucBookCount"),0);//上门数量
				int totalSignCount = NumberUtil.getInt(row.get("totalSignCount"),0);//签单数量
				int sucRetCount = NumberUtil.getInt(row.get("sucRetCount"),0);//回款数
				int repCount = NumberUtil.getInt(row.get("repCount"),0);//重复数量
				int applyCount = NumberUtil.getInt(row.get("applyCount"),0);//总申请数量
				double sucDeal = (sucBookCount*100.0)/(dealStoreCount == 0 ? 1:dealStoreCount);
				row.put("sucDeal", df.format(sucDeal) + "%");//上门率
				double totalDeal = (totalSignCount*100.0)/(dealStoreCount == 0 ? 1:dealStoreCount);//签单率
				row.put("totalDeal", df.format(totalDeal) + "%");
				double sucRetDeal = (sucRetCount*100.0)/(dealStoreCount == 0 ? 1:dealStoreCount);//回款率
				row.put("sucRetDeal", df.format(sucRetDeal) + "%");
				double sucTotal = (sucRetCount*100.0)/(totalSignCount == 0 ? 1:totalSignCount);//回款数/签单数
				row.put("sucTotal", df.format(sucTotal) + "%");
				
				double repRate = (repCount*100.0)/((applyCount + repCount)== 0 ? 1:(applyCount + repCount));//重复数量/总申请数量
				row.put("repRate", df.format(repRate) + "%");
				
				
				AppParam param = new AppParam("pageCountService", "queryRangeUVCount");
				param.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
				param.addAttr("channelCode", row.get("channelCode"));
				param.addAttr("startRecordDate", startRecordDate);
				param.addAttr("endRecordDate", endRecordDate);
				AppResult queryResult = RemoteInvoke.getInstance().callNoTx(param);
				double uv = NumberUtil.getDouble(queryResult.getRow(0).get("recordCount"), 0.0);
				double regCount = NumberUtil.getDouble(queryResult.getRow(0).get("regCount"), 0.0);
				row.put("recordCount", uv);
				double updateRate = ((applyCount-regCount)*100.0)/(applyCount== 0 ? 1:applyCount );
				row.put("updateRate", df.format(updateRate) + "%");
				double cvr = 0.0;
				if (uv > 0) {
					cvr = (applyCount / uv) * 100.0;
				}
				row.put("recordCount1", df.format(cvr) + "%");
			}
		}
		
		if ("month".equals(dateType)) {
			for (Map<String, Object> map : list) {
				
				int dealStoreCount = NumberUtil.getInt(map.get("dealStoreCount"),0);//已更进数量
				int sucBookCount = NumberUtil.getInt(map.get("sucBookCount"),0);//上门数量
				int totalSignCount = NumberUtil.getInt(map.get("totalSignCount"),0);//签单数量
				int sucRetCount = NumberUtil.getInt(map.get("sucRetCount"),0);//回款数
				int repCount = NumberUtil.getInt(map.get("repCount"),0);//重复数量
				int applyCount = NumberUtil.getInt(map.get("applyCount"),0);//总申请数量
				
				double sucDeal = (sucBookCount*100.0)/(dealStoreCount == 0 ? 1:dealStoreCount);
				map.put("sucDeal", df.format(sucDeal) + "%");//上门率
				double totalDeal = (totalSignCount*100.0)/(dealStoreCount == 0 ? 1:dealStoreCount);//签单率
				map.put("totalDeal", df.format(totalDeal) + "%");
				double sucRetDeal = (sucRetCount*100.0)/(dealStoreCount == 0 ? 1:dealStoreCount);//回款率
				map.put("sucRetDeal", df.format(sucRetDeal) + "%");
				double sucTotal = (sucRetCount*100.0)/(totalSignCount == 0 ? 1:totalSignCount);//回款数/签单数
				map.put("sucTotal", df.format(sucTotal) + "%");
				double repRate = (repCount*100.0)/((applyCount + repCount)== 0 ? 1:(applyCount + repCount));//重复数量/总申请数量
				map.put("repRate", df.format(repRate) + "%");
				
				params.addAttr("channelCode", map.get("channelCode"));
				params.addAttr("countDate", map.get("recordDate"));
				params.addAttr("datePattern", "%Y-%m");
				params.setService("pageCountService");
				params.setMethod("queryUVCount");
				params.setRmiServiceName(AppProperties
						.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
				AppResult result2 = RemoteInvoke.getInstance().callNoTx(params);
				if(result2.getRow(0)!=null){
					double regCount = NumberUtil.getDouble(result2.getRow(0).get("regCount"), 0.0);
					double updateRate = ((applyCount-regCount)*100.0)/(applyCount== 0 ? 1:applyCount );
					map.put("updateRate", df.format(updateRate) + "%");
					int recordCount = NumberUtil.getInt(result2.getRow(0).get("recordCount"), 0);
					map.put("recordCount", recordCount);
					map.put("recordCount1", df.format((applyCount*100.0)/((recordCount == 0 ? 1 : recordCount)))+"%");
				}else {
					map.put("recordCount", 0);
				}
			}
		}
		return result;
	}
	
	/**
	 * 解析assetCount字段给页面
	 * @param result
	 * @return
	 */
	public static AppResult analyzedAssetCountToPage(AppResult result) {
		List<Map<String, Object>> list = result.getRows();
		for (Map<String, Object> map : list) {
			String assetCount1 = StringUtil.getString(map.remove("assetCount1"));
			String[] split1 = assetCount1.split("-");
			for (int i = 0; i < split1.length; i++) {
				String sign ="assetCount1num"+i;
				map.put(sign, split1[i]);
			}
			String assetCount2 = StringUtil.getString(map.remove("assetCount2"));
			String[] split2 = assetCount2.split("-");
			for (int i = 0; i < split2.length; i++) {
				String sign ="assetCount2num"+i;
				map.put(sign, split2[i]);
			}
			String assetCount3 = StringUtil.getString(map.remove("assetCount3"));
			String[] split3 = assetCount3.split("-");
			for (int i = 0; i < split3.length; i++) {
				String sign ="assetCount3num"+i;
				map.put(sign, split3[i]);
			}
			String assetCount4 = StringUtil.getString(map.remove("assetCount4"));
			String[] split4 = assetCount4.split("-");
			for (int i = 0; i < split4.length; i++) {
				String sign ="assetCount4num"+i;
				map.put(sign, split4[i]);
			}
			String assetCount5 = StringUtil.getString(map.remove("assetCount5"));
			String[] split5 = assetCount5.split("-");
			for (int i = 0; i < split5.length; i++) {
				String sign ="assetCount5num"+i;
				map.put(sign, split5[i]);
			}
		}
		return result;
	}
	/**
	 * 解析assetCount字段给页面(时段分析统计使用)
	 * @param result
	 * @return
	 */
	public static AppResult analyzedAssetCountToPage2(AppResult result) {
		List<Map<String, Object>> list = result.getRows();
		for (Map<String, Object> map : list) {
			String assetCount1 = StringUtil.getString(map.remove("assetCount1"));
			String[] split1 = assetCount1.split("-");
			for (int i = 0; i < split1.length; i++) {
				String sign ="assetCount1num_"+i;
				map.put(sign, split1[i]);
			}
			String assetCount2 = StringUtil.getString(map.remove("assetCount2"));
			String[] split2 = assetCount2.split("-");
			for (int i = 0; i < split2.length; i++) {
				String sign ="assetCount2num_"+i;
				map.put(sign, split2[i]);
			}
			String assetCount3 = StringUtil.getString(map.remove("assetCount3"));
			String[] split3 = assetCount3.split("-");
			for (int i = 0; i < split3.length; i++) {
				String sign ="assetCount3num_"+i;
				map.put(sign, split3[i]);
			}
			String assetCount4 = StringUtil.getString(map.remove("assetCount4"));
			String[] split4 = assetCount4.split("-");
			for (int i = 0; i < split4.length; i++) {
				String sign ="assetCount4num_"+i;
				map.put(sign, split4[i]);
			}
			String assetCount5 = StringUtil.getString(map.remove("assetCount5"));
			String[] split5 = assetCount5.split("-");
			for (int i = 0; i < split5.length; i++) {
				String sign ="assetCount5num_"+i;
				map.put(sign, split5[i]);
			}
		}
		return result;
	}
	/**
	 * 处理数据（时段分析统计使用）
	 * @param result
	 * @param k
	 * @return
	 */
	public static AppResult processingData(AppResult result,int time) {
		List<Map<String, Object>> rows = result.getRows();
		if (time==1) {
			for (Map<String, Object> map : rows) {
				int hours1 = NumberUtil.getInt(map.get("hours"));
				map.put("hour", hours1);
			}
		}else {
			for (int i = 0; i < rows.size(); i++) {
				Map<String, Object> map = rows.get(i);
				int hours1 = NumberUtil.getInt(map.get("hours"));
				String channelCode1 = StringUtil.getString(map.get("channelCode"));
				for (int l = 0; l*time<24; l++) {
					if (hours1>=l*time&&hours1<(l+1)*time) {
						map.put("hour", "["+l*time+","+(l+1)*time+")");
						for (int j = i+1; j < rows.size(); j++) {
							Map<String, Object> map2 = rows.get(j);
							int hours2 = NumberUtil.getInt(map2.get("hours"));
							String channelCode2 = StringUtil.getString(map2.get("channelCode"));
							if (hours2>=l*time&&hours2<(l+1)*time&&channelCode1.equals(channelCode2)) {
								Set<String> keySet = map.keySet();
								for (String key : keySet) {
									if (key.contains("_")) {
										int sum = NumberUtil.getInt(map.get(key))+NumberUtil.getInt(map2.get(key));
										map2.put(key, sum);
									}
								}
								map2.put("hour", "["+l*time+","+(l+1)*time+")");
								rows.remove(i);
								i--;
								break;
							}
						}
					}
				}
			}
		}
		result.getPage().setTotalRecords(rows.size());
		return result;
	}
	
	/**
	 * 小渠道处理数据（时段分析统计使用）
	 * @param result
	 * @param k
	 * @return
	 */
	public static AppResult processingDataDtl(AppResult result,int time) {
		List<Map<String, Object>> rows = result.getRows();
		if (time==1) {
			for (Map<String, Object> map : rows) {
				int hours1 = NumberUtil.getInt(map.get("hours"));
				map.put("hour", hours1);
			}
		}else {
			for (int i = 0; i < rows.size(); i++) {
				Map<String, Object> map = rows.get(i);
				int hours1 = NumberUtil.getInt(map.get("hours"));
				String channelCode1 = StringUtil.getString(map.get("channelDetail"));
				for (int l = 0; l*time<24; l++) {
					if (hours1>=l*time&&hours1<(l+1)*time) {
						map.put("hour", "["+l*time+","+(l+1)*time+")");
						for (int j = i+1; j < rows.size(); j++) {
							Map<String, Object> map2 = rows.get(j);
							int hours2 = NumberUtil.getInt(map2.get("hours"));
							String channelCode2 = StringUtil.getString(map2.get("channelDetail"));
							if (hours2>=l*time&&hours2<(l+1)*time&&channelCode1.equals(channelCode2)) {
								Set<String> keySet = map.keySet();
								for (String key : keySet) {
									if (key.contains("_")) {
										int sum = NumberUtil.getInt(map.get(key))+NumberUtil.getInt(map2.get(key));
										map2.put(key, sum);
									}
								}
								map2.put("hour", "["+l*time+","+(l+1)*time+")");
								rows.remove(i);
								i--;
								break;
							}
						}
					}
				}
			}
		}
		result.getPage().setTotalRecords(rows.size());
		return result;
	}
	
	public static void getChannelDtlRecodeAndAddList (List<Map<String, Object>> rows, String dateType, String startRecordDate, String endRecordDate) {
		DecimalFormat df = new DecimalFormat("######0.00");
		if ("day".equals(dateType)) {
			for (Map<String, Object> row : rows) {
				AppParam param = new AppParam("pageCountService", "queryChannelDtlDayUv");
				param.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
				param.addAttr("channelDetail", row.get("channelDetail"));
				param.addAttr("countDate", row.get("recordDate"));
				AppResult result = RemoteInvoke.getInstance().callNoTx(param);
				double uv = NumberUtil.getDouble(result.getRow(0).get("recordCount"), 0.0);
				row.put("uv", uv);
				int applyCount = NumberUtil.getInt(row.get("applyCount"), 0);
				double cvr = 0.0;
				if (uv > 0) {
					cvr = (applyCount / uv) * 100.0;
				}
				row.put("cvr", df.format(cvr) + "%");
			}
		}
		
		if ("range".equals(dateType)) {
			for (Map<String, Object> row : rows) {
				AppParam param = new AppParam("pageCountService", "queryChannelDtlRangeUv");
				param.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
				param.addAttr("channelDetail", row.get("channelDetail"));
				param.addAttr("startRecordDate", startRecordDate);
				param.addAttr("endRecordDate", endRecordDate);
				AppResult queryResult = RemoteInvoke.getInstance().callNoTx(param);
				double uv = NumberUtil.getDouble(queryResult. getRow(0).get("recordCount"), 0.0);
				row.put("uv", uv);
				int applyCount = NumberUtil.getInt(row.get("applyCount"), 0);
				double cvr = 0.0;
				if (uv > 0) {
					cvr = (applyCount / uv) * 100.0;
				}
				row.put("cvr", df.format(cvr) + "%");
			}
		}
		
		if ("month".equals(dateType)) {
			for (Map<String, Object> row : rows) {
				AppParam param = new AppParam("pageCountService", "queryChannelDtlMonthUv");
				param.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
				param.addAttr("channelDetail", row.get("channelDetail"));
				String recordDate = StringUtil.getString(row.get("recordDate")) + "-01";
				param.addAttr("startRecordDate", recordDate);
				Date now = DateUtil.toDateByString(recordDate, DateUtil.DATE_PATTERN_YYYY_MM_DD);
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(now);
				calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
				String endDateStr = DateUtil.toStringByParttern(calendar.getTime(), DateUtil.DATE_PATTERN_YYYY_MM_DD);
				param.addAttr("endRecordDate", endDateStr);
				AppResult result = RemoteInvoke.getInstance().callNoTx(param);
				double uv = NumberUtil.getDouble(result.getRow(0).get("recordCount"), 0.0);
				row.put("uv", uv);
				int applyCount = NumberUtil.getInt(row.get("applyCount"), 0);
				double cvr = 0.0;
				if (uv > 0) {
					cvr = (applyCount / uv) * 100.0;
				}
				if (!StringUtils.isEmpty(result.getRow(0).get("pageEn"))) {
					row.put("pageReferer", result.getRow(0).get("pageEn"));
				}
				row.put("cvr", cvr > 0 ? df.format(cvr) + "%" : cvr + "%");
			}
		}
		
	}

	public static void addField(AppResult result) {
		DecimalFormat df = new DecimalFormat("######0.00");
		List<Map<String, Object>> rows = result.getRows();
		for (Map<String, Object> map : rows) {
			int failCount = NumberUtil.getInt(map.get("failCount"));
			int sucCount = NumberUtil.getInt(map.get("sucCount"));
			int allCount = failCount + sucCount;
			double temp = (sucCount*100.00)/allCount;
			map.put("allCount", allCount);
			map.put("svr", df.format(temp)+"%");
		}
	}
	/**
	 * 发送消息
	 * @param customerId	用户id
	 * @param realName		姓名
	 * @param messageType	消息类型
	 */
	public static void sendMsgByFreeTicketActivity(String customerId,Object realName, String messageType){
		//发送通知
		Map<String,Object> msgParam = new HashMap<String, Object>();
		msgParam.put("jpushClientType", "xdjl");  //推送给信贷经理app
		msgParam.put("realName", realName);
		msgParam.put("joinTime", new SimpleDateFormat("yyyy年M月dd日").format(new Date()));
		try {
			CustMessageSend messageSend = SpringAppContext.getBean(CustMessageSend.class);
			messageSend.sendCustMessage(customerId, messageType, msgParam);
			JpushMessageSend jpushSend = SpringAppContext.getBean(JpushMessageSend.class);
			jpushSend.sendCustMessage(customerId, messageType, msgParam);
		} catch (Exception e) {
			LogerUtil.error(PageUtil.class, e, messageType +"send message error");
		}
	}
	
	/**
	 * 封装一些通用的查询参数
	 * @param request
	 * @param params
	 */
	public static void packageQueryParam(HttpServletRequest request, AppParam params){
		int openStoreChannelFlag = SysParamsUtil.getIntParamByKey("openStoreChannelFlag", 0);
		if(openStoreChannelFlag == 1){
			//设置查询渠道类型
			String queryChannelType = SysParamsUtil.getStringParamByKey("storeQueryChannelType", "2");
			params.addAttr("queryChannelType", queryChannelType);
		}
	}
	
}
