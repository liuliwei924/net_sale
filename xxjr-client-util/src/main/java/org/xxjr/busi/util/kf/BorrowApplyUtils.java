package org.xxjr.busi.util.kf;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.service.RemoteInvoke;
import org.ddq.common.util.DateUtil;
import org.ddq.common.util.StringUtil;
import org.llw.model.cache.RedisUtils;
import org.springframework.util.StringUtils;
import org.xxjr.busi.util.BorrowConstant;
import org.xxjr.busi.util.StoreConstant;
import org.xxjr.cust.util.CustConstant;
import org.xxjr.cust.util.info.CustomerIdentify;
import org.xxjr.cust.util.info.CustomerUtil;
import org.xxjr.sys.util.NumberUtil;
import org.xxjr.sys.util.ServiceKey;
import org.xxjr.sys.util.SysParamsUtil;

/**
 * 贷款申请
 * @author liulw 2017-02-13
 *
 */
public class BorrowApplyUtils {
	public static final String allApplyCountKey = "key_all_apply_count";

	/**
	 * 是否能申请
	 * @param telephone
	 * @return 
	 */
	public static AppResult isCanApply(String telephone){
		AppResult result = new AppResult();
		AppParam params = new AppParam();
		params.addAttr("telephone", telephone);
		
		Map<String, Object> queryMap = queryApplyInfo(null, telephone);
		if(queryMap != null && !queryMap.isEmpty()){
			String applyId = StringUtil.getString(queryMap.get("applyId"));
			result.putAttr("applyId", applyId);
			if ((!isTestUser(telephone))) {
				//如果客服转黑名单，3个月后才能申请
				int applyType = Integer.parseInt(queryMap.get("applyType").toString());
				String applyTime = StringUtil.getString(queryMap.get("applyTime"));
				Date date = DateUtil.plus(DateUtil.toLocalDateTime(applyTime, 
						DateUtil.DATE_PATTERN_YYYY_MM_DD_HHMMSS), 3, DateUtil.ChronoUnit_MONTHS);
				
				if(BorrowConstant.apply_type_3 == applyType && date.getTime() > new Date().getTime()){
					result.setSuccess(false);
					result.setErrorCode("100");
					result.setMessage("抱歉，您之前已经有过贷款申请，无需再次申请,我们将为您推荐最适合您的贷款产品。");
					return result;
				}
				//0-待处理  1-客服锁定中 2-门店锁定中 3-可转化 4-转化中 5-转化成功 6-转化失败 7-门店退回 8-过期失效
				String status = StringUtil.getString(queryMap.get("status"));
				result.putAttr("status", status);
				if(!(BorrowConstant.apply_status_0.equals(status) 
						|| BorrowConstant.apply_status_6.equals(status)
						|| BorrowConstant.apply_status_8.equals(status))){
					result.setSuccess(false);
					result.setErrorCode("100");
					result.setMessage("抱歉，您之前已经有过贷款申请，无需再次申请,我们将为您推荐最适合您的贷款产品。");
				}
			}
		}
		return result;
	}
	
	/**
	 * 是否能抢优质单
	 * @param telephone
	 * @return
	 */
	public static AppResult isCanRob(String customerId){
		Map<String, Object> custMap = CustomerIdentify
				.getCustIdentify(customerId);
		String roleType = StringUtil.getString(custMap.get("roleType"));
		if (!CustConstant.CUST_ROLETYPE_3.equals(roleType)&&
				!CustConstant.CUST_ROLETYPE_1.equals(roleType)) {
			return CustomerUtil.retErrorMsg("抱歉，你没有抢优质单的资格");
		}
		
		AppResult result = new AppResult();
		AppParam params = new AppParam();
		params.addAttr("lastStore", customerId);
		params.addAttr("status", "2");
	//	params.addAttr("lockDate", DateUtil.toStringByParttern(new Date(), "yyyy-MM-dd"));
		params.addAttr("orderStatus", StoreConstant.STORE_ORDER_f1);//-1-未跟进 
		int count = queryCount(params);
		if(count > 0){
			int maxRobSeniorCount = SysParamsUtil.getIntParamByKey("maxRobSeniorCount", 5);
			
			if(count >= maxRobSeniorCount){
				result.setSuccess(false);
				result.setMessage("抱歉，您今天抢优质单数量已达上限，不能再抢");
			}
		}
		return result;
	}
	
	public static boolean isTestUser (String telephone) {
		String telephones = SysParamsUtil.getParamByKey("tgTestTelephone");
		if (!StringUtils.isEmpty(telephones)) {
			if (telephones.indexOf(telephone) > -1) {
				return true;
			}
		}
		return false;
	}
	
	public static int queryCount(AppParam params) {
		params.setService("borrowApplyService");
		params.setMethod("queryCount");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START
						+ ServiceKey.Key_busi_in));

		AppResult result = RemoteInvoke.getInstance().callNoTx(params);
		if(result.getAttr(DuoduoConstant.TOTAL_SIZE)==null) {
			return 0;
		}
		int count = (Integer) result.getAttr(DuoduoConstant.TOTAL_SIZE);

		return count;
	}
	
	public static int queryApplyCount(String telephone) {
		AppParam  params = new AppParam("borrowApplyService","query");
		params.addAttr("telephone", telephone);
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START
						+ ServiceKey.Key_busi_in));

		AppResult result = RemoteInvoke.getInstance().callNoTx(params);
		
		int applyCount = 0;
		if(result.getRows().size() > 0){
			Map<String, Object> applyMap = result.getRow(0);
			String applyCountStr = StringUtil.getString(applyMap.get("applyCount"));
			if(applyCountStr != ""){
				applyCount = Integer.parseInt(applyCountStr);
			}
		}
		return applyCount;
	}
	
	/**
	 * 是否可进入编辑界面
	 * @param applyId
	 * @return
	 */
	public static AppResult isCanEdit(String customerId, String applyId){
		AppResult result = new AppResult();
		
		Map<String,Object> custRight = KfUserUtil.getUserRight(customerId);
		int editBorrow = NumberUtil.getInt(custRight.get("editBorrow"),0);
		int isZSenior = NumberUtil.getInt(custRight.get("isZSenior"),0);
		int isSenior = NumberUtil.getInt(custRight.get("isSenior"),0);
		if(CustomerUtil.isAdmin(customerId)|| editBorrow == 0){//管理员，推广和录单的能查看
			return result;
		}
		AppParam queryParam = new AppParam();
		queryParam.addAttr("applyId", applyId);
		queryParam.setService("borrowApplyService");
		queryParam.setMethod("query");
		queryParam.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
		AppResult queryResult = RemoteInvoke.getInstance().call(queryParam);
		if(queryResult.getRows().size() == 0){
			result.setSuccess(false);
			result.setMessage("借款数据已不存在！");
			return result;
		}
		Map<String,Object> applyInfo = queryResult.getRow(0);
		int applyType = NumberUtil.getInt(applyInfo.get("applyType"));
		if(!StringUtils.isEmpty(applyInfo.get("lastKf")) 
				&& !customerId.equals(applyInfo.get("lastKf").toString())){
			result.setSuccess(false);
			result.setMessage("您不是当前借款处理人，不能编辑此借款信息");
			return result;
		}
		
		if(applyType == 1 && isSenior == 0){
			result.setSuccess(false);
			result.setMessage("很抱歉，你没有处理优质单的权限");
			return result;
		}
		
		if(applyType == 6 && isZSenior == 0){
			result.setSuccess(false);
			result.setMessage("很抱歉，你没有处理准优质单的权限");
			return result;
		}
		
		return result;
	}
	
	/**
	 * 查询借款列表
	 * @param params
	 * @return
	 */
	public static AppResult queryBorrowList(AppParam params){
		params.setService("borrowApplyService");
		params.setMethod("queryShowByPage");
		params.setOrderBy("applyTime");
		params.setOrderValue("desc");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START
						+ ServiceKey.Key_busi_in));
		return RemoteInvoke.getInstance().callNoTx(params);
	}
	
	
	/**
	 * 查询贷款基本信息
	 * @param applyId
	 * @return
	 */
	public static Map<String,Object> queryApplyInfo(Object applyId,String telephone){
		AppParam queryParam = new AppParam("borrowApplyService", "query");
		queryParam.addAttr("applyId", applyId);
		queryParam.addAttr("telephone", telephone);
		queryParam.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START
						+ ServiceKey.Key_busi_in));
		
		AppResult qeuryResult = RemoteInvoke.getInstance().call(queryParam);
		if(qeuryResult.getRows().size() > 0){
			return qeuryResult.getRow(0);
		}
		return null;
	}
	
	/**
	 * 查询贷款简单信息
	 * @param applyId
	 * @return
	 */
	public static Map<String,Object> querySimpleInfo(Object applyId){
		AppParam queryParam = new AppParam("borrowApplyService", "querySimpleInfo");
		queryParam.addAttr("applyId", applyId);
		queryParam.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START
						+ ServiceKey.Key_busi_in));
		
		AppResult qeuryResult = RemoteInvoke.getInstance().call(queryParam);
		if(qeuryResult.getRows().size() > 0){
			return qeuryResult.getRow(0);
		}
		return new HashMap<String, Object>();
	}
	
	/**
	 * 查询贷款基本信息
	 * @param applyId
	 * @return
	 */
	public static Map<String,Object> queryBaseInfo(Object applyId){
		AppParam queryParam = new AppParam("borrowApplyService", "queryBaseInfo");
		queryParam.addAttr("applyId", applyId);
		queryParam.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START
						+ ServiceKey.Key_busi_in));
		
		AppResult qeuryResult = RemoteInvoke.getInstance().call(queryParam);
		if(qeuryResult.getRows().size() > 0){
			return qeuryResult.getRow(0);
		}
		return new HashMap<String, Object>();
	}
	
	/**
	 * 基本信息(将数字-中文)
	 * @param applyId
	 * @return
	 */
	public static Map<String,Object> queryBaseText(Object applyId){
		AppParam queryParam = new AppParam("borrowBaseService", "queryText");
		queryParam.addAttr("applyId", applyId);
		queryParam.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START
						+ ServiceKey.Key_busi_in));
		
		AppResult qeuryResult = RemoteInvoke.getInstance().call(queryParam);
		if(qeuryResult.getRows().size() > 0){
			return qeuryResult.getRow(0);
		}
		return new HashMap<String, Object>();
	}
	
	/**
	 * 查询贷款简单信息
	 * @param applyId
	 * @return
	 */
	public static Map<String,Object> queryDetail(Object applyId){
		AppParam queryParam = new AppParam("borrowBaseService", "queryDetail");
		queryParam.addAttr("applyId", applyId);
		queryParam.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START
						+ ServiceKey.Key_busi_in));
		
		AppResult qeuryResult = RemoteInvoke.getInstance().call(queryParam);
		if(qeuryResult.getRows().size() > 0){
			return qeuryResult.getRow(0);
		}
		return new HashMap<String, Object>();
	}
	
	/**
	 * 查询贷款收入信息
	 * @param applyId
	 * @return
	 */
	public static Map<String,Object> queryIncomeInfo(Object applyId){
		AppParam queryParam = new AppParam("borrowIncomeService", "query");
		queryParam.addAttr("applyId", applyId);
		queryParam.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START
						+ ServiceKey.Key_busi_in));
		AppResult qeuryResult = RemoteInvoke.getInstance().call(queryParam);
		if(qeuryResult.getRows().size() > 0){
			return qeuryResult.getRow(0);
		}
		return new HashMap<String, Object>();
	}
	
	/**
	 * 查询贷款收入信息(将数字-中文)
	 * @param applyId
	 * @return
	 */
	public static Map<String,Object> queryIncomeText(Object applyId){
		AppParam queryParam = new AppParam("borrowIncomeService", "queryText");
		queryParam.addAttr("applyId", applyId);
		queryParam.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START
						+ ServiceKey.Key_busi_in));
		AppResult qeuryResult = RemoteInvoke.getInstance().call(queryParam);
		if(qeuryResult.getRows().size() > 0){
			return qeuryResult.getRow(0);
		}
		return new HashMap<String, Object>();
	}
	
	
	/**
	 * 查询贷款用户保险信息
	 * @param applyId
	 * @return
	 */
	public static Map<String,Object> queryInsureInfo(Object applyId){
		AppParam queryParam = new AppParam("borrowInsureService", "query");
		queryParam.addAttr("applyId", applyId);
		queryParam.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START
						+ ServiceKey.Key_busi_in));
		AppResult qeuryResult = RemoteInvoke.getInstance().call(queryParam);
		if(qeuryResult.getRows().size() > 0){
			return qeuryResult.getRow(0);
		}
		return new HashMap<String, Object>();
	}
	
	/**
	 * 查询贷款用户房产信息
	 * @param applyId
	 * @return
	 */
	public static Map<String,Object> queryHouseInfo(Object applyId){
		AppParam queryParam = new AppParam("borrowHouseService", "query");
		queryParam.addAttr("applyId", applyId);
		queryParam.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START
						+ ServiceKey.Key_busi_in));
		AppResult qeuryResult = RemoteInvoke.getInstance().call(queryParam);
		if(qeuryResult.getRows().size() > 0){
			return qeuryResult.getRow(0);
		}
		return new HashMap<String, Object>();
	}
	
	/**
	 * 查询贷款用户房产信息(将数字-中文)
	 * @param applyId
	 * @return
	 */
	public static Map<String,Object> queryHouseText(Object applyId){
		AppParam queryParam = new AppParam("borrowHouseService", "queryText");
		queryParam.addAttr("applyId", applyId);
		queryParam.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START
						+ ServiceKey.Key_busi_in));
		AppResult qeuryResult = RemoteInvoke.getInstance().call(queryParam);
		if(qeuryResult.getRows().size() > 0){
			return qeuryResult.getRow(0);
		}
		return new HashMap<String, Object>();
	}
	
	/**
	 * 查询贷款用户车产信息
	 * @param applyId
	 * @return
	 */
	public static Map<String,Object> queryCarInfo(Object applyId){
		AppParam queryParam = new AppParam("borrowCarService", "query");
		queryParam.addAttr("applyId", applyId);
		queryParam.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START
						+ ServiceKey.Key_busi_in));
		AppResult qeuryResult = RemoteInvoke.getInstance().call(queryParam);
		if(qeuryResult.getRows().size() > 0){
			return qeuryResult.getRow(0);
		}
		return new HashMap<String, Object>();
	}
	
	/**
	 * 查询贷款用户车产信息(将数字-中文)
	 * @param applyId
	 * @return
	 */
	public static Map<String,Object> queryCarText(Object applyId){
		AppParam queryParam = new AppParam("borrowCarService", "queryText");
		queryParam.addAttr("applyId", applyId);
		queryParam.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START
						+ ServiceKey.Key_busi_in));
		AppResult qeuryResult = RemoteInvoke.getInstance().call(queryParam);
		if(qeuryResult.getRows().size() > 0){
			return qeuryResult.getRow(0);
		}
		return new HashMap<String, Object>();
	}
	
	/**
	 * 查询门店预约记录
	 * @param applyId
	 * @return
	 */
	public static Map<String,Object> queryBookInfo(Object applyId){
		AppParam queryParam = new AppParam("treatBookService", "queryBookInfo");
		queryParam.addAttr("applyId", applyId);
		queryParam.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START
						+ ServiceKey.Key_busi_in));
		AppResult qeuryResult = RemoteInvoke.getInstance().call(queryParam);
		if(qeuryResult.getRows().size() > 0){
			return qeuryResult.getRow(0);
		}
		return new HashMap<String, Object>();
	}
	
	/**
	 * 查询门店预约记录
	 * @param applyId
	 * @return
	 */
	public static Map<String,Object> queryBook(Object applyId,String customerId){
		AppParam queryParam = new AppParam("treatBookService", "query");
		queryParam.addAttr("applyId", applyId);
		queryParam.addAttr("customerId", customerId);
		queryParam.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START
						+ ServiceKey.Key_busi_in));
		AppResult qeuryResult = RemoteInvoke.getInstance().call(queryParam);
		if(qeuryResult.getRows().size() > 0){
			return qeuryResult.getRow(0);
		}
		return new HashMap<String, Object>();
	}
	
	/**
	 * 查询门店签单合同
	 * @param applyId
	 * @return
	 */
	public static Map<String,Object> queryTreatInfo(Object applyId){
		AppParam queryParam = new AppParam("treatInfoService", "queryTreatInfo");
		queryParam.addAttr("applyId", applyId);
		queryParam.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START
						+ ServiceKey.Key_busi_in));
		AppResult qeuryResult = RemoteInvoke.getInstance().call(queryParam);
		if(qeuryResult.getRows().size() > 0){
			return qeuryResult.getRow(0);
		}
		return new HashMap<String, Object>();
	}
	
	/**
	 * 查询门店签单合同
	 * @param applyId
	 * @return
	 */
	public static Map<String,Object> queryTreat(Object applyId,String customerId){
		AppParam queryParam = new AppParam("treatInfoService", "query");
		queryParam.addAttr("applyId", applyId);
		queryParam.addAttr("customerId", customerId);
		queryParam.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START
						+ ServiceKey.Key_busi_in));
		AppResult qeuryResult = RemoteInvoke.getInstance().call(queryParam);
		if(qeuryResult.getRows().size() > 0){
			return qeuryResult.getRow(0);
		}
		return new HashMap<String, Object>();
	}
	
	/**
	 * 查询门店放款成功信息
	 * @param applyId
	 * @return
	 */
	public static List<Map<String,Object>>  queryTreatSuccessList(Object applyId){
		AppParam queryParam = new AppParam("treatSuccessService", "querySuccessInfo");
		queryParam.addAttr("applyId", applyId);
		queryParam.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START
						+ ServiceKey.Key_busi_in));
		queryParam.setOrderBy("createTime");
		queryParam.setOrderValue("DESC");
		AppResult qeuryResult = RemoteInvoke.getInstance().call(queryParam);
		return qeuryResult.getRows();
	}
	
	/**
	 * 查询门店放款成功信息
	 * @param applyId
	 * @return
	 */
	public static Map<String,Object> queryTreatSuccess(Object recordId,Object applyId){
		AppParam queryParam = new AppParam("treatSuccessService", "query");
		queryParam.addAttr("recordId", recordId);
		queryParam.addAttr("applyId", applyId);
		queryParam.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START
						+ ServiceKey.Key_busi_in));
		AppResult qeuryResult = RemoteInvoke.getInstance().call(queryParam);
		if(qeuryResult.getRows().size() > 0){
			return qeuryResult.getRow(0);
		}
		return new HashMap<String, Object>();
	}
	
	/**
	 * 查询门店结算信息
	 * @param applyId
	 * @return
	 */
	public static Map<String,Object> querySettlementInfo(Object applyId){
		AppParam queryParam = new AppParam("treatSettlementService", "querySettlementInfo");
		queryParam.addAttr("applyId", applyId);
		queryParam.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START
						+ ServiceKey.Key_busi_in));
		AppResult qeuryResult = RemoteInvoke.getInstance().call(queryParam);
		if(qeuryResult.getRows().size() > 0){
			return qeuryResult.getRow(0);
		}
		return new HashMap<String, Object>();
	}
	
	/**
	 * 查询贷款客服跟进记录
	 * @param applyId
	 * @return
	 */
	public static List<Map<String,Object>> queryKefuHandleList(Object applyId){
		AppParam queryParam = new AppParam("borrowKfRecordService", "queryShowByPage");
		queryParam.addAttr("applyId", applyId);
		queryParam.setCurrentPage(1);
		queryParam.setEveryPage(5);
		queryParam.setOrderBy("createTime");
		queryParam.setOrderValue("desc");
		queryParam.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START
						+ ServiceKey.Key_busi_in));
		return RemoteInvoke.getInstance().call(queryParam).getRows();
	}
	
	/**
	 * 查询贷款门店跟进记录
	 * @param applyId
	 * @return
	 */
	public static List<Map<String,Object>> queryStoreHandleList(Object applyId, int limit){
		AppParam queryParam = new AppParam("borrowStoreRecordService", "queryShowByPage");
		queryParam.addAttr("applyId", applyId);
		queryParam.setCurrentPage(1);
		queryParam.setEveryPage(limit);
		queryParam.setOrderBy("createTime");
		queryParam.setOrderValue("desc");
		queryParam.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START
						+ ServiceKey.Key_busi_in));
		return RemoteInvoke.getInstance().call(queryParam).getRows();
	}
	
	/**
	 * 查询优质单门店处理记录
	 * @param applyId
	 * @return
	 */
	public static List<Map<String,Object>> querySeniorHandleList(Object applyId, Object customerId, int limit){
		AppParam queryParam = new AppParam("borrowStoreRecordService", "queryShowByPage");
		queryParam.addAttr("applyId", applyId);
		queryParam.addAttr("storeBy", customerId);
		queryParam.setCurrentPage(1);
		queryParam.setEveryPage(limit);
		queryParam.setOrderBy("createTime");
		queryParam.setOrderValue("desc");
		queryParam.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START
						+ ServiceKey.Key_busi_in));
		return RemoteInvoke.getInstance().call(queryParam).getRows();
	}
	
	/**
	 * 查询客服跟进记录
	 * @param applyId
	 * @return
	 */
	public static List<Map<String,Object>> queryKfRecords(Object applyId, int limit){
		AppParam kfParams = new AppParam("borrowKfRecordService", "queryShowByPage");
		kfParams.addAttr("applyId", applyId);
		kfParams.setCurrentPage(1);
		kfParams.setEveryPage(limit);
		kfParams.setOrderBy("createTime");
		kfParams.setOrderValue("DESC");
		kfParams.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START
						+ ServiceKey.Key_busi_in));
		return RemoteInvoke.getInstance().call(kfParams).getRows();
	}
	
	/**
	 * 更改转换状态
	 * @param applyId 申请Id
	 * @param applyStatus  3-可转化 4-转化中 5-转化成功 6-转化失败
	 * @param handleType 1，下发处理 2，成功付款 3，不成功 4，退款处理
	 * @param robType 1-免费抢 2-积分抢 3-现金抢
	 * @return
	 */
	public static AppResult changeTranStatus(Object applyId, String applyStatus,int handleType,int robType){
		AppParam updateParam = new AppParam("borrowApplyService", "update");
		updateParam.addAttr("applyId", applyId);
		updateParam.addAttr("status", applyStatus+"");
		updateParam.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START
						+ ServiceKey.Key_busi_in));
		AppResult result = RemoteInvoke.getInstance().call(updateParam);
		
		if(result.isSuccess()){
			updateParam = new AppParam("borrowSelRecordService", "update");
			updateParam.addAttr("applyId", applyId);
			updateParam.addAttr("handleType", handleType);
			updateParam.addAttr("robType", robType);
			if(robType == 1){
				updateParam.addAttr("costScore", 0);
				updateParam.addAttr("costPrice", 0);
			}else if(robType == 2){
				updateParam.addAttr("costPrice", 0);
			}else if(robType == 3){
				updateParam.addAttr("costPrice", 0);
			}
			updateParam.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().call(updateParam);
		}
		
		return result;
	}
	
	/**
	 * 获取签单列表
	 * @param params
	 * @return
	 */
	public static AppResult getTreatInfoList(AppParam params){
		params.setService("treatInfoService");
		params.setMethod("queryByPage");
		if(StringUtils.isEmpty(params.getOrderBy())){
			params.setOrderBy("status,createTime");
			params.setOrderValue("ASC,DESC");
		}
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START
						+ ServiceKey.Key_busi_in));
		
		return RemoteInvoke.getInstance().call(params);
		
	}
	
	/**
	 * 录单列表
	 * @param applyId
	 * @param isDetail 是否查询简单信息
	 * @return
	 */
	public static List<Map<String, Object>> getTreatSuccessList(String applyId, String customerId, String inStatus, boolean isDetail){
		AppParam params = new AppParam();
		params.setService("treatSuccessService");
		params.addAttr("applyId", applyId);
		params.addAttr("customerId", customerId);
		params.addAttr("inStatus", inStatus);
		params.setMethod(isDetail? "query" : "querySimpleInfo");
		
		if(StringUtils.isEmpty(params.getOrderBy())){
			params.setOrderBy("updateTime");
			params.setOrderValue("DESC");
		}
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START
						+ ServiceKey.Key_busi_in));
		return RemoteInvoke.getInstance().call(params).getRows();
		
	}
	
	/**
	 * 查询专属订单
	 * @param applyId
	 * @return
	 */
	public static int queryExecOrder(Object applyId){
		AppParam queryParam = new AppParam("exclusiveOrderService", "queryCount");
		queryParam.addAttr("applyId", applyId);
		queryParam.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START
						+ ServiceKey.Key_busi_in));
		AppResult quryResult = RemoteInvoke.getInstance().callNoTx(queryParam);
		int count = NumberUtil.getInt(quryResult.getAttr(DuoduoConstant.TOTAL_SIZE),0);
		return count;
	}
	
	/**
	 * 获取申请总数
	 * @return
	 */
	public static int getAllApplyCount () {
		Integer count = (Integer) RedisUtils.getRedisService().get(allApplyCountKey);
		if (count == null || count == 0) {
			count = 1011999;
			RedisUtils.getRedisService().set(allApplyCountKey, count);
		}
		AppParam queryCount = new AppParam();
		queryCount.addAttr("applyTime", DateUtil.toStringByParttern(new Date(), DateUtil.DATE_PATTERN_YYYY_MM_DD));
		int nowCount = BorrowApplyUtils.queryCount(queryCount);
		count = count + nowCount;
		return count;
	}
	
	public static void refreshApplyCount () {
		AppParam queryParam = new AppParam();
		String timeStr = DateUtil.toStringByParttern(new Date(), DateUtil.DATE_PATTERN_YYYY_MM_DD);
		queryParam.addAttr("queryCountTime", timeStr);
		int queryCount = queryCount(queryParam);
		RedisUtils.getRedisService().set(allApplyCountKey, queryCount);
	}
}
