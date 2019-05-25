/*
 * Copyright (c) 2013, OpenCloudDB/MyCAT and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software;Designed and Developed mainly by many Chinese 
 * opensource volunteers. you can redistribute it and/or modify it under the 
 * terms of the GNU General Public License version 2 only, as published by the
 * Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 * Any questions about this component can be directed to it's project Web address 
 * https://code.google.com/p/opencloudb/.
 *
 */
package org.ddq.common.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.TemporalUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.ddq.common.exception.SysException;
import org.springframework.util.StringUtils;

/**
 * 日期操作工具类
 * 
 * @author hx
 * 
 */
public class DateUtil {
	
	/**
	 *日期格式:yyyy-MM-dd
	 */
	public static final String DATE_PATTERN_YYYY_MM_DD = "yyyy-MM-dd";
	
	/**
	 *日期格式:yyyy-MM-dd
	 */
	public static final String DATE_PATTERN_YYYY_MM_DD_BACKSLASH = "yyyy/MM/dd";

	/**
	 *时间格式:yyyy-MM-dd HH:mm:ss
	 */
	public static final String DATE_PATTERN_YYYY_MM_DD_HHMMSS = "yyyy-MM-dd HH:mm:ss";
	
	/**
	 * 日期格式:yyyyMM
	 */
	public static final String DATE_PATTERN_YYYYMM = "yyyyMM";
	
	/**
	 *日期格式:yyyy-MM
	 */
	public static final String DATE_PATTERN_YYYY_MM = "yyyy-MM";
	
	/**
	 *时间格式:yyyy-MM-dd HH:mm
	 */
	public static final String DATE_PATTERN_YYYY_MM_DD_HHMM = "yyyy-MM-dd HH:mm";
	/**
	 * 时间戳格式，到毫秒 yyyy-MM-dd HH:mm:ss SSS
	 */
	public static final String DATE_PATTERN_YYYY_MM_DD_HHMMSS_SSS = "yyyy-MM-dd HH:mm:ss SSS";
	
	/**
	 *时间格式，到毫秒yyyyMMddHHmmssSSS
	 */
	public static final String DATE_PATTERNYYYYMMDDHHMMSSSSS = "yyyyMMddHHmmssSSS";
	
	/**
	 *时间格式，到日期yyyyMMdd
	 */
	public static final String DATE_PATTERN_YYYYMMDD = "yyyyMMdd";
	
	/**
	 *英文日期格式:MM/dd/yyyy
	 */
	public static final String DATE_EN_PATTERN_YYYY_MM_DD = "MM/dd/yyyy";
	
	/**
	 *英文时间格式:MM/dd/yyyy HH:mm
	 */
	public static final String DATE_EN_PATTERN_YYYY_MM_DD_HHMM = "MM/dd/yyyy HH:mm";
	
	/**天*/
	public static String DATE_TYPE_DD = "dd";
	/**时*/
	public static String DATE_TYPE_HOUR = "HH";
	/**分*/
	public static String DATE_TYPE_Minutes = "mm";

	/**区间查询 今天 */
	public static String DateRange_Today="1";
	/**区间查询  过去一周 */
	public static String DateRange_Last7Days="2";
	/**区间查询  一个月*/
	public static String DateRange_Last1Months="3";
	/**区间查询  二个月 */
	public static String DateRange_Last2Months="4";
	/**区间查询  三个月 */
	public static String DateRange_Last3Months="5";
	/**
	 * 1天折算成毫秒数
	 */
	public static final long MILLIS_A_DAY = 24 * 3600 * 1000;
	/**
	 * 毫秒
	 */
    public static final ChronoUnit ChronoUnit_MILLTS = ChronoUnit.MILLIS;
    /**
     * 秒
     */
    public static final ChronoUnit ChronoUnit_SECONDS = ChronoUnit.SECONDS;
    /**
     * 分钟
     */
    public static final ChronoUnit ChronoUnit_MINUTES = ChronoUnit.MINUTES;
    /**
     * 小时
     */
    public static final ChronoUnit ChronoUnit_HOURS = ChronoUnit.HOURS;
    /**
     * 半天
     */
    public static final ChronoUnit ChronoUnit_HALF_DAYS = ChronoUnit.HALF_DAYS;
    /**
     * 天
     */
    public static final ChronoUnit ChronoUnit_DAYS = ChronoUnit.DAYS;
    /**
     * 星期
     */
    public static final ChronoUnit ChronoUnit_WEEKS = ChronoUnit.WEEKS;
    /**
     * 月
     */
    public static final ChronoUnit ChronoUnit_MONTHS = ChronoUnit.MONTHS;
    /**
     * 年
     */
    public static final ChronoUnit ChronoUnit_YEARS = ChronoUnit.YEARS;
    
	private static Object lockObj = new Object();
    private static final Map<String, ThreadLocal<SimpleDateFormat>> sdfMap = new ConcurrentHashMap<String, ThreadLocal<SimpleDateFormat>>();
    
    /**
     * 返回一个ThreadLocal的sdf,每个线程只会new一次sdf
     * 
     * @param pattern
     * @return
     */
    private static SimpleDateFormat getDateParser(final String pattern) {
        ThreadLocal<SimpleDateFormat> threadLocal = sdfMap.get(pattern);
        // 此处的双重判断和同步是为了防止sdfMap这个单例被多次put重复的sdf
        if (threadLocal == null) {
            synchronized (lockObj) {
            	threadLocal = sdfMap.get(pattern);
                if (threadLocal == null) {
                    // 这里是关键,使用ThreadLocal<SimpleDateFormat>替代原来直接new SimpleDateFormat
                    threadLocal = new ThreadLocal<SimpleDateFormat>() {
                        @Override
                        protected SimpleDateFormat initialValue() {
                            return new SimpleDateFormat(pattern);
                        }
                    };
                    sdfMap.put(pattern, threadLocal);
                }
            }
        }
        return threadLocal.get();
    }
    
	/**
	 * 将java.util.Date对象转换为时间字符串，使用默认日期格式
	 * @param date
	 * @return
	 */
	public static String toStringByParttern(Date date,String parttern) {
		return getDateParser(parttern).format(date);
	}
	
	
	/**
	 * 将LocalDateTime对象转换为时间字符串，使用默认日期格式
	 * @param date
	 * @param partten
	 * @return
	 */
	public static String toStringByParttern(LocalDateTime localDateTime,String partten) {
		try {
			return localDateTime.format(DateTimeFormatter.ofPattern(partten));
		} catch (IllegalArgumentException e) {
			throw new SysException("日期格式不正确:" + localDateTime + " parttern:" + partten);
		}
	}
	
	/**
	 * 将localDate对象转换为时间字符串，使用默认日期格式
	 * @param date
	 * @param partten
	 * @return
	 */
	public static String toStringByParttern(LocalDate localDate,String partten) {
		try {
			return localDate.format(DateTimeFormatter.ofPattern(partten));
		} catch (IllegalArgumentException e) {
			throw new SysException("日期格式不正确:" + localDate + " parttern:" + partten);
		}
	}
	
	
	/**
	 * 将string转为Date
	 * @param time
	 * @param parttern
	 * </BR>	DATE_PATTERN_YYYY_MM_DD_HHMMSS
	 * </BR>	DATE_PATTERN_YYYY_MM_DD
	 * @return
	 */
    public static Date toDateByString(String time , String parttern) {
    	try {
    		Date date = getDateParser(parttern).parse(time);
			return date;
		} catch (ParseException e) {
			throw new SysException("日期格式不正确:" + time + " parttern:" + parttern);
		}
    }
    
