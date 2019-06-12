package org.xxjr.busiIn.store.treat;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.service.RemoteInvoke;
import org.ddq.common.core.service.SoaManager;
import org.ddq.common.exception.AppException;
import org.ddq.common.exception.DuoduoError;
import org.ddq.common.exception.SysException;
import org.ddq.common.util.DateUtil;
import org.ddq.common.util.StringUtil;
import org.llw.common.core.service.BaseService;
import org.llw.model.cache.RedisUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.xxjr.busi.util.BorrowConstant;
import org.xxjr.busiIn.utils.StoreOptUtil;
import org.xxjr.store.util.StoreApplyUtils;
import org.xxjr.sys.util.NumberUtil;
import org.xxjr.sys.util.ServiceKey;

import lombok.extern.slf4j.Slf4j;

@Lazy
@Service
@Slf4j
public class TreatSuccessService extends BaseService {
	private static final String NAMESPACE = "TREATSUCCESS";

	/**
	 * querys
	 * @param params
	 * @return
	 */
	public AppResult query(AppParam params) {
		return super.query(params, NAMESPACE);
	}
	
	/**
	 * 查询放款成功信息
	 * @param params
	 * @return
	 */
	public AppResult querySuccessInfo(AppParam params) {
		return super.query(params, NAMESPACE, "querySuccessInfo");
	}
	
	/**
	 * 查询简单的录单列表
	 * @param params
	 * @return
	 */
	public AppResult querySimpleInfo(AppParam params) {
		return super.query(params, NAMESPACE, "querySimpleInfo");
	}
	
	
	/**
	 * queryByPage
	 * @param params
	 * @return
	 */
	public AppResult queryByPage(AppParam params) {
		return super.queryByPage(params, NAMESPACE);
	}
	
	/**
	 * queryShowByPage
	 * @param params
	 * @return
	 */
	public AppResult queryShowByPage(AppParam params) {
		return super.queryByPage(params, NAMESPACE, "queryShow", "queryShowCount");
	}
	
