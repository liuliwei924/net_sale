package org.xxjr.cust.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.ddq.common.util.LogerUtil;
import org.springframework.util.StringUtils;

/**   
 * 身份证号码验证    
 * 1、号码的结构   
 * 公民身份号码是特征组合码，由十七位数字本体码和一位校验码组成。排列顺序从左至右依次为：六位数字地址码，   
 * 八位数字出生日期码，三位数字顺序码和一位数字校验码。   
 * 2、地址码(前六位数）    
 * 表示编码对象常住户口所在县(市、旗、区)的行政区划代码，按GB/T2260的规定执行。    
 * 3、出生日期码（第七位至十四位）   
 * 表示编码对象出生的年、月、日，按GB/T7408的规定执行，年、月、日代码之间不用分隔符。    
 * 4、顺序码（第十五位至十七位）   
 * 表示在同一地址码所标识的区域范围内，对同年、同月、同日出生的人编定的顺序号，   
 * 顺序码的奇数分配给男性，偶数分配给女性。    
 * 5、校验码（第十八位数）   
 * （1）十七位数字本体码加权求和公式 S = Sum(Ai * Wi), i = 0,  , 16 ，先对前17位数字的权求和   
 * Ai:表示第i位置上的身份证号码数字值 Wi:表示第i位置上的加权因子 Wi: 7 9 10 5 8 4 2 1 6 3 7 9 10 5 8 4   
 * 2 （2）计算模 Y = mod(S, 11) （3）通过模得到对应的校验码 Y: 0 1 2 3 4 5 6 7 8 9 10 校验码: 1 0   
 * X 9 8 7 6 5 4 3 2   
 * 功能：身份证的有效验证
 /** 
 * @param IDStr
 *            身份证号
 * @return 有效：返回"" 无效：返回String信息
 * @throws ParseException
 */
public class IDCardValidate {

	public static final String ACCEPT = ""; // 检查通过是返回的的成功标识字符串  

	public static final int EIGHTEEN_IDCARD = 18;   //标识18位身份证号码  
	public static final int FIFTEEN_IDCARD = 15;    //标识15位身份证号码  

	public static final int MAX_MAINLAND_AREACODE = 659004; //大陆地区地域编码最大值  
	public static final int MIN_MAINLAND_AREACODE = 110000; //大陆地区地域编码最小值  
	public static final int HONGKONG_AREACODE = 810000; //香港地域编码值  
	public static final int TAIWAN_AREACODE = 710000;   //台湾地域编码值  
	public static final int MACAO_AREACODE = 820000;    //澳门地域编码值  