    /**
     * 根据格式转华成 LocalDate 
     * @param dateStr
     * @param pattern
     * @return
     */
    public static LocalDate toLocalDate(String dateStr, String pattern) {
        return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(pattern));
    }
    
    /**
     * 根据格式转化成 LocalDateTime
     * @param dateStr
     * @param pattern
     * @return
     */
    public static LocalDateTime toLocalDateTime(String dateStr, String pattern) {
        return LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern(pattern));
    }
    
    /**
     * 将Date转换成LocalDate
     * @param Date
     * @return
     */
    public static LocalDate toLocalDateByDate(Date date) {
        Instant instant = date.toInstant();
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        return localDateTime.toLocalDate();
    }
    /**
     * 将Date转换成LocalDate
     * @param Date
     * @return
     */
    public static LocalDateTime toLocalDateTimeByDate(Date date) {
        Instant instant = date.toInstant();
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }
	
	/**
     * 将LocalDate转换成Date
     * @param localDate
     * @return date
     */
    public static Date toDateByLocalDate(LocalDate localDate) {
        Instant instant = localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
        return Date.from(instant);
    }
	
	/**
	 * 将LocalDateTime转为Date
	 */
	public static Date toDateByLocalDateTime(LocalDateTime localDateTime){
		 ZoneId zoneId = ZoneId.systemDefault();
	     ZonedDateTime zdt = localDateTime.atZone(zoneId);
	     Date date = Date.from(zdt.toInstant());
	     return date;
	}
	
	
	/**
	 * 日期加上一个数,根据field不同加不同值,field为ChronoUnit.*
	 * @param localDateTime
	 * @param number
	 * @param field
       <BR/>MILLIS		毫秒<BR/>
			SECONDS		秒;<BR/>
			MINUTES		分钟<BR/>
			HOURS		小时<BR/>
			HALF_DAYS	半天<BR/>
			DAYS		天<BR/>
			Weeks		星期<BR/>
			MONTHS		月<BR/>
			YEARS		年<BR/>
	 * @return
	 */
    public static Date plus(LocalDateTime localDateTime, long number, TemporalUnit field) {
        LocalDateTime ldTime = localDateTime.plus(number, field);
        return toDateByLocalDateTime(ldTime);
    }
    
    /**
     * 日期减去一个数,根据field不同加不同值,field为ChronoUnit.*
     * @param localDateTime
     * @param number
	 * @param field
       <BR/>MILLIS		毫秒<BR/>
			SECONDS		秒;<BR/>
			MINUTES		分钟<BR/>
			HOURS		小时<BR/>
			HALF_DAYS	半天<BR/>
			DAYS		天<BR/>
			Weeks		星期<BR/>
			MONTHS		月<BR/>
			YEARS		年<BR/>
     * @return
     */
    public static Date minu(LocalDateTime localDateTime, long number, TemporalUnit field){
    	LocalDateTime ldTime = localDateTime.minus(number,field);
    	return toDateByLocalDateTime(ldTime);
    }
    
	/**
	 * 获取日期 所在月份的 第几天
	 * @param localDateTime
	 * @return
	 */
	public static int getDayOfMonth(LocalDateTime localDateTime){
		if (localDateTime == null) {
			localDateTime = LocalDateTime.now();
		}
		return localDateTime.getDayOfMonth();
	}
	
	/**
	 * 根据日期及day替换日期
	 * @param date
	 * @return
	 */
	public static Date withDayOfMonth(LocalDateTime localDateTime,int day){
		if (localDateTime == null) {
			localDateTime = LocalDateTime.now();
		}
		LocalDateTime ldTime = localDateTime.withDayOfMonth(day);
		return toDateByLocalDateTime(ldTime);
	}
	
	/**
	 * 获取月最开始一秒日期 例 2017-12-31T00:00
	 * @param date
	 * @return
	 */
	public static Date getMonthFirstSecond(LocalDateTime localDateTime){
		if (localDateTime == null) {
			localDateTime = LocalDateTime.now();
		}
		LocalDateTime ldt = localDateTime.with(TemporalAdjusters.firstDayOfMonth())
								.with(LocalTime.MIN);
		return toDateByLocalDateTime(ldt);
	}
	
	/**
	 * 获取月最后一秒日期 例2017.12.31T59:59:59
	 * @param date
	 * @return
	 */
	public static Date getMonthLastSecond(LocalDateTime localDateTime){
		if (localDateTime == null) {
			localDateTime = LocalDateTime.now();
		}
		LocalDateTime ldt = localDateTime.with(TemporalAdjusters.lastDayOfMonth())
								.with(LocalTime.MAX);
		return toDateByLocalDateTime(ldt);
	}
	
	/**
	 * 获取下一月开始秒日期 例 2017-12-01T00:00
	 * @param date
	 * @return
	 */
	public static Date getNextMonthFirstSecond(LocalDateTime localDateTime){
		LocalDateTime ldt = localDateTime.withMonth(localDateTime.getMonthValue()+1).with(LocalTime.MIN);
		return toDateByLocalDateTime(ldt);
	}
	
	/**
	 * 获取一天开始时间 例 2017-12-31T00:00
	 * @param date
	 * @return
	 */
	public static Date getDayFirstSecond(LocalDateTime localDateTime){
		LocalDateTime ldt = localDateTime.with(LocalTime.MIN);
		return toDateByLocalDateTime(ldt);
	}
	
	/**
	 * 获取天数最后一秒日期 例2017-12-31T59:59:59
	 * @param date
	 * @return
	 */
	public static Date getDayLastSecond(LocalDateTime localDateTime){
		LocalDateTime ldt = localDateTime.with(LocalTime.MAX);
		return toDateByLocalDateTime(ldt);
	}
	

	
	/**
	 * 比较两个日期   单位 field的参数为ChronoUnit.*
	 * @param startTime
	 * @param endTime
	 * @param field
	 * @param field
       <BR/>MILLIS		毫秒<BR/>
			SECONDS		秒;<BR/>
			MINUTES		分钟<BR/>
			HOURS		小时<BR/>
			HALF_DAYS	半天<BR/>
			DAYS			天<BR/>
			Weeks		星期<BR/>
			MONTHS		月<BR/>
			YEARS		年<BR/>
	 * @return
	 * 
	 */
	public static long betweenTwoTime(LocalDateTime startTime, LocalDateTime endTime, ChronoUnit field){
		Period period = Period.between(LocalDate.from(startTime), LocalDate.from(endTime));
        if (field == ChronoUnit.YEARS){
        	return period.getYears();
        }
        if (field == ChronoUnit.MONTHS) {
        	return period.getYears() * 12 + period.getMonths();
        }
        return field.between(startTime, endTime);
	}
	
	/**
	 * 判断今天是否在两个日期期间内
	 * @param before
	 * @param after
	 * @return
	 */
	public static boolean twoDatePeriod(LocalDateTime before,LocalDateTime after){
		LocalDateTime now = LocalDateTime.now();
		return now.isAfter(before) && now.isAfter(after);
	}
	
	/**
	 * 转换时间戳为日期类型
	 * @param stamp
	 * @return
	 */
	public static String stampToDate(String stamp) {
		if(StringUtils.isEmpty(stamp)){
			return null;
		}
		long lt = new Long(stamp);
		Instant instant = Instant.ofEpochMilli(lt);
		ZoneId zone = ZoneId.systemDefault();
		LocalDateTime ldT = LocalDateTime.ofInstant(instant, zone);
		return toStringByParttern(ldT, DATE_PATTERN_YYYY_MM_DD_HHMMSS);
	}
	
	
	/**
	 * 获取区间时间
	 * 
	 * @param rangeType
	 * 			1,一天;2,一周前;3,一个月前; 4,二个月前; 5,三个月前;
	 * @return
	 */
	public static Date getDataByRange(String rangeType){
		Date date = new Date();
		if(DateRange_Today.equals(rangeType)){
			return date;
		}else if(DateRange_Last7Days.equals(rangeType)){
			date = DateUtil.minu(DateUtil.toLocalDateTimeByDate(
					date), 1,DateUtil.ChronoUnit_WEEKS);
		}else if(DateRange_Last1Months.equals(rangeType)){
			date = DateUtil.minu(DateUtil.toLocalDateTimeByDate(
					date), 1,DateUtil.ChronoUnit_MONTHS);
		}else if(DateRange_Last2Months.equals(rangeType)){
			date = DateUtil.minu(DateUtil.toLocalDateTimeByDate(
					date), 2,DateUtil.ChronoUnit_MONTHS);
		}else if(DateRange_Last3Months.equals(rangeType)){
			date = DateUtil.minu(DateUtil.toLocalDateTimeByDate(
					date), 3,DateUtil.ChronoUnit_MONTHS);
		}
		return date;
	}
	
	/**
	 * 获取相隔年数
	 * @param date
	 * @param days
	 * @return
	 */
	public static Date getNextYear(Date date, int year) {
		return  plus(DateUtil.toLocalDateTimeByDate(date), year, ChronoUnit_YEARS);
	}
	
	/**
	 * 获取相隔days天数
	 * @param date
	 * @param days
	 * @return
	 */
	public static Date getNextMonth(Date date, int month) {
		return  plus(DateUtil.toLocalDateTimeByDate(date), month, ChronoUnit_MONTHS);
	}
	
	/**
	 * 获取相隔days天数
	 * @param date
	 * @param days
	 * @return
	 */
	public static Date getNextDay(Date date, int days) {
		return  plus(DateUtil.toLocalDateTimeByDate(date), days, ChronoUnit_DAYS);
	}
	
	/**
	 * 获取相隔hour小时时间
	 * @param date
	 * @param hour
	 * @return
	 */
	public static Date getNextHour(Date date, int hour) {
		return  plus(DateUtil.toLocalDateTimeByDate(date), hour, ChronoUnit_HOURS);
	}
	
	/**
	 * 获取相隔seconds小时时间
	 * @param date
	 * @param seconds
	 * @return
	 */
	public static Date getNextSeconds(Date date, int seconds) {
		return  plus(DateUtil.toLocalDateTimeByDate(date), seconds, ChronoUnit_SECONDS);
	}
	
	/**
	 * 获取相隔minutes分钟时间
	 * @param date
	 * @param minutes
	 * @return
	 */
	public static Date getNextMinutes(Date date, int minutes) {
		return  plus(DateUtil.toLocalDateTimeByDate(date), minutes, ChronoUnit_MINUTES);
	}
	
	/**
	 * 获取当天最开始一秒日期
	 */
	public static Date getDayFirstSecond(Date date){
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.HOUR_OF_DAY , 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}
	
	/**
	 * 获取当天最后一秒日期
	 */
	public static Date getDayLastSecond(Date date){
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.HOUR_OF_DAY, 23);
		cal.set(Calendar.MINUTE, 59);
		cal.set(Calendar.SECOND, 59);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}
	
	/**
	 * 比较时间相差天数
	 */
	public static int diffDate(Date b1,Date b2){
		Date bf = getDayFirstSecond(b1);
		Date bl = getDayFirstSecond(b2);
		return (int)((bf.getTime() - bl.getTime())/(24*60*60*1000));
	}
	
	/**
	 * 获取日期简单格式
	 * @param date
	 * @return
	 */
	public static String getSimpleFmt(Date date){
		return toStringByParttern(date, DATE_PATTERN_YYYY_MM_DD);
	}
	
	/**
	 * 获取日期常用格式
	 * @param date
	 * @return
	 */
	public static String getNormalFmt(Date date){
		return toStringByParttern(date, DATE_PATTERN_YYYY_MM_DD_HHMMSS);
	}
	
	/***
	 * 小时取整
	 * @param date
	 * @param parttern
	 * @return
	 */
	public static Date getHourDate(String dateStr, String parttern){
		Date date = DateUtil.toDateByString(dateStr, parttern);
		Calendar c = Calendar.getInstance();
		if (date == null) {
			date = new Date();
		}
		c.setTime(date);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		return c.getTime();
	}
	
	/**
	 * 获取月最开始一秒日期
	 * @param date
	 * @return
	 */
	public static Date getMonthFirstSecond(Date date){
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}
	/**
	 * 获取上月最开始一秒日期
	 * @param date 当前日期
	 * @return
	 */
	public static Date getLastMonthFirstSecond(Date date){
		Calendar cal = Calendar.getInstance();
		cal.setTime(getNextMonth(date,-1));
		cal.set(Calendar.DAY_OF_MONTH, 1);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}
	
	/**
	 * 获取上月最后一秒日期
	 * @param date 当前日期
	 * @return
	 */
	public static Date getLastMonthEndSecond(Date date){
		Calendar cal = Calendar.getInstance();
		cal.setTime(getLastMonthFirstSecond(date));
		cal.add(Calendar.MONTH, 1);
		cal.add(Calendar.SECOND, -1);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}
	/**
	 * 获取月最后一秒日期
	 * @param date
	 * @return
	 */
	public static Date getMonthLastSecond(Date date){
		Calendar cal = Calendar.getInstance();
		cal.setTime(getMonthFirstSecond(date));
		cal.add(Calendar.MONTH, 1);
		cal.add(Calendar.SECOND, -1);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}
	
	/**
	 * 获取当月日期
	 * @param dayOfMonth :月的日期
	 * @return
	 */
	public static Date getCurMonthDate(int dayOfMonth){
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
		return cal.getTime();
	}

}