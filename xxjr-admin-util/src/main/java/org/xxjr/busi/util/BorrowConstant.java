package org.xxjr.busi.util;


public class BorrowConstant {

	public static final String channel_WX = "wx" ;
	
	public static final String channel_WXReferer = "WXReferer" ;
	
	public static final String channel_jrtt1 = "jrtt1" ;
	
	public static final String channel_jrtt2 = "jrtt2" ;
	
	public static final String channel_shenma = "shenma" ;
	
	public static final String channel_uc = "uc" ;
	
	/**1无固定职业**/
	public static final String work_type_1 = "1";
	/** 2企业主 **/
	public static final String work_type_2 = "2";
	/** 3个体户 **/
	public static final String work_type_3 = "3";
	/** 4上班族 **/
	public static final String work_type_4 = "4";
	/** 5学生  **/
	public static final String work_type_5 = "5";
	
	/** 0-待处理 **/
	public static final String apply_status_0 = "0";
	/** 1-客服锁定中 **/
	public static final String apply_status_1 = "1";
	/** 2-门店锁定中 **/
	public static final String apply_status_2 = "2";
	/** 3-可转化 **/
	public static final String apply_status_3 = "3";
	/** 4-转化中 **/
	public static final String apply_status_4 = "4";
	/** 5-转化成功 **/
	public static final String apply_status_5 = "5";
	/** 6-转化失败 **/
	public static final String apply_status_6 = "6";
	
	/** 7-门店退回 **/
	public static final String apply_status_7 = "7";
	/** 8-过期失效 **/
	public static final String apply_status_8 = "8";
	/** 9-待自动分配门店 **/
	public static final String apply_status_9 = "9";
	/** 10-二次单待分配 **/
	public static final String apply_status_10 = "10";
	/** 11-转其它平台  **/
	public static final String apply_status_11 = "11";
	
	/** 贷款类型 优质单  **/
	public final static int apply_type_1 = 1;
	/** 贷款类型 普通单  **/
	public final static int apply_type_2 = 2;
	/** 贷款类型 垃圾单   **/
	public final static int apply_type_3 = 3;
	/** 贷款类型 不押车贷  **/
	public final static int apply_type_4 = 4;
	/** 贷款类型 信贷员录单 **/
	public final static int apply_type_5 = 5;
	
	/** 贷款类型 转准优质单  **/
	public final static int apply_type_6 = 6;
	/** 贷款类型 转优质单客户  **/
	public final static int apply_type_7 = 7;
	
	/** 贷款 接单状态 --成功  **/
	public final static String receiveStatus_2 = "2";
	/** 贷款 接单状态 --申请退款  **/
	public final static String receiveStatus_5 = "5";
	
	/** 客服操作类型 跟单  **/
	public final static String KEFU_OPER_1 = "1";
	/** 客服操作类型 转门店  **/
	public final static String KEFU_OPER_2 = "2";
	/** 客服操作类型 转客服  **/
	public final static String KEFU_OPER_3 = "3";
	/** 客服操作类型 转普通单  **/
	public final static String KEFU_OPER_4 = "4";
	/** 客服操作类型 转垃圾单  **/
	public final static String KEFU_OPER_5 = "5";
	/** 客服操作类型 修改信息  **/
	public final static String KEFU_OPER_6 = "6";
	/** 客服操作类型 继续跟进  **/
	public final static String KEFU_OPER_7 = "7";
	/** 客服操作类型 转其他小贷  **/
	public final static String KEFU_OPER_8 = "8";
	
	/** 客服操作类型 去挂卖  **/
	public final static String KEFU_OPER_9 = "9";
	
	/** 客服操作类型 未填信息修改  **/
	public final static String KEFU_OPER_10 = "10";
	
	/** 门店退款操作  **/
	public final static String KEFU_OPER_11 = "11";
	
	/** 客服操作类型 转优质客户  **/ 
	public final static String KEFU_OPER_12 = "12";
	
	/** 门店操作类型 待处理  **/
	public final static String STORE_OPER_f1 = "-1";
	
	/** 门店操作类型 接单  **/
	public final static String STORE_OPER_0 = "0";
	/** 门店操作类型 继续跟进  **/
	public final static String STORE_OPER_1 = "1";
	/** 门店操作类型   客户预约**/
	public final static String STORE_OPER_2 = "2";
	