	/**
	 * queryShowCount
	 * @param params
	 * @return
	 */
	public AppResult queryShowCount(AppParam params) {
		int size = getDao().count(NAMESPACE, "queryShowCount",params.getAttr(),params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
		return result;
	}
	/**
	 * queryCount
	 * @param params
	 * @return
	 */
	public AppResult queryCount(AppParam params) {
		int size = getDao().count(NAMESPACE, super.COUNT,params.getAttr(),params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
		return result;
	}
	
	
	/**
	 * insert
	 * @param params
	 * @return
	 */
	public AppResult insert(AppParam params) {
		AppResult result = new AppResult();
		params.addAttr("createTime", new Date());
		params.addAttr("updateTime",  new Date());
		result = super.insert(params, NAMESPACE);
		if(result.isSuccess()){
			result.putAttr("recordId", params.getAttr("recordId"));
			//删除缓存
			RedisUtils.getRedisService().del(StoreApplyUtils.STORE_APPLY_RET_RECORD + params.getAttr("applyId"));
		}
		return result;
	}	
	/**
	 * update
	 * @param params
	 * @return
	 */
	public AppResult update(AppParam params) {
		AppResult result = new AppResult();
		params.addAttr("updateTime",  new Date());
		result = super.update(params, NAMESPACE);
		//删除缓存
		RedisUtils.getRedisService().del(StoreApplyUtils.STORE_APPLY_RET_RECORD + params.getAttr("applyId"));
		return result;
	}
	
	/**
	 * update
	 * @param params
	 * @return
	 */
	public AppResult updateBackLoan(AppParam params) {
		params.addAttr("updateTime",  new Date());
		int size = getDao().update(NAMESPACE, "updateBackLoan",params.getAttr(),params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.DAO_Update_SIZE, size);
		//删除缓存
		RedisUtils.getRedisService().del(StoreApplyUtils.STORE_APPLY_RET_RECORD + params.getAttr("applyId"));
		return result;
	}
	
	/**
	 * updateBackLoanCust
	 * @param params
	 * @return
	 */
	public AppResult updateBackLoanCust(AppParam params) {
		params.addAttr("updateTime",  new Date());
		int size = getDao().update(NAMESPACE, "updateBackLoanCust",params.getAttr(),params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.DAO_Update_SIZE, size);
		//删除缓存
		RedisUtils.getRedisService().del(StoreApplyUtils.STORE_APPLY_RET_RECORD + params.getAttr("applyId"));
		return result;
	}
	
	/**
	 * deleteBack
	 * @param params
	 * @return
	 */
	public AppResult deleteBack(AppParam params) {
		int size = getDao().delete(NAMESPACE, "deleteBack",params.getAttr(),params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.DAO_Delete_SIZE, size);
		//删除缓存
		RedisUtils.getRedisService().del(StoreApplyUtils.STORE_APPLY_RET_RECORD + params.getAttr("applyId"));
		return result;
	}
	
	/**
	 * 保存回款数据
	 * @param params
	 * @return
	 * @throws ParseException 
	 */
	@SuppressWarnings("unchecked")
	public AppResult saveData(AppParam params) throws ParseException {
		String applyId = StringUtil.getString(params.getAttr("applyId"));
		String customerId = StringUtil.getString(params.getAttr("customerId"));
		String reContractId = StringUtil.getString(params.getAttr("reContractId"));
		String orgId = StringUtil.getString(params.getAttr("orgId"));
		AppResult result = new AppResult();
		if(!StringUtils.isEmpty(applyId)){
			AppParam queryHostory = new AppParam();
			queryHostory.addAttr("applyId", applyId);
			AppResult historyResult = StoreOptUtil.querySignHostory(queryHostory);
			String delreContractId = StringUtil.getString(historyResult.getAttr("reContractId"));
			AppParam deleteParams = new AppParam();
			if(!StringUtils.isEmpty(delreContractId)){
				deleteParams.addAttr("reContractId", delreContractId);
			}
			deleteParams.addAttr("applyId", applyId);
			deleteParams.addAttr("backType",2);
			AppResult appResult = this.deleteBack(deleteParams);
			//如果有两笔签单且合同编号不一样，如果第一笔删除为空则再删除第二笔
			int deleteSize = NumberUtil.getInt(appResult.getAttr(DuoduoConstant.DAO_Delete_SIZE),0);
			if(deleteSize == 0 && !StringUtils.isEmpty(deleteParams.getAttr("reContractId"))){
				deleteParams.addAttr("reContractId", reContractId);
				appResult = this.deleteBack(deleteParams);
			}
			if(appResult.isSuccess()){
				List<Map<String, Object>> recordList = (List<Map<String, Object>>) params
						.getAttr("recordList");
				List<String> newBookIds = new ArrayList<String>();
				for(Map<String,Object> map : recordList){
					String payTime = null;
					if(!StringUtils.isEmpty(map.get("PayTime"))){
						DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
						Date date = sdf.parse(StringUtil.getString(map.get("PayTime")));
						SimpleDateFormat simpFormat = new SimpleDateFormat ("EEE MMM dd HH:mm:ss Z yyyy", Locale.UK);
				        Date payDate =  simpFormat.parse(date.toString());
				        payTime = DateUtil.toStringByParttern(payDate, DateUtil.DATE_PATTERN_YYYY_MM_DD);
				        
					}
					AppParam updateParams  = new AppParam();
					String loanNo = StringUtil.getString(map.get("LoanNo"));
					String loanOrg = StoreApplyUtils.getLoanOrg(applyId, loanNo);
					if(!StringUtils.isEmpty(loanOrg)){
						updateParams.addAttr("loanOrg",loanOrg);
					}
					updateParams.addAttr("applyId",applyId);
					updateParams.addAttr("customerId",customerId);
					updateParams.addAttr("reContractId",reContractId);
					updateParams.addAttr("loanNo",loanNo);
					updateParams.addAttr("orgId", orgId);
					updateParams.addAttr("payType",map.get("PayType"));
					updateParams.addAttr("backType",2); //回款方式1-手动添加 2-查CFS回款
					updateParams.addAttr("queryStatus",2); //查询状态 1-未查询 2-查询成功 3-查询失败
					String recType = StringUtil.getString(map.get("RecType"));
					updateParams.addAttr("recType",recType);
					double feeAmount = NumberUtil.getDouble(map.get("RecMoney"),0.00);
					// 7是客户退款 10 是退定金
					if(("7".equals(recType) || "10".equals(recType) 
							|| "17".equals(recType) || "18".equals(recType)) && feeAmount > 0){
						updateParams.addAttr("feeAmount",-feeAmount);
					}else{
						updateParams.addAttr("feeAmount",feeAmount);
					}
					//投诉标识 true-被投诉 false-未投诉
					boolean isComplained = (boolean) map.get("IsComplained");
					if(isComplained){
						updateParams.addAttr("feeAmount",0);
					}
					String recStatus = StringUtil.getString(map.get("RecStatus"));
					updateParams.addAttr("recStatus",recStatus);
					// recStatus 0-未审核 1-审核通过 2-审核拒绝 3-金额已审核(代扣) 4-已发起代扣 5-金账户处理中  6-金账户待充值
					if("0".equals(recStatus)){
						updateParams.addAttr("status",1); // status = 1 未核算
					}else if("1".equals(recStatus)){
						updateParams.addAttr("status",2); // status = 2 已核算
					}else if("2".equals(recStatus)){
						updateParams.addAttr("status",3); // status = 3 核算失败
					}else if("3".equals(recStatus)){
						updateParams.addAttr("status",2); 
						updateParams.addAttr("recStatus",1);
					}else if("4".equals(recStatus)){
						updateParams.addAttr("status",1); 
						updateParams.addAttr("recStatus",0);
					}else{
						updateParams.addAttr("status",1); 
						updateParams.addAttr("recStatus",0);
					}
					updateParams.addAttr("errorMessage", "");
					updateParams.addAttr("feeAmountDate",payTime);
					result = this.insert(updateParams);
					if(result.isSuccess()){
						//同步回款数据
						Map<String, Object> sendParam = new HashMap<String, Object>();
						sendParam.put("recordDate",payTime);//记录日期
						StoreOptUtil.dealStoreOrderByMq(customerId,"countDealType", sendParam);
						StoreOptUtil.dealStoreOrderByMq(customerId,"backDealType", sendParam);
						
						// 同步回款历史表
						AppParam treatHistoryParam = new AppParam("treatSuccessHistoryService","updateOrInsert");
						String repayId = StringUtil.getString(map.get("BookID"));
						treatHistoryParam.addAttr("repayId", repayId);
						updateParams.removeAttr("createTime");
						treatHistoryParam.addAttrs(updateParams.getAttr());
						SoaManager.getInstance().invoke(treatHistoryParam);
						newBookIds.add(repayId);
					}
				}
				//新回款笔数
				int newBackCount = recordList.size();
				// 查询已有的回款记录
				AppParam queryHistoryParam = new AppParam("treatSuccessHistoryService","query");
				queryHistoryParam.addAttr("applyId", applyId);
				queryHistoryParam.addAttr("reContractId", reContractId);
				AppResult queryResult = SoaManager.getInstance().invoke(queryHistoryParam);
				int currentBackCount = queryResult.getRows().size();
				//判断回款笔数是否减少，减少则删除这笔回款
				if(currentBackCount > newBackCount){
					List<String> currBookIds = new ArrayList<String>();
					for(Map<String,Object> map : queryResult.getRows()){
						if(!StringUtils.isEmpty(map.get("repayId"))){
							currBookIds.add(StringUtil.getString(map.get("repayId")));
						}else{
							//查询是否存在两笔一样的回款,如果存在则把有repayId回款的申请时间和渠道更新,没有repayId的则删除
							AppParam queryParam = new AppParam("treatSuccessHistoryService","query");
							queryParam.addAttr("applyId", applyId);
							queryParam.addAttr("reContractId", reContractId);
							queryParam.addAttr("loanNo", map.get("loanNo"));
							queryParam.addAttr("feeAmount", map.get("feeAmount"));
							queryParam.addAttr("feeAmountDate", map.get("feeAmountDate"));
							queryParam.addAttr("recStatus", map.get("recStatus"));
							queryParam.addAttr("status", map.get("status"));
							queryParam.addAttr("repayFlag","1");
							AppResult history = SoaManager.getInstance().invoke(queryParam);
							if(history.getRows().size() > 0){
								String recordId = StringUtil.getString(history.getRow(0).get("recordId"));
								if(!StringUtils.isEmpty(recordId)){
									AppParam updateParam = new AppParam("treatSuccessHistoryService","update");
									updateParam.addAttr("recordId", recordId);
									updateParam.addAttr("applyTime", map.get("applyTime"));
									updateParam.addAttr("channelCode", map.get("channelCode"));
									updateParam.addAttr("channelDetail", map.get("channelDetail"));
									SoaManager.getInstance().invoke(updateParam);
								}
							}
							//删除没有repayId的回款
							String recordId = StringUtil.getString(map.get("recordId"));
							AppParam deleteParam = new AppParam("treatSuccessHistoryService","delete");
							deleteParam.addAttr("recordId", recordId);
							SoaManager.getInstance().invoke(deleteParam);
						}
					}
					//删除CFS不存在的回款数据
					for(String bookId : currBookIds){
						AppParam deleteParam = new AppParam("treatSuccessHistoryService","deleteByRepayId");
						if(!newBookIds.contains(bookId)){
							deleteParam.addAttr("applyId", applyId);
							deleteParam.addAttr("reContractId", reContractId);
							deleteParam.addAttr("repayId", bookId);
							SoaManager.getInstance().invoke(deleteParam);
						}
					}
				}
			}
		}
		return result;
	}
	
	/**
	 * 删除数据
	 * @param params
	 * @return
	 * @throws ParseException 
	 */
	public AppResult deleteData(AppParam params) throws ParseException {
		String applyId = StringUtil.getString(params.getAttr("applyId"));
		AppResult result = new AppResult();
		if(!StringUtils.isEmpty(applyId)){
			AppResult appResult = this.deleteBack(params);
			if(appResult.isSuccess()){
				result = this.insert(params);
			}
		}
		return result;
	}
	
	/**
	 * queryRetBackInfo
	 * @param params
	 * @return
	 */
	public AppResult queryRetBackInfo(AppParam params) {
		return super.query(params, NAMESPACE,"queryRetBackInfo");
	}
	

	/**后台人员录入回款信息
	 * adminAddTreatSucInfo
	 * @param params
	 * @return
	 */
	public AppResult adminAddTreatSucInfo(AppParam params) {
		
		AppResult result = this.queryRetBackInfo(params);
		int size = result.getRows().size();
		if(size ==0){
			result.setMessage("借款人不存在或业务经理不存在，请核查借款人和业务经理的手机是否正确!");
			result.setSuccess(false);
			return result;
		}
		if(size > 0){
			Map<String,Object> resultMap = result.getRow(0);
			if(resultMap != null && !resultMap.isEmpty()){
				Object customerId = resultMap.get("customerId");
				Object applyId = resultMap.get("applyId");
				
				if(StringUtils.isEmpty(customerId)){
					result.setMessage("业务员信息不存在，请检查业务员手机号码是否正确或已注册");
					result.setSuccess(false);
					return result;
				}
				
				if(StringUtils.isEmpty(customerId)){
					result.setMessage("借款人借款记录不存在，请核实该借款人是否有借款记录");
					result.setSuccess(false);
					return result;
				}

				params.addAttr("customerId", customerId);
				params.addAttr("applyId", applyId.toString());
				params.addAttr("status", "2");//admin 添加默认都市审核通过的
				result = this.insert(params);
				
				if(result.isSuccess()){
					AppParam applyParams = new AppParam("borrowApplyService", "update");
					applyParams.addAttr("storeStatus",BorrowConstant.STORE_OPER_4);
					applyParams.addAttr("applyId", applyId);
					SoaManager.getInstance().invoke(applyParams);
				}
			}
		}
		
		return result;
	}
	
	/**批量导入回款信息
	 * adminAddTreatSucInfo
	 * @param params
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public AppResult batchImportTreatSuc(AppParam params) {
		
		AppResult result = new AppResult();
		List<Map<String,Object>> treatSucList = (List<Map<String,Object>>)params.getAttr("treatSucList");
		StringBuilder sb = new StringBuilder();
		for(Map<String, Object> tmpMap : treatSucList){
			AppParam tmpParams = new AppParam();
			String seriNo = StringUtil.getString(tmpMap.get("seriNo"));
			try{
				tmpParams.addAttrs(tmpMap);
				if(!StringUtils.isEmpty(tmpParams.getAttr("borrowTelephone")) && 
						!StringUtils.isEmpty(tmpParams.getAttr("loanTelephone"))){
					
					result = this.queryRetBackInfo(tmpParams);
				}else{
					tmpParams = null;
					sb.append(seriNo).append(",");
					continue;
				}
				
				int size = result.getRows().size();
				if(size > 0){
					Map<String,Object> resultMap = result.getRow(0);
					if(resultMap != null && !resultMap.isEmpty()){
						Object customerId = resultMap.get("customerId");
						Object applyId = resultMap.get("applyId");
						if(StringUtils.isEmpty(customerId)){
							sb.append(seriNo).append("[业务员不存在，请检查业务员号码是否正确],");
							continue;
						}
						if(StringUtils.isEmpty(applyId)){
							sb.append(seriNo).append("[借款人不存在，请检查借款人号码是否正确],");
							continue;
						}
						tmpParams.addAttr("customerId", customerId);
						tmpParams.addAttr("applyId", applyId.toString());
						tmpParams.addAttr("status", "2");//admin 添加默认都市审核通过的
						result = this.insert(tmpParams);
						
						if(result.isSuccess()){
							AppParam applyParams = new AppParam("borrowApplyService", "update");
							applyParams.addAttr("storeStatus",BorrowConstant.STORE_OPER_4);
							applyParams.addAttr("applyId", applyId);
							SoaManager.getInstance().invoke(applyParams);
						}
					}
				}else{
					sb.append(seriNo).append(",");
				}
				tmpParams = null;
			}catch(Exception e){
				sb.append(seriNo).append(",");
				log.error("导入回款信息报错：" +tmpMap);
			}
			
		}
		if(!StringUtils.isEmpty(sb.toString())){
			result.setMessage(sb.toString()+"以上序号导入不成功，请核实！");
			result.setSuccess(false);
		}
		return result;
	}
	
	
	/**
	 * 
	 * @param params
	 * @return
	 */
	public AppResult batchCheck(AppParam params) {
		String ids = (String) params.getAttr("ids");
		if (!StringUtils.isEmpty(ids)) {
			for (String recordId : ids.split(",")) {
				AppParam param = new AppParam();
				param.addAttr("recordId", recordId);
				this.check(param);
			}
		}
		return new AppResult();
	}
	
	/**
	 * 核算确认
	 * @param params
	 * @return
	 */
	public AppResult check(AppParam params) {
		AppResult result = new AppResult();
		Object recordId = params.getAttr("recordId");
		if(StringUtils.isEmpty(recordId)){
			throw new AppException(DuoduoError.UPDATE_NO_PARAMS);
		}
		Map<String,Object> treatInfo = queryById(recordId);
		String status = treatInfo.get("status").toString();
		Object feeAmount = treatInfo.get("feeAmount");
		if("1".equals(status) && !StringUtils.isEmpty(feeAmount)
				&& NumberUtil.getDouble(feeAmount) > 0){
			Object customerId = treatInfo.get("customerId");
			AppParam updateParam = new AppParam();
			updateParam.addAttr("recordId", recordId);
			updateParam.addAttr("customerId", customerId);
			updateParam.addAttr("status", "2");
			updateParam.addAttr("fromStatus", "1");
			AppResult updateResult = this.update(updateParam);
			
			if(1 == Integer.valueOf(updateResult.getAttr(DuoduoConstant.DAO_Update_SIZE).toString())){
				
				try{
					  AppParam queryParam = new AppParam("borrowApplyService", "query");
					  queryParam.addAttr("applyId", treatInfo.get("applyId"));
					  AppResult queryResult = SoaManager.getInstance().invoke(queryParam);
					  
					  if(queryResult.getRows().size() > 0){
						  
						  Map<String,Object> queryMap = queryResult.getRow(0);
						  Object channelCode = queryMap.get("channelCode");
						  Object lastStore = queryMap.get("lastStore");
						  Object lastKf = queryMap.get("lastKf");
						  Object recordDate = treatInfo.get("feeAmountDate");
						   
						  	//更新总的回款统计
							AppParam tjParams = new AppParam("sumRetBaseService", "updateSucRetAmount");
							tjParams.addAttr("sucRetAmount", feeAmount);
							tjParams.addAttr("sucRetCount", 1);
							tjParams.addAttr("recordDate", recordDate);
							tjParams.setRmiServiceName(AppProperties
									.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
							
							if(!StringUtils.isEmpty(recordDate)){
								RemoteInvoke.getInstance().call(tjParams);
								
								if(!StringUtils.isEmpty(channelCode)){
									//更新渠道的回款统计
									tjParams.addAttr("channelCode", channelCode);
									tjParams.setService("sumRetChannelService");
									RemoteInvoke.getInstance().call(tjParams);
								}
								
								if(!StringUtils.isEmpty(lastStore)){
									//更新门店人员的回款统计
									tjParams.addAttr("lastStore", lastStore);
									tjParams.setService("sumRetStoreService");
									RemoteInvoke.getInstance().call(tjParams);
								}
								
								if(!StringUtils.isEmpty(lastKf)){
									//更新客服的回款统计
									tjParams.addAttr("lastKf", lastKf);
									tjParams.setService("sumRetKfService");
									RemoteInvoke.getInstance().call(tjParams);
								}
								
							}
					  }
					
				}catch(Exception e){
					log.error("回款审核时，更新统计回款报错", e);
				}
			}
			
		}
		
		return result;
	}
	
	/**
	 * delete
	 * @param params
	 * @return
	 */
	public AppResult delete(AppParam params) {
		String ids = (String) params.getAttr("ids");
		AppResult  result = null;
		if (!StringUtils.isEmpty(ids)) {
			for (String id : ids.split(",")) {
				AppParam param = new AppParam();
				param.addAttr("recordId", id);
				
				result = super.delete(param, NAMESPACE);
			}
		} else if (!StringUtils.isEmpty(params.getAttr("recordId"))) {
			result = super.delete(params, NAMESPACE);
		} else {
			throw new AppException(DuoduoError.DELETE_NO_ID);
		}
		if(result.isSuccess()){
			//删除缓存
			RedisUtils.getRedisService().del(StoreApplyUtils.STORE_APPLY_RET_RECORD + params.getAttr("applyId"));
		}
		return result;
	}
	
	/**
	 * 
	 * @param recordId
	 * @return
	 */
	private Map<String,Object> queryById(Object recordId){
		AppParam queryParam = new AppParam();
		queryParam.addAttr("recordId", recordId);
		AppResult queryResult = this.query(queryParam);
		if(queryResult.getRows().size() == 0){
			throw new SysException(DuoduoError.UPDATE_DATA_IS_NOTEXISTS);
		}
		return queryResult.getRow(0);
	}
}
