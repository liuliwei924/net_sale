package org.llw.com.util;

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
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.TemporalUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.llw.com.exception.SysException;
import org.springframework.util.StringUtils;

/**
 * 日期操作工具类
 * 
 * @author yuany
 * 
 */
public class DateTimeUtil {
	/**
	 *日期格式：yyyy-MM
	 */
	public static final String DATE_PATTERN_YYYY_MM = "yyyy-MM";

	/**
	 *日期格式：yyyy-MM-dd
	 */
	public static final String DATE_PATTERN_YYYY_MM_DD = "yyyy-MM-dd";

	/**
	 *日期格式：yyyy-MM-dd
	 */
	public static final String DATE_PATTERN_YYYY_MM_DD_BACKSLASH = "yyyy/MM/dd";

	/**
	 *时间格式：yyyy-MM-dd HH:mm:ss
	 */
	public static final String DATE_PATTERN_YYYY_MM_DD_HHMMSS = "yyyy-MM-dd HH:mm:ss";

	/**
	 *时间格式：yyyy-MM-dd HH:mm
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
	 *英文日期格式：MM/dd/yyyy
	 */
	public static final String DATE_EN_PATTERN_YYYY_MM_DD = "MM/dd/yyyy";

	/**
	 *英文时间格式：MM/dd/yyyy HH:mm
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
	 * 将Date对象转换为时间字符串，使用默认日期格式
	 * @param date
	 * @return
	 */
	public static String toStringByParttern(Date date, String parttern) {
		return getDateParser(parttern).format(date);
	}

	/**
	 * 将LocalDateTime对象转换为时间字符串，使用默认日期格式
	 * @param date
	 * @param partten
	 * @return
	 */
	public static String toStringByParttern(LocalDateTime localDateTime, String partten) {
		try {
			return localDateTime.format(DateTimeFormatter.ofPattern(partten));
		} catch (IllegalArgumentException e) {
			throw new SysException("日期格式不正确：" + localDateTime + " parttern:" + partten);
		}
	}


	/**
	 * 根据时间格式返回系统当前的时间
	 * @param parttern
	 * @return
	 */
	public static String getCurTimeByParttern(String parttern) {
		LocalDateTime now = LocalDateTime.now();
		return toStringByParttern(now,parttern);
	}

	/***
	 * 判断是否为闰年
	 * @param localDate
	 * @return
	 */
	public static boolean isLeapYear(LocalDate localDate){
		return localDate.isLeapYear();
	}


	/**
	 * 将时间字符串转换为LocalDateTime对象
	 * @param dateString
	 * yyyy-MM-dd HH:mm:ss
	 * @return
	 * @throws Exception
	 */
	public static LocalDateTime toLocalDateTimeByString(String dateString) {
		DateTimeFormatter df = DateTimeFormatter.ofPattern(DATE_PATTERN_YYYY_MM_DD_HHMMSS);
		LocalDateTime ldt = LocalDateTime.parse(dateString,df);
		return ldt;
	}
	
	/**
	 * 将时间字符串转换为LocalDateTime对象
	 * @param dateString
	 * yyyy-MM-dd HH:mm:ss
	 * @return
	 * @throws Exception
	 */
	public static LocalDateTime toLocalDateTimeByString(String dateString, String parttern) {
		DateTimeFormatter df = DateTimeFormatter.ofPattern(parttern);
		LocalDateTime ldt = LocalDateTime.parse(dateString,df);
		return ldt;
	}


	/**
	 * 将Date转为LocalDateTime
	 * @param date
	 * @return
	 */
	public static LocalDateTime toLocalDateTimeByDate(Date date) {
		Instant instant = date.toInstant();
		ZoneId zoneId = ZoneId.systemDefault();
		LocalDateTime localDateTime = instant.atZone(zoneId).toLocalDateTime();
		return localDateTime;
	}

	/**
	 * 将string转为Date
	 * @param time
	 * @param parttern
	 * </BR>	DATE_PATTERN_YYYY_MM_DD_HHMMSS
	 * </BR>	DATE_PATTERN_YYYY_MM_DD
	 * @return
	 */
	public static Date toDateByString(String time, String parttern) {
		try {
			DateTimeFormatter f = DateTimeFormatter.ofPattern(parttern);
			if (DATE_PATTERN_YYYY_MM_DD_HHMMSS.equals(parttern)) {
				return DateTimeUtil.toDateByLocalDateTime(LocalDateTime.parse(time, f));
			} else if (DATE_PATTERN_YYYY_MM_DD.equals(parttern)){
				return DateTimeUtil.toDateByLocalDate(LocalDate.parse(time, f));
			}else{
				return getDateParser(parttern).parse(time);
			}
		} catch (DateTimeParseException | ParseException e) {
			throw new SysException("日期格式不正确：" + time + " parttern:" + parttern);
		}
	}

	/**
	 * 字符串转LocalDate
	 * 说明:传入的格式必须与字符串一致
	 * @param dateStr
	 * @param pattern
	 */
	public static LocalDate toLocalDateByString(String dateStr, String pattern) {
		LocalDate localDate = null;
		try{
			localDate = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(pattern));
		}catch (Exception e) {
			throw new SysException("日期格式不正确：传入的格式必须与字符串一致");
		}
		return localDate;
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
	 * 获取日期 的day值
	 * @param date
	 * @return
	 */
	public static int getDayByDate(LocalDateTime localDateTime){
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
	public static Date exchangeDateByDay(LocalDateTime localDateTime,int day){
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
	 * 获取今天开始时间 例 2017-12-31T00:00
	 * @return
	 */
	public static Date getTodayFirstTime(){
		LocalDateTime ldt = LocalDate.now().atStartOfDay();
		return toDateByLocalDateTime(ldt);
	}

	/**
	 * 获取今天结束时间 例2017-12-31T59:59:59
	 * @return
	 */
	public static Date getTodayLastTime(){
		LocalDateTime ldt = LocalDate.now().atTime(23,59,59);
		return toDateByLocalDateTime(ldt);
	}

	/**
	 * 获取当月日期
	 * @param dayOfMonth :月的日期
	 * @return
	 */
	public static Date getCurMonthDate(int dayOfMonth){
		LocalDateTime ldt = LocalDateTime.now().withDayOfMonth(dayOfMonth);
		return toDateByLocalDateTime(ldt);
	}

	/**
	 * 比较两个日期   单位 field的参数为ChronoUnit.*
	 * @param startTime
	 * @param endTime
	 * @param field
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
	 * 获取相隔多少秒时间
	 * @param date
	 * @param minutes
	 * @return
	 */
	public static Date getNextSeconds(Date date, int seconds) {
		return plus(toLocalDateTimeByDate(date), seconds, ChronoUnit_SECONDS);
	}
	

	/**
	 * 获取相隔minutes分钟时间
	 * @param date
	 * @param minutes
	 * @return
	 */
	public static Date getNextMinutes(Date date, int minutes) {
		return plus(toLocalDateTimeByDate(date), minutes, ChronoUnit_MINUTES);
	}

	/**
	 * 获取相隔days天数
	 * @param date
	 * @param days
	 * @return
	 */
	public static Date getNextDays(Date date, int days) {
		return plus(toLocalDateTimeByDate(date), days, ChronoUnit_DAYS);
	}
	
	/**
	 * 获取相隔月份日期
	 * @param date
	 * @param days
	 * @return
	 */
	public static Date getNextMonths(Date date, int months) {
		return plus(toLocalDateTimeByDate(date), months, ChronoUnit_MONTHS);
	}
	
	
	/**
	 * 日期加上一个数,根据field不同加不同值,field为ChronoUnit.*
	 * @param localDateTime
	 * @param number
	 * @param field
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
	 * @return
	 */
	public static Date minu(LocalDateTime localDateTime, long number, TemporalUnit field){
		LocalDateTime ldTime = localDateTime.minus(number,field);
		return toDateByLocalDateTime(ldTime);
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

}