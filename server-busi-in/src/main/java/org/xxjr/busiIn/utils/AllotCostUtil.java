package org.xxjr.busiIn.utils;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.ddq.active.mq.message.RmiServiceSend;
import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.SpringAppContext;
import org.ddq.common.core.service.SoaManager;
import org.ddq.common.util.DateUtil;
import org.ddq.common.util.StringUtil;
import org.springframework.util.StringUtils;
import org.xxjr.busi.util.ApplyAllotUtil;
import org.xxjr.busi.util.BorrowChannelUtil;
import org.xxjr.busi.util.CountGradeUtil;
import org.xxjr.busi.util.StoreSeparateUtils;
import org.xxjr.cust.util.CustConstant;
import org.xxjr.sys.util.NumberUtil;
import org.xxjr.sys.util.ServiceKey;
import org.xxjr.sys.util.SysParamsUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * 分单成本工具类
 * @author zenghw
 *
 */
@Slf4j
public class AllotCostUtil {
	
	/**
	 * 判断是否满足记录分单成本条件
	 * @param applyTime
	 * @return
	 */
	public static boolean isAllotCost(String applyTime){
		Date nowTime = DateUtil.toDateByString(applyTime, DateUtil.DATE_PATTERN_YYYY_MM_DD_HHMMSS);
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		//1周日 2周一 3周二......
		int w = cal.get(Calendar.DAY_OF_WEEK);
		if(2 == w){
			//当前时间前两天
			String afterTowDay = DateUtil.toStringByParttern(getNextDay(new Date(),-2), DateUtil.DATE_PATTERN_YYYY_MM_DD)+ " 00:00:00";
			Date beforDate = DateUtil.toDateByString(afterTowDay, DateUtil.DATE_PATTERN_YYYY_MM_DD_HHMMSS);
			//判断applyTime是否在时间段内
			boolean belong = belongCalendar(nowTime,beforDate,new Date());
			if(belong){
				return true;
			}
		}else{
			//当前时间前一天
			String beforOneDay = DateUtil.toStringByParttern(getNextDay(new Date(),-1), DateUtil.DATE_PATTERN_YYYY_MM_DD)+ " 00:00:00";
			Date beforDate = DateUtil.toDateByString(beforOneDay, DateUtil.DATE_PATTERN_YYYY_MM_DD_HHMMSS);
			boolean belong2 = belongCalendar(nowTime,beforDate,new Date());
			if(belong2){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 获取当前系统前几天日期
	 * @param date
	 * @return
	 */
	public static Date getNextDay(Date date,int beforeDay) {  
        Calendar calendar = Calendar.getInstance();  
        calendar.setTime(date);  
        calendar.add(Calendar.DAY_OF_MONTH, beforeDay);  
        date = calendar.getTime();  
        return date;  
    }  
	
	
	/**
     * 判断时间是否在时间段内
     * @param nowTime
     * @param beginTime
     * @param endTime
     * @return
     */
    public static boolean belongCalendar(Date nowTime, Date beginTime, Date endTime) {
        if (nowTime.getTime() > beginTime.getTime()  && nowTime.getTime() < endTime.getTime()) {
            return true;
        } else {
            return false;
        }
    }
    
	/**
	 * 通过MQ同步注册数
	 * @param paramsMap
	 */
	public static void pageCount (AppParam param) {
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		try {
			paramsMap.put("service", "pageCountService");
			paramsMap.put("method", "recordCount");
			paramsMap.put("rmiServiceName", AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
			Object pageId = param.getAttr("pageId");
			Object pageReferer = param.getAttr("pageReferer");
			if (StringUtils.isEmpty(pageReferer)) {
				pageReferer = param.getAttr("sysCode");
			}
			if (StringUtils.isEmpty(pageId) && StringUtils.isEmpty(pageReferer)) {
				return;
			}
			if (StringUtils.isEmpty(param.getAttr("sumType"))) {//如果sumType不传，就默认统计pv
				param.addAttr("sumType", "pv");
			}
			
			Object channelDetail = param.getAttr("regSourceType");
			if (StringUtils.isEmpty(channelDetail)) {
				channelDetail = param.getAttr("channelDetail");
			}
			channelDetail = StringUtils.isEmpty(channelDetail) ? CustConstant.CUST_SOURCETYPE_DEFAULT
					: channelDetail;
			paramsMap.put("pageId", pageId);
			paramsMap.put("sumType", param.getAttr("sumType"));
			paramsMap.put("pageReferer", pageReferer);
			String channelCode = BorrowChannelUtil.getChannelByStartCode(channelDetail.toString());
			paramsMap.put("channelCode", channelCode);
			paramsMap.put("channelDetail", channelDetail);
			RmiServiceSend send = SpringAppContext.getBean(RmiServiceSend.class);
			send.sendExecuteMessage(paramsMap);
		} catch (Exception e) {
			log.error("同步注册数 失败", e);
		}
	}
	
	
	/**
	 * 通过MQ同步t_apply_union 信息
	 * @param paramsMap
	 */
	public static  AppResult immedTranApply(Map<String,Object> now){
		AppResult result = new AppResult();
		try{
			String applyName = StringUtil.getString(now.get("applyName"));
			if(applyName.indexOf("测试")> -1){
				return result;
			}
			
			ApplyAllotUtil.conversionType(now);
			String grade = CountGradeUtil.getGrade(now) ;
			
			now.put("grade", grade);
			
			int insure = NumberUtil.getInt(now.get("insure"),0);//是否勾选保险协议
			String cityName = StringUtil.getString(now.get("cityName"));
			
			//double loanAmount = NumberUtil.getDouble(now.get("loanAmount"), 0);
			
			int allotFlag = 1;
			String testDatachannels = SysParamsUtil.getStringParamByKey("test_data_channel", "cccj,ccdx,aijjk,soho") + ",";
			String netChannelCodes = SysParamsUtil.getStringParamByKey("netChannelCodes", "");
			String saleChannelCodes = SysParamsUtil.getStringParamByKey("saleChannelCodes", "");
			String kfChannelCodes = SysParamsUtil.getStringParamByKey("kfChannelCodes", "");
			String channelCode = StringUtil.getString(now.get("channelCode"));

			if(StringUtils.hasText(netChannelCodes) 
					&& StringUtils.hasText(channelCode) 
					&& (netChannelCodes.indexOf(channelCode+",") >-1 || testDatachannels.indexOf(channelCode+",") >-1)){
				boolean flag = SysParamsUtil.getBoleanByKey("open_kf_test_channel", false);
				String cityNames = SysParamsUtil.getStringParamByKey("kf_test_channel_citys", "深圳市");
				String notChannels = SysParamsUtil.getStringParamByKey("kf_test_not_channel", "keji001");
				if (flag && cityNames.contains(cityName) && (!notChannels.contains(channelCode))) {
					allotFlag = 4;
				}else {
					allotFlag = 1;
					now.put("testNetChannel", 1);
				}
			} else if(StringUtils.hasText(saleChannelCodes) 
					&& StringUtils.hasText(channelCode) 
					&& saleChannelCodes.indexOf(channelCode+",") >-1){
				allotFlag = 2;
			}else if(StringUtils.hasText(kfChannelCodes) 
					&& StringUtils.hasText(channelCode) 
					&& kfChannelCodes.indexOf(channelCode+",") >-1){
				
				allotFlag = 4;
			}
			
			now.put("insure", insure);//保险标志			
			now.put("allotFlag", allotFlag);//分单标识
			
			result = ApplyAllotUtil.saveBorrowApplyInfo(now, allotFlag);
			
			
		}catch(Exception e){
			result.setSuccess(false);
			StackTraceElement[] stackTrace = e.getStackTrace();
			if (stackTrace.length > 0) {
				String errorNumber = stackTrace[0].toString();
				result.setMessage(e.getMessage() + "|" + errorNumber);//记录一下是第几行报错
			}
			log.error("立即分单失败，参数：" +now, e);
		}
		return result ;
	}
	
	/**
	 * 计算分单成本公共方法
	 * @param param
	 */
	public static void computeAllotOrderCost(AppParam param){
		//String applyTime = StringUtil.getString(param.getAttr("applyTime"));
		String customerId = StringUtil.getString(param.getAttr("customerId"));
		String applyId = StringUtil.getString(param.getAttr("applyId"));
		//是否满足记录分单成本
//		boolean isCost = AllotCostUtil.isAllotCost(applyTime);
		boolean isCost = true;
		//保存记录到门店人员分单成本统计
		if(isCost){
			AppParam costParams = new AppParam("storeCostRecordService","insert");
			costParams.addAttr("customerId", customerId);
			costParams.addAttr("applyId", applyId);
			costParams.addAttr("orgId", param.getAttr("orgId"));
			costParams.addAttr("recordDate", DateUtil.getSimpleFmt(new Date()));
			Map<String,Object> baseMap = StoreSeparateUtils.getBaseConfig();//获取全局配置
			if(baseMap != null){
				costParams.addAttr("price", baseMap.get("allotPrice"));//分单成本
			}
			SoaManager.getInstance().invoke(costParams);
		}
	}
	
	/**
	 * 同步t_borrow_auth_info表的borrowApplyId
	 * 分单过程中用不到，可以去使用mq执行
	 * @param applyId
	 * @param borrowApplyId
	 */
	public static void updateAuthInfo (Object customerId, Object applyId) {
		try {
			if (StringUtils.isEmpty(customerId) || StringUtils.isEmpty(applyId)) {
				return;
			}
			Map<String, Object> param = new HashMap<String, Object>();
			param.put("service", "borrowAuthInfoService");
			param.put("method", "update");
			param.put("rmiServiceName", AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			param.put("customerId", customerId);
			param.put("applyId", applyId);
			RmiServiceSend send = SpringAppContext.getBean(RmiServiceSend.class);
			send.sendExecuteMessage(param);
		} catch (Exception e) {
			log.error("同步t_borrow_auth_info的applyId失败!", e);
		}
	}
	
	
	public static void saveRefererLoan (Map<String, Object> now,Object applyId, int allotFlag) {
		try {
			String referChannel = "referBorrow";
			if (!referChannel.equals(now.get("channelDetail")) || StringUtils.isEmpty(now.get("referer")) || StringUtils.isEmpty(now.get("applyId"))) {
				return;
			}
			int referStatus = (allotFlag == 1) ? 1 : 0; //代表复核门店要求代表推荐成功，是其他代考推荐失败
			AppParam param = new AppParam("daiReferRecordService", "save");
			param.addAttr("customerId", now.get("referer"));
			param.addAttr("applyName", now.get("applyName"));
			param.addAttr("telephone", now.get("telephone"));
			param.addAttr("applyId", applyId);
			param.addAttr("applyTime", now.get("applyTime"));
			param.addAttr("referStatus", referStatus);
			param.addAttr("allotFlag", allotFlag);
			SoaManager.getInstance().invoke(param);
		} catch (Exception e) {
			log.error("保存推荐记录失败!", e);
		}
	}
}