	/** 门店操作类型 签单  **/
	public final static String STORE_OPER_3 = "3";
	/** 门店操作类型 放款  **/
	public final static String STORE_OPER_4 = "4";
	/** 门店操作类型 不能做(退单) 失败  **/
	public final static String STORE_OPER_5 = "5";
	/** 门店操作类型 办理完成  **/
	public final static String STORE_OPER_6 = "6";
	
	/** 门店退款成功  **/
	public final static String STORE_OPER_7 = "7";
	
	/** 门店退款失败  **/
	public final static String STORE_OPER_8 = "8";
	
	/** 门店操作类型   已上门**/
	public final static String STORE_OPER_9 = "9";
	
	/** 门店操作类型   预约未上门的**/
	public final static String STORE_OPER_10 = "10";
	
	/** 门店操作类型   签约失败**/
	public final static String STORE_OPER_11 = "11";
	
	/** 门店操作类型  门店回款成功**/
	public final static String STORE_OPER_12 = "12";
	
	/** 门店操作类型  门店回款失败**/
	public final static String STORE_OPER_13 = "13";
	
	/** 门店操作类型  门店处理不需要**/
	public final static String STORE_OPER_14 = "14";
	
	/** 门店操作类型 设置专属单**/
	public final static String STORE_OPER_15 = "15";
	
	/** 门店操作类型  取消专属单**/
	public final static String STORE_OPER_16 = "16";
	
	/** 门店预约状态  预约中**/
	public final static String STORE_BOOK_1 = "1";
	
	/** 门店预约状态 未上门**/
	public final static String STORE_BOOK_2 = "2";
	
	/** 门店预约状态  已上门**/
	public final static String STORE_BOOK_3 = "3";
	
	
	
	/** 抢单方式，免费抢单  **/
	public static final String RobWay_1 = "1";
	/** 抢单方式，积分抢单 **/
	public static final String RobWay_2 = "2";
	/** 抢单方式，现金抢单 **/
	public static final String RobWay_3 = "3";
	/** 抢单方式，高级抢单**/
	public static final String RobWay_4 = "4";
	/** 抢单方式，余额抢单**/
	public static final String RobWay_5 = "5";
	/** 抢单方式，自动抢单**/
	public static final String RobWay_6 = "6";
	/** 抢单方式，会员特价抢单**/
	public static final String RobWay_7 = "7";
	/** 抢单方式，特价抢单**/
	public static final int RobType_7 = 7;
	
	
	
	/** 比较参数，待处理数  **/
	public static final int COMPWAY_1 = 1;
	/** 比较参数，接单数  **/
	public static final int COMPWAY_2 = 2;
	/** 比较参数，反馈次数  **/
	public static final int COMPWAY_3 = 3;
	/** 比较参数，上门数  **/
	public static final int COMPWAY_4 = 4;
	/** 比较参数，总签单数  **/
	public static final int COMPWAY_5 = 5;
	/** 比较参数，总签单金额 **/
	public static final int COMPWAY_6 = 6;
	/** 比较参数，在账签单数  **/
	public static final int COMPWAY_7 = 7;
	/** 比较参数，在账签单金额   **/
	public static final int COMPWAY_8 = 8;
	/** 比较参数，成功签单数  **/
	public static final int COMPWAY_9 = 9;
	/** 比较参数，成功签单金额  **/
	public static final int COMPWAY_10 = 10;
	/** 比较参数，黄单数量  **/
	public static final int COMPWAY_11 = 11;
	/** 比较参数，黄单金额(万) **/
	public static final int COMPWAY_12 = 12;
	/** 比较参数，成功回款数 **/
	public static final int COMPWAY_13 = 13;
	/** 比较参数，成功回款金额 **/
	public static final int COMPWAY_14 = 14;
	/** 比较参数，未核算回款数 **/
	public static final int COMPWAY_15 = 15;
	/** 比较参数，未核算回款金额 **/
	public static final int COMPWAY_16 = 16;
	
	/** 落地页申请 **/
	public static final int APPLY_KIND_1 = 1;
	
	/** 大额申请 **/
	public static final int APPLY_KIND_2 = 2;
	
	/** 微店产品申请 **/
	public static final int APPLY_KIND_3 = 3;
	


	
}
