package org.xxjr.busi.util;

/**
 * 融资吧错误代码
 * @author Administrator
 *
 */
public class BorrowErrorCode {

	/** 抢单方式，可免费抢单  **/
	public static final int RobType_1 = 1;
	/** 抢单方式，可积分抢单 **/
	public static final int RobType_2 = 2;
	/** 抢单方式，只能购买**/
	public static final int RobType_3 = 3;
	/** 抢单方式，高级抢单**/
	public static final int RobType_4 = 4;
	/** 抢单方式，余额抢单**/
	public static final int RobType_5 = 5;
	/** 抢单方式 ，自动抢单**/
	public static final int RobType_6 = 6;
	/** 抢单方式，特价抢单**/
	public static final int RobType_7 = 7;
	
	/** 借款申请来源 1 本平台申请  **/
	public static final int BorrowSource_1 = 1;
	/** 借款申请来源2 卡牛申请**/
	public static final int BorrowSource_2 = 2;
	/** 借款申请来源3 第一贷款 **/
	public static final int BorrowSource_3 = 3;
	
	/** 借款申请来源 4  第一贷款申请  **/
	public static final int BorrowSource_4 = 4;
	/** 借款申请来源 5  客服转xxjr  **/
	public static final int BorrowSource_5 = 5;
	/** 借款申请来源   推广申请  **/
	public static final int BorrowSource_6 = 6;
	

	/** 积分不足  **/
	public static final String SCORE_INSUFFICIENT = "积分不足"; 
	
	/** 已没有免费加急机会  **/
	public static final String HAVE_NO_FREE_CHANCE = "您的免费机会已用完，请使用积分加急"; 
	
	/** 已申请过该甩单  **/
	public static final String EXCHANGE_HAVE_APPLYED = "您已申请过该单，请等待申请结果";
	
	/** 甩单申请人数已达上限  **/
	public static final String EXCHANGE_APPLY_LIMIT = "申请人数已达上限";
	
	/** 直借单已被其他人处理  **/
	public static final String BORROW_HAD_ROBED = "手慢了，该单已被其他人处理";
	
	/** 免费抢单机会已达上限  **/
	public static final String FREE_ROB_CHANCE_LIMIT = "今天免费抢单数量已抢光";
	
	/** 甩单已被其他人处理  **/
	public static final String EXCHANGE_HAD_ROBED = "该甩单已被其他人申请";

	/** 个人信息不完善  **/
	public static final String MISS_PERSON_INFO = "请先完善个人信息";
}
