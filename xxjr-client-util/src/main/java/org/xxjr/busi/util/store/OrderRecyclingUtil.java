package org.xxjr.busi.util.store;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import org.ddq.common.util.DateUtil;
import org.ddq.common.util.StringUtil;
import org.springframework.util.StringUtils;


public class OrderRecyclingUtil {
	
	/**
	 * 获取没处理的订单分配时间
	 * @param 多久没处理
	 * @return
	 */
	public static String getNotDealAllotDate(int notHandl){
		String allotDate = "";
		long startTime = DateUtil.minu(LocalDateTime.now(), notHandl, DateUtil.ChronoUnit_MINUTES).getTime(); //job开始时间减去没处理时间
		//设置工作时间
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 9);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		long startWorkTime = cal.getTime().getTime(); //开始工作时间
		long nowTime = new Date().getTime();
		//设置休息时间
		Calendar calend = Calendar.getInstance();
		calend.set(Calendar.HOUR_OF_DAY, 12);
		calend.set(Calendar.MINUTE, 0);
		calend.set(Calendar.SECOND, 0);
		long relaxTime = calend.getTime().getTime(); //中午休息时间
		long amCount = startTime - startWorkTime;
		long pmCount= nowTime - relaxTime;
		if(amCount < 0){
			allotDate =
					DateUtil.toStringByParttern(DateUtil.minu(LocalDateTime.now(), 12*60 + notHandl, DateUtil.ChronoUnit_MINUTES)
					, DateUtil.DATE_PATTERN_YYYY_MM_DD_HHMM);
		}else{
			if(pmCount > 0){
				allotDate =
						DateUtil.toStringByParttern(DateUtil.minu(LocalDateTime.now(), 2*60 + notHandl, DateUtil.ChronoUnit_MINUTES)
						, DateUtil.DATE_PATTERN_YYYY_MM_DD_HHMM);
				
			}else{
				allotDate =
						DateUtil.toStringByParttern(DateUtil.minu(LocalDateTime.now(), notHandl, DateUtil.ChronoUnit_MINUTES)
						, DateUtil.DATE_PATTERN_YYYY_MM_DD_HHMM);
				
			}
		}
		return allotDate;
	}
	
	/**
	 * 获取没上门的订单分配时间
	 * @param 多久没上门
	 * @return
	 */
	public static String getNotVisitAllotDate(int notHandl){
		String allotDate = "";
		Calendar cal = Calendar.getInstance();
	    cal.setTime(new Date());
	    int weekDays = cal.get(Calendar.DAY_OF_WEEK) - 1;
	    if(notHandl == 2){
	    	if(weekDays == 1 || weekDays == 2){
	    		notHandl = notHandl + 2;
	    	}
	    	
	    } else if(notHandl == 3){
	    	if(weekDays == 1 || weekDays == 2 || weekDays == 3){
	    		notHandl = notHandl + 2;
	    	}
	    	
	    } else if(notHandl == 4){
	    	if(weekDays == 1 || weekDays == 2 || weekDays == 3 || weekDays == 4){
	    		notHandl = notHandl + 2;
	    	}
	    	
	    } else if(notHandl == 5){
	    	if(weekDays == 1 || weekDays == 2 || weekDays == 3 || weekDays == 4 || weekDays == 5){
	    		notHandl = notHandl + 2;
	    	}
	    	
	    }
		
		allotDate = DateUtil.toStringByParttern(DateUtil.getNextDay(new Date(), -notHandl), DateUtil.DATE_PATTERN_YYYY_MM_DD_HHMM);
		return allotDate;
	}
	
	
	
	/**
	 * 获取没处理的订单分配时间
	 * @param 多久没处理
	 * @return
	 */
	public static String getNotDealAllotDate2(int notHandl,Map<String, Object> resultMap){
		String allotDate = "";
		if(!StringUtils.isEmpty(resultMap)){
			String amBeginWorkTime = StringUtil.getString(resultMap.get("amBeginWorkTime"));
			long amStartTime = getLongTimes(StringUtils.isEmpty(amBeginWorkTime) ? "00:00" : amBeginWorkTime);
			String amEndWorkTime = StringUtil.getString(resultMap.get("amEndWordTime"));//中午下班时间
			String amEndWorkTimeStr = StringUtils.isEmpty(amEndWorkTime) ? "00:00" : amEndWorkTime;
			long amEndTime = getLongTimes(amEndWorkTimeStr);
			String pmBeginWorkTime = StringUtil.getString(resultMap.get("pmBeginWorkTime"));
			long pmStartTime = getLongTimes(StringUtils.isEmpty(pmBeginWorkTime) ? "00:00" : pmBeginWorkTime);
			String pmEndWorkTime = StringUtil.getString(resultMap.get("pmEndWordTime"));//下午下班时间
			String pmEndWorkTimeStr = StringUtils.isEmpty(pmEndWorkTime) ? "00:00" : pmEndWorkTime;
			long pmEndTime = getLongTimes(pmEndWorkTimeStr);
			String evesBeginWorkTime = StringUtil.getString(resultMap.get("evesBeginWorkTime"));
			String evesBeginWorkTimeStr = StringUtils.isEmpty(evesBeginWorkTime) ? "00:00" : evesBeginWorkTime;
			long evesStartTime = getLongTimes(evesBeginWorkTimeStr);
			String evesEndWorkTime = StringUtil.getString(resultMap.get("evesEndWordTime"));//下班时间
			String evesEndWorkTimeStr = StringUtils.isEmpty(evesEndWorkTime) ? "00:00":evesEndWorkTime;
			long evesEndTime = getLongTimes(evesEndWorkTimeStr);
			
			Date date = new Date();
			//上午下班时间
			String amEndTimeStr = DateUtil.toStringByParttern(date, DateUtil.DATE_PATTERN_YYYY_MM_DD) +" "+amEndWorkTimeStr;
			Date amEndDate = DateUtil.toDateByString(amEndTimeStr, DateUtil.DATE_PATTERN_YYYY_MM_DD_HHMM);
			//前一天下午下班时间
			String yesterPmEndTimeStr = DateUtil.toStringByParttern(DateUtil.getNextDay(date, -1), DateUtil.DATE_PATTERN_YYYY_MM_DD) +" "+pmEndWorkTimeStr;
			Date yesterPmEndTime = DateUtil.toDateByString(yesterPmEndTimeStr, DateUtil.DATE_PATTERN_YYYY_MM_DD_HHMM);
			//前一天晚上上班时间
			String yesterEvesStartTimeStr = DateUtil.toStringByParttern(DateUtil.getNextDay(date, -1), DateUtil.DATE_PATTERN_YYYY_MM_DD) +" "+evesBeginWorkTimeStr;
			Date yesterEvesStartTime = DateUtil.toDateByString(yesterEvesStartTimeStr, DateUtil.DATE_PATTERN_YYYY_MM_DD_HHMM);
			//前一天晚上下班时间
			String yesterEvesEndTimeStr = DateUtil.toStringByParttern(DateUtil.getNextDay(date, -1), DateUtil.DATE_PATTERN_YYYY_MM_DD) +" "+evesEndWorkTimeStr;
			Date yesterEvesEndTime = DateUtil.toDateByString(yesterEvesEndTimeStr, DateUtil.DATE_PATTERN_YYYY_MM_DD_HHMM);
			
			long nowTime = date.getTime();//当前时间
			//判断当前当前是否早上工作时间
			Date nextDateTime = DateUtil.minu(DateUtil.toLocalDateTimeByDate(date), notHandl, DateUtil.ChronoUnit_MINUTES);
			long allotTime = nextDateTime.getTime(); //订单分配时间
			if(!StringUtils.isEmpty(amBeginWorkTime) && !StringUtils.isEmpty(amEndWorkTime) && nowTime > amStartTime && nowTime <= amEndTime){//上午工作时间
				Date amBeginDate = getBeginDate(date,amBeginWorkTime);
				if(allotTime < amStartTime){//分单时间小于开始工作时间   
					allotDate = getWorkAllotDate2(nowTime,amBeginDate,notHandl,DateUtil.getNextDay(date, -1),StringUtils.isEmpty(evesEndWorkTime) == true ? pmEndWorkTime : evesEndWorkTime,yesterEvesStartTime.getTime(),yesterPmEndTime);
				}else{
					allotDate = DateUtil.toStringByParttern(nextDateTime, DateUtil.DATE_PATTERN_YYYY_MM_DD_HHMM);
				}
			}else if(!StringUtils.isEmpty(pmBeginWorkTime) && !StringUtils.isEmpty(pmEndWorkTime) && nowTime > pmStartTime && nowTime <= pmEndTime){//下午工作时间
				Date pmBeginDate = getBeginDate(date,pmBeginWorkTime);
				if(allotTime < pmStartTime){
					allotDate = getWorkAllotDate2(nowTime,pmBeginDate,notHandl,date,amEndWorkTime,amStartTime,yesterEvesEndTime);
				}else{
					allotDate = DateUtil.toStringByParttern(nextDateTime, DateUtil.DATE_PATTERN_YYYY_MM_DD_HHMM);
				}
			}else if(!StringUtils.isEmpty(evesBeginWorkTime) && !StringUtils.isEmpty(evesEndWorkTime) && nowTime > evesStartTime && nowTime <= evesEndTime){//晚上工作时间
				Date evesBeginDate = getBeginDate(date,evesBeginWorkTime);
				if(allotTime < evesStartTime){
					allotDate = getWorkAllotDate2(nowTime,evesBeginDate,notHandl,date,pmEndWorkTime,pmStartTime,amEndDate);
				}else{
					allotDate = DateUtil.toStringByParttern(nextDateTime, DateUtil.DATE_PATTERN_YYYY_MM_DD_HHMM);
				}
			}else{//非工作时间
				if((StringUtils.isEmpty(amBeginWorkTime) && StringUtils.isEmpty(amEndWorkTime)) || 
						(nowTime <= amStartTime)){//早上非上班时间
					allotDate = getRestAllotDate2(DateUtil.getNextDay(date, -1),evesEndWorkTime,notHandl,yesterEvesStartTime.getTime(),pmEndWorkTime);
				}else if((StringUtils.isEmpty(pmBeginWorkTime) && StringUtils.isEmpty(pmEndWorkTime)) || 
						(nowTime > amEndTime && nowTime <= pmStartTime)){//中午午休时间
					allotDate = getRestAllotDate2(date,amEndWorkTime,notHandl,amStartTime,evesEndWorkTime);
				}else if((StringUtils.isEmpty(evesBeginWorkTime) && StringUtils.isEmpty(evesEndWorkTime)) || 
						(nowTime > pmEndTime && nowTime <= evesStartTime)){//晚上休息时间
					allotDate = getRestAllotDate2(date,pmEndWorkTime,notHandl,pmStartTime,evesEndWorkTime);
				}else if(nowTime > evesEndTime){
					allotDate = getRestAllotDate2(date,evesEndWorkTime,notHandl,evesStartTime,pmEndWorkTime);
				}
			}
		}
		return allotDate;
	}
	
	
	/**
	 * 获取消息通知分配时间
	 * @param allotDate
	 * @return
	 */
	public static String getMessageNitoceAlloDate(Map<String, Object> resultMap,Date tempDate){
		String allotDate = "";
		String amBeginWorkTime = StringUtil.getString(resultMap.get("amBeginWorkTime"));
		long amStartTime = getLongTimes(StringUtils.isEmpty(amBeginWorkTime) ? "00:00" : amBeginWorkTime);
		String amEndWorkTime = StringUtil.getString(resultMap.get("amEndWordTime"));//中午下班时间
		long amEndTime = getLongTimes(StringUtils.isEmpty(amEndWorkTime) ? "00:00" : amEndWorkTime);
		String pmBeginWorkTime = StringUtil.getString(resultMap.get("pmBeginWorkTime"));
		long pmStartTime = getLongTimes(StringUtils.isEmpty(pmBeginWorkTime) ? "00:00" : pmBeginWorkTime);
		String pmEndWorkTime = StringUtil.getString(resultMap.get("pmEndWordTime"));//下午下班时间
		long pmEndTime = getLongTimes(StringUtils.isEmpty(pmEndWorkTime) ? "00:00" : pmEndWorkTime);
		String evesBeginWorkTime = StringUtil.getString(resultMap.get("evesBeginWorkTime"));
		long evesStartTime = getLongTimes(StringUtils.isEmpty(evesBeginWorkTime) ? "00:00" : evesBeginWorkTime);
		String evesEndWorkTime = StringUtil.getString(resultMap.get("evesEndWordTime"));//晚上下班时间
		long evesEndTime = getLongTimes(StringUtils.isEmpty(evesEndWorkTime) ? "00:00":evesEndWorkTime);
		//早上上班时间
		Date amBeginDate = getWorkDate(amBeginWorkTime);
		//中午上班时间
		Date pmBeginDate = getWorkDate(pmBeginWorkTime);
		//晚上上班时间
		Date evesBeginDate = getWorkDate(evesBeginWorkTime);
		//前一天下班时间
		String yesterDayStr = DateUtil.toStringByParttern(DateUtil.getNextDay(new Date(), -1), DateUtil.DATE_PATTERN_YYYY_MM_DD) +" "+(StringUtils.isEmpty(evesEndWorkTime) ? "00:00":evesEndWorkTime);
		Date yesterDate = DateUtil.toDateByString(yesterDayStr, DateUtil.DATE_PATTERN_YYYY_MM_DD_HHMM);
		Date nowDateTime = getWorkDate( DateUtil.toStringByParttern(tempDate, "HH:mm"));
		Date nextHour  = DateUtil.plus(DateUtil.toLocalDateTimeByDate(nowDateTime), 1, DateUtil.ChronoUnit_HOURS);
		long nowTime = nextHour.getTime();
		if((StringUtils.isEmpty(amBeginWorkTime) && StringUtils.isEmpty(amEndWorkTime)) || 
				(nowTime > yesterDate.getTime() && nowTime <= amStartTime)){//前一天晚上下班到今天早上上班之前的休息时间
			allotDate = getRestAllotDate(nowTime,yesterDate.getTime(),amBeginDate);
		}else if((StringUtils.isEmpty(pmBeginWorkTime) && StringUtils.isEmpty(pmEndWorkTime)) || 
				(nowTime > amEndTime && nowTime <= pmStartTime)){//中午午休时间
			allotDate = getRestAllotDate(nowTime,amEndTime,pmBeginDate);
		}else if((StringUtils.isEmpty(evesBeginWorkTime) && StringUtils.isEmpty(evesEndWorkTime)) || 
				(nowTime > pmEndTime && nowTime <= evesStartTime)){//晚上午休时间
			allotDate = getRestAllotDate(nowTime,pmEndTime,evesBeginDate);
		}else if(nowTime > evesEndTime){
			allotDate = getRestAllotDate(nowTime,evesEndTime,amBeginDate);
		}else{
			allotDate = DateUtil.toStringByParttern(
					DateUtil.plus(DateUtil.toLocalDateTimeByDate(tempDate), 1, DateUtil.ChronoUnit_HOURS), DateUtil.DATE_PATTERN_YYYY_MM_DD_HHMM);
		}
		return allotDate;
	}
	
	
	/**
	 * 时间转换成long类型
	 * @param workTime
	 * @return
	 */
	public static long getLongTimes(String workTime){
		String[] timeStr = workTime.split(":");
		long time = 0L;
		if(timeStr.length > 0){
			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.HOUR_OF_DAY, Integer.valueOf(timeStr[0]));
			cal.set(Calendar.MINUTE, Integer.valueOf(timeStr[1]));
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND,0);                                                                                                                                                                                       
			time = cal.getTime().getTime(); 
		}
		return time;
	}
	
	/**
	 * 获取工作时分配时间
	 * @param nowTime
	 * @param beginDate
	 * @param notHandl
	 * @param toStrDate
	 * @param workTimes
	 * @return
	 */
	public static String getWorkAllotDate(long nowTime,Date beginDate,int notHandl,Date toStrDate,String workTimes){
		String allotDate = "";
		//订单已处理时间 = 当前时间 - 开始工作时间
		int diffMinute = (int) ((nowTime - beginDate.getTime())/(1000 * 60));
		//订单剩余处理时间 = 处理时间  - 订单已处理时间
		int count = notHandl - diffMinute;
		String dayStr = DateUtil.toStringByParttern(toStrDate, DateUtil.DATE_PATTERN_YYYY_MM_DD) +" "+workTimes;
		
		//分配时间 = 前一个下班时间 - 订单剩余处理时间
		allotDate = DateUtil.toStringByParttern(DateUtil.minu(DateUtil.toLocalDateTime(dayStr, DateUtil.DATE_PATTERN_YYYY_MM_DD_HHMM), 
				count, DateUtil.ChronoUnit_MINUTES),
					DateUtil.DATE_PATTERN_YYYY_MM_DD_HHMM);
		return allotDate;
	}
	
	
	public static String getWorkAllotDate2(long nowTime,Date beginDate,int notHandl,Date toStrDate,String workTimes,long startTime,Date endTime){
		String allotDate = "";
		//订单已处理时间 = 当前时间 - 开始工作时间
		int diffMinute = (int) ((nowTime - beginDate.getTime())/(1000 * 60));
		//订单剩余处理时间 = 处理时间  - 订单已处理时间
		int count = notHandl - diffMinute;
		String dayStr = DateUtil.toStringByParttern(toStrDate, DateUtil.DATE_PATTERN_YYYY_MM_DD) +" "+workTimes;
		
		//分配时间 = 前一个下班时间 - 订单剩余处理时间
		allotDate = DateUtil.toStringByParttern(
				DateUtil.minu(DateUtil.toLocalDateTime(dayStr, DateUtil.DATE_PATTERN_YYYY_MM_DD_HHMM), 
						count, DateUtil.ChronoUnit_MINUTES)
				, DateUtil.DATE_PATTERN_YYYY_MM_DD_HHMM);
		Date allotDate2 = DateUtil.toDateByString(allotDate, DateUtil.DATE_PATTERN_YYYY_MM_DD_HHMM);
			//时间差 = 晚上上 班班时间  - 当前分配时间 
		if(allotDate2.getTime() < startTime){
			int diffTime = (int) ((startTime - allotDate2.getTime())/(1000 * 60));
			//分配时间 = 下午下班时间 - 时间差
			allotDate = DateUtil.toStringByParttern(
					DateUtil.minu(DateUtil.toLocalDateTimeByDate(endTime), 
							diffTime, DateUtil.ChronoUnit_MINUTES), DateUtil.DATE_PATTERN_YYYY_MM_DD_HHMM);
		}
		return allotDate;
	}
	
	/**
	 * 获取休息是分配时间
	 * @param toStrDate 
	 * @param timeWorks 上一个上班结束时间
	 * @param notHandl 处理时间
	 * @return
	 */
	public static String getRestAllotDate(Date toStrDate,String timeWorks,int notHandl){
		String allotDate = "";
		String dayStr = DateUtil.toStringByParttern(toStrDate, DateUtil.DATE_PATTERN_YYYY_MM_DD) +" "+timeWorks;
		Date dayDate = DateUtil.toDateByString(dayStr, DateUtil.DATE_PATTERN_YYYY_MM_DD_HHMM);
		//休息时间段分配时间 = 前一个下班时间 - 处理时间
		allotDate = DateUtil.toStringByParttern(
				DateUtil.minu(DateUtil.toLocalDateTimeByDate(dayDate), 
						notHandl, DateUtil.ChronoUnit_MINUTES), DateUtil.DATE_PATTERN_YYYY_MM_DD_HHMM);
		return allotDate;
	}
	
	public static String getRestAllotDate2(Date toStrDate,String timeWorks,int notHandl,long startTime,String endWoreTimes2){
		String allotDate = "";
		allotDate = getRestAllotDate(toStrDate,timeWorks,notHandl);
		Date allotDate2 = DateUtil.toDateByString(allotDate, DateUtil.DATE_PATTERN_YYYY_MM_DD_HHMM);
		if(allotDate2.getTime() <  startTime){
			int diffTime = (int) ((startTime - allotDate2.getTime())/(1000 * 60));
			allotDate = getRestAllotDate(DateUtil.getNextDay(toStrDate, -1),endWoreTimes2,diffTime);
		}
		return allotDate;
	}
	
	/**
	 * 获取各时间段开始工作时间
	 * @param date
	 * @param timeWorks 工作时间
	 * @return
	 */
	public static Date getBeginDate(Date date,String timeWorks){
		String beginDateStr = DateUtil.toStringByParttern(date, DateUtil.DATE_PATTERN_YYYY_MM_DD) +" "+timeWorks;
		Date beginDate = DateUtil.toDateByString(beginDateStr, DateUtil.DATE_PATTERN_YYYY_MM_DD_HHMM);
		return beginDate;
	}
	
	/**
	 * 获取上下班时间
	 * @param beginWorkTime
	 * @return
	 */
	public static Date getWorkDate(String beginWorkTime){
		String beginDateStr = DateUtil.toStringByParttern(new Date(), DateUtil.DATE_PATTERN_YYYY_MM_DD) +" "+(StringUtils.isEmpty(beginWorkTime) ? "00:00" : beginWorkTime);
		Date beginDate = DateUtil.toDateByString(beginDateStr, DateUtil.DATE_PATTERN_YYYY_MM_DD_HHMM);
		return beginDate;
	}
	
	
	/**
	 * 获取休息是分配时间
	 * @param toStrDate 
	 * @param timeWorks 上一个上班结束时间
	 * @param notHandl 处理时间
	 * @return
	 */
	public static String getRestAllotDate(long nowTime,long endTime,Date date){
		String allotDate = "";
		//超出下班的时间 = 当前时间 - 下班时间
		int exceedMinute = (int) ((nowTime - endTime)/(1000 * 60));
		//分配时间 = 上班时间  + 超出时间
		allotDate = DateUtil.toStringByParttern(
				DateUtil.plus(DateUtil.toLocalDateTimeByDate(date), 
						exceedMinute, DateUtil.ChronoUnit_MINUTES),
				DateUtil.DATE_PATTERN_YYYY_MM_DD_HHMM);
		return allotDate;
	}
}