	//储存18位身份证校验码  
	private static final String[] SORTCODES = new String[]{"1","0","X","9","8","7","6","5","4","3","2"}; 
	private static final Integer[] a = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};


	/**
	 * 验证身份证号码或后6位
	 */
	public static String validCardNoOrLast6(String cardNo){
		int length = cardNo.length();
		if(length != 6 && length != 18){
			return "身份证号码不正确，系统只支持18位的新身份证或后6位!";
		}
		if(length == 6){
			return validCardNoLast6(cardNo);
		}else{
			return validCardNo(cardNo);
		}
	}

	/**
	 * 验证身份证后6位主方法
	 */
	public static String validCardNoLast6(String cardNo){
		if (cardNo.length() != 6) {
			return "身份证号码不正确，系统只支持验证身份证号码后6位";
		}
		char[] chars = cardNo.toCharArray();  
		for( int i = 0; i < chars.length; i++ ) {  
			if( i < chars.length-1 ){  
				if( chars[i] > '9' )  
					return "身份证号码后6位中前5位不能出现字母";  
			} else {  
				if( chars[i] > '9' && chars[i] != 'X')  
					return "身份证号码后6位中最后一位只能是数字0~9或字母X";  
			}  
		}       

		int dayCode = Integer.parseInt(cardNo.substring(0, 2));
		if(dayCode > 31){
			return "身份证号码后6位中前两位不能大于31";
		}
		return ACCEPT;  
	}

	/** 
	 * 验证身份证主方法 
	 */  
	public static String validCardNo(String cardNo ){  
		int sortCode = 0;
		int MAN_SEX = 0;
		if (cardNo.length() == 18) {
			sortCode = Integer.parseInt(cardNo.substring(14, 17));
		} else {
			return "身份证号码不正确，系统只支持18位的新身份证!";
		}
		if (sortCode % 2 != 0) {
			MAN_SEX = 1;// 男性身份证
		} else if (sortCode % 2 == 0) {
			MAN_SEX = 2;// 女性身份证
		} else {
			return  "身份证号码不正确，系统只支持18位的新身份证!";
		}
		return checkIdCard18(MAN_SEX,cardNo); 
	}  


	/***
	 * 根据身份证获取性别
	 * 前提已经校验过身份证的合法性
	 * @param cardNo
	 * @return
	 */
	public static String getCardSex(String cardNo ){  
		try {
			if (!StringUtils.isEmpty(cardNo) && cardNo.length() == 18) {
				int sortCode = Integer.parseInt(cardNo.substring(14, 17));;
				int MAN_SEX = 0;
				if (sortCode % 2 != 0) {
					MAN_SEX = 1;// 男性身份证
				} else if (sortCode % 2 == 0) {
					MAN_SEX = 2;// 女性身份证
				} else {
					return  null;
				}
				return MAN_SEX==1?"1":"0";
			}else {
				return null;
			}
		} catch (Exception e) {
			LogerUtil.error(IDCardValidate.class, e, "getCardSex error");
		}
		return null;
	} 


	/***
	 * 根据身份证获取年龄
	 * 前提已经校验过身份证的合法性
	 * @param cardNo
	 * @return
	 */
	public static Integer getCardAge(String cardNo){  
		Integer age = null; 
		try {
			if (!StringUtils.isEmpty(cardNo) && cardNo.length() == 18) {
				String year = cardNo.substring(6).substring(0, 4);// 得到年份  
				String month = cardNo.substring(10).substring(0, 2);// 得到月份  
				Date date = new Date();// 得到当前的系统时间  
				
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");  
				String curYear = format.format(date).substring(0, 4);// 当前年份  
				String curMonth = format.format(date).substring(5, 7);//当前月份  

				 
				if (Integer.parseInt(month) <= Integer.parseInt(curMonth)) { // 当前月份大于用户出身的月份表示已过生  
					age = Integer.parseInt(curYear) - Integer.parseInt(year) + 1;  
				} else {// 当前用户还没过生  
					age = Integer.parseInt(curYear) - Integer.parseInt(year);  
				} 
			} 
		} catch (Exception e) {
			LogerUtil.error(IDCardValidate.class, e, "getCardAge error");
		}
		return age; 
	} 

	/** 
	 * 验证身份证主方法 
	 */  
	public static String chekIdCard( int sex,String idCardInput ){  
		return checkIdCard18(sex,idCardInput); 
	}  


	/** 
	 * 验证18位身份证号码 
	 */  
	private static String checkIdCard18( int sex, String idCardInput ){  

		String numberResult = checkNumber(EIGHTEEN_IDCARD,idCardInput);  
		if( !ACCEPT.equals(numberResult))  
			return numberResult;  

		String areaResult = checkArea(idCardInput);  
		if( !ACCEPT.equals(areaResult))  
			return areaResult;  

		String birthResult = checkBirthDate( EIGHTEEN_IDCARD, idCardInput);  
		if( !ACCEPT.equals(birthResult))  
			return birthResult;  

		//        String sortCodeResult = checkSortCode(EIGHTEEN_IDCARD,sex,idCardInput);  
		//        if( !ACCEPT.equals(sortCodeResult))  
		//            return sortCodeResult;  

		String checkCodeResult = checkCheckCode(EIGHTEEN_IDCARD,idCardInput);  
		if( !ACCEPT.equals(checkCodeResult))  
			return checkCodeResult;  

		return ACCEPT;  
	}  

	/** 
	 * 验证身份证的地域编码是符合规则 
	 */  
	private static String checkArea( String idCardInput ){  
		String subStr = idCardInput.substring(0, 6);  
		int areaCode = Integer.parseInt(subStr);  
		if( areaCode != HONGKONG_AREACODE && areaCode != TAIWAN_AREACODE  
				&& areaCode != MACAO_AREACODE   
				&& ( areaCode > MAX_MAINLAND_AREACODE || areaCode < MIN_MAINLAND_AREACODE) )  
			return "输入的身份证号码地域编码不符合大陆和港澳台规则";  
		return ACCEPT;  
	}  

	/** 
	 * 验证身份证号码数字字母组成是否符合规则 
	 */  
	private static String checkNumber( int idCardType ,String idCard ){  
		char[] chars = idCard.toCharArray();  
		if( idCardType == FIFTEEN_IDCARD ){  
			for( int i = 0; i<chars.length;i++){  
				if( chars[i] > '9' )  
					return idCardType+"位身份证号码中不能出现字母";  
			}  
		} else {  
			for( int i = 0; i < chars.length; i++ ) {  
				if( i < chars.length-1 ){  
					if( chars[i] > '9' )  
						return EIGHTEEN_IDCARD+"位身份证号码中前"+(EIGHTEEN_IDCARD-1)+"不能出现字母";  
				} else {  
					if( chars[i] > '9' && chars[i] != 'X')  
						return idCardType+"位身份证号码中最后一位只能是数字0~9或字母X";  
				}  
			}  

		}  

		return ACCEPT;  
	}  

	/** 
	 * 验证身份证号码出生日期是否符合规则 
	 */  
	private static String checkBirthDate(int idCardType, String idCardInput ){  
		String yearResult = checkBirthYear(idCardType,idCardInput);  
		if( !ACCEPT.equals(yearResult))  
			return yearResult;  

		String monthResult = checkBirthMonth(idCardType,idCardInput);  
		if( !ACCEPT.equals(monthResult))  
			return monthResult;  

		String dayResult = checkBirthDay(idCardType,idCardInput);  
		if( !ACCEPT.equals(dayResult))  
			return dayResult;  

		return ACCEPT;  
	}  

	/** 
	 * 验证身份证号码出生日期年份是否符合规则 
	 */  
	private static String checkBirthYear(int idCardType, String idCardInput){  
		if( idCardType == FIFTEEN_IDCARD){  
			int year = Integer.parseInt(idCardInput.substring(6, 8));  
			if( year < 0 || year > 99 )  
				return idCardType+"位的身份证号码年份须在00~99内";  
		} else {  
			int year = Integer.parseInt(idCardInput.substring(6, 10));  
			int yearNow = getYear();  
			if( year < 1921 || year > yearNow )  
				return idCardType + "位的身份证号码年份须在1921~" + yearNow + "内";
		}  
		return ACCEPT;  
	}  

	/** 
	 * 验证身份证号码出生日期月份是否符合规则 
	 */  
	private static String checkBirthMonth(int idCardType, String idCardInput){  
		int month = 0;  
		if( idCardType == FIFTEEN_IDCARD)  
			month = Integer.parseInt(idCardInput.substring(8, 10));   
		else   
			month = Integer.parseInt(idCardInput.substring(10, 12));  

		if( month < 1 || month > 12)  
			return "身份证号码月份须在01~12内";  

		return ACCEPT;  
	}  

	/** 
	 * 验证身份证号码出生日期天数是否符合规则 
	 */  
	private static String checkBirthDay(int idCardType, String idCardInput){  
		boolean bissextile = false;   
		int year,month,day;  
		if( idCardType == FIFTEEN_IDCARD){  
			year = Integer.parseInt("19"+idCardInput.substring(6, 8));  
			month = Integer.parseInt(idCardInput.substring(8, 10));   
			day = Integer.parseInt(idCardInput.substring(10, 12));  
		} else {  
			year = Integer.parseInt(idCardInput.substring(6, 10));  
			month = Integer.parseInt(idCardInput.substring(10, 12));  
			day = Integer.parseInt(idCardInput.substring(12, 14));  
		}  
		if( year%4 == 0 && year%100 != 0 || year%400 ==0 )    
			bissextile = true;  

		switch( month ){  
		case 1:  
		case 3:  
		case 5:  
		case 7:  
		case 8:  
		case 10:  
		case 12:  
			if( day < 1 || day > 31 )  
				return "身份证号码相应月份日期须在1~31之间";  
			break;  
		case 4:  
		case 6:  
		case 9:  
		case 11:  
			if( day < 1 || day > 30 )  
				return "身份证号码相应月份日期须在1~30之间";  
			break;  
		case 2:  
			if(bissextile){  
				if( day < 1 || day > 29 )  
					return "身份证号码相应月份日期须在1~29之间";  
			}else {  
				if( day < 1 || day > 28 )  
					return "身份证号码相应月份日期须在1~28之间";  
			}  
			break;  
		}  
		return ACCEPT;  
	}  

	//    /** 
	//     * 验证身份证号码顺序码是否符合规则,男性为偶数,女性为奇数 
	//     */  
	//    private static int checkSortCode(int idCardType ,int sex,String idCardInput){  
	//        int sortCode = 0;  
	//        if( idCardType == FIFTEEN_IDCARD ){  
	//            sortCode = Integer.parseInt(idCardInput.substring(12, 15));  
	//        } else {  
	//            sortCode = Integer.parseInt(idCardInput.substring(14, 17));  
	//        }    
	//        return sortCode;  
	//    }  

	/** 
	 * 验证18位身份证号码校验码是否符合规则 
	 */  
	private static String checkCheckCode( int idCardType , String idCard ){  
		if( idCardType == EIGHTEEN_IDCARD ){
			int sigma = 0;
			for (int i=0; i<17; i++) {
				int ai = Integer.parseInt(idCard.substring(i,i+1));
				int wi = a[i];
				sigma += ai * wi;
			}
			int number = sigma % 11;
			String check_number = SORTCODES[number];

			if(!check_number.equals(idCard.substring(17)))  {
				return "身份中的校验码不正确，身份证号码不合规";  
			}
		}  
		return ACCEPT;  
	}  

	/** 
	 * 返回当前年份 
	 */  
	private static int getYear(){  
		Date now = new Date();  
		SimpleDateFormat format = new SimpleDateFormat("yyyymmdd");   
		String nowStr = format.format(now);  
		return Integer.parseInt(nowStr.substring(0, 4));  
	}  
}  