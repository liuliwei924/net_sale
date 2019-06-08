package org.xxjr.busiIn.kf.record;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.service.SoaManager;
import org.ddq.common.exception.AppException;
import org.ddq.common.exception.DuoduoError;
import org.ddq.common.util.DateUtil;
import org.ddq.common.util.StringUtil;
import org.llw.common.core.service.BaseService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.xxjr.busi.util.ApplyAllotUtil;
import org.xxjr.busi.util.BorrowConstant;
import org.xxjr.busi.util.CountGradeUtil;
import org.xxjr.cust.util.info.CustomerUtil;
import org.xxjr.sys.util.NumberUtil;
import org.xxjr.sys.util.ValidUtils;

import lombok.extern.slf4j.Slf4j;

@Lazy
@Service
@Slf4j
public class ThirdDataService extends BaseService {
	private static final String NAMESPACE = "THIRDDATA";

	/**
	 * querys
	 * @param params
	 * @return
	 */
	public AppResult query(AppParam params) {
		return super.query(params, NAMESPACE);
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
		params.addAttr("createTime", new Date());
		return super.insert(params, NAMESPACE);
	}
	
	/**
	 * update
	 * @param params
	 * @return
	 */
	public AppResult update(AppParam params) {
		return super.update(params, NAMESPACE);
	}
	
	/**
	 * 保存数据
	 * @param params
	 * @return
	 */
	public AppResult save(AppParam params) {
		AppResult result = new AppResult();
		
		String cityName = StringUtil.getString(params.getAttr("cityName"));

		if(!cityName.endsWith("市")){
			cityName = cityName + "市";
		}
		params.addAttr("havePinan", params.getAttr("haveWeiLi"));
		
		String grade = CountGradeUtil.getGrade(params.getAttr()) ;
		
		params.addAttr("cityName", cityName);
		params.addAttr("grade", grade);
		params.addAttr("applyType", BorrowConstant.apply_type_1);
		// 保存第三方数据
		this.insert(params);
		
		// 非重复的 或者 处理中的不进行更新操作
		if("1".equals(StringUtil.getString(params.getAttr("isHandling"))) 
				|| "1".equals(StringUtil.getString(params.getAttr("isRepeat")))){
			return result;
		}
		
		String applyName = StringUtil.getString(params.getAttr("applyName"));
		if(applyName.indexOf("测试")> -1){
			return result;
		}
		AppResult saveResult = new AppResult();
		AppParam context = new AppParam("borrowApplyService",
				"saveAllApply");
		context.addAttr("applyId", params.getAttr("applyId"));
		//context.addAttr("customerId", customerId);
		oldValueToNew(params.getAttr());
		context.addAttrs(params.getAttr());
		context.addAttr("channelCode", params.getAttr("sourceChannel"));
		context.addAttr("haveDetail", "1");
		try {
			saveResult = SoaManager.getInstance().callNewTx(context);
		} catch (Exception e) {//同一时间插入多条可能失败需要返回插入重复
			saveResult.setSuccess(false);
			log.error("third Data save error", e);
		}
		if (saveResult.isSuccess()) {
			String applyId = StringUtil.objectToStr(saveResult.getAttr("applyId"));
			result.putAttr("unionId", CustomerUtil.getEncrypt(applyId));
		}else {
			AppParam updateParam = new AppParam();
			updateParam.addAttr("isRepeat", "1");
			updateParam.addAttr("recordId", params.getAttr("recordId"));
			this.update(updateParam);
			result.setErrorCode("003");
			result.setSuccess(false);
			result.setMessage("申请重复");
		}
		return result;
	}
	
	private void oldValueToNew (Map<String, Object> row) {
		if (!StringUtils.isEmpty(row.get("workType"))) {
			int workType = NumberUtil.getInt(row.get("workType"), 1);
			if (workType == 5) {
				row.put("workType", 1);
			}
		}
		
		if (!StringUtils.isEmpty(row.get("houseType"))) {
			int houseType = NumberUtil.getInt(row.get("houseType"), 2);
			if (houseType > 4) {
				row.put("houseType", 1);
			}
		}
		
		if (!StringUtils.isEmpty(row.get("carType"))) {
			int carType = NumberUtil.getInt(row.get("carType"), 2);
			if (carType == 1) {
				row.put("carType", 3);
			}else if (carType == 5) {
				row.put("carType", 2);
			}
		}
		
		if (!StringUtils.isEmpty(row.get("creditType"))) {
			int creditType = NumberUtil.getInt(row.get("creditType"), 2);
			if (creditType == 3) {
				row.put("creditType", 5);
			}
		}
		
		if (!StringUtils.isEmpty(row.get("age"))) {
			int age = NumberUtil.getInt(row.get("age"), 0);
			if (age <= 0 || age >= 100) {
				row.remove("age");
			}
		}
	}
	
	
	public AppResult updateDate(AppParam params) {
		AppResult result = new AppResult();
		
		String applyId = CustomerUtil.getDecrypt(StringUtil.objectToStr(params.getAttr("unionId")));
		
		if (StringUtils.isEmpty(applyId)) {
			result.setErrorCode("0016");
			result.setMessage("unionId解析错误");
			return result;
		}
		
		AppParam queryParam = new AppParam("borrowBaseService", "queryBaseInfo");
		queryParam.addAttr("applyId", applyId);
		AppResult queryResult = SoaManager.getInstance().callNoTx(queryParam);
		if (queryResult.getRows().size() != 1) {
			result.setErrorCode("0017");
			result.setMessage("数据不存在!");
			return result;
		}
		Map<String, Object> row = queryResult.getRow(0);
		row.putAll(params.getAttr());
		row.put("applyId", applyId);
		
		ApplyAllotUtil.conversionType(row);
	
		String grade = CountGradeUtil.getGrade(row) ;
		
		String cityName = StringUtil.getString(row.get("cityName"));
		
		double loanAmount = NumberUtil.getDouble(row.get("loanAmount"), 0);
		int allotFlag = ApplyAllotUtil.allot(grade, cityName, 0, loanAmount);//分单
		
		AppParam updateParam = new AppParam();
		updateParam.addAttrs(row);
		updateParam.addAttr("applyId", row.get("applyId"));
		updateParam.addAttr("grade", grade);
		updateParam.addAttr("allotFlag", allotFlag);
		updateParam.setService("borrowBaseService");
		updateParam.setMethod("updateBaseInfo");
		
		//修改busiIn里面相关资产表的信息
		AppResult updateResult = SoaManager.getInstance().invoke(updateParam);
		int size = NumberUtil.getInt(updateResult.getAttr(DuoduoConstant.DAO_Insert_SIZE), 0);
		if (size == 0) {
			return CustomerUtil.retErrorMsg("数据不存在!");
		}
		
		if(allotFlag == 1){//如果之前没有分给网销，重新分给网销
			ApplyAllotUtil.newOrderAllotOrNet(updateParam);
		}
		
		return result;
	}
	
	/**
	 * 导入贷上我的数据
	 * @param params
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public AppResult batchImportDswData(AppParam params) {
		AppResult result = new AppResult();
		
		
		List<Map<String,Object>> dswDataList = (List<Map<String,Object>>)params.getAttr("dswDataList");
		StringBuilder serIdSb = new StringBuilder();
		int errCount = 0;
		int sucCount = 0;
		Date today = new Date();
		for(Map<String, Object> dswDataMap : dswDataList){
				String serId = StringUtil.getString(dswDataMap.get("serId"));
				if(StringUtils.isEmpty(serId)){//忽略计算
					continue;
				}
				
				String applyName = StringUtil.getString(dswDataMap.get("applyName"));
				String telephone = StringUtil.getString(dswDataMap.get("telephone"));
				String cityName = StringUtil.getString(dswDataMap.get("cityName"));
				double income =NumberUtil.getDouble(dswDataMap.get("income"),0);
				String sourceChannel = StringUtil.getString(dswDataMap.get("sourceChannel"));
				boolean isRepeat = false; 
				
				if(StringUtils.isEmpty(applyName) || 
						!ValidUtils.validateTelephone(telephone) || 
						StringUtils.isEmpty(cityName) || StringUtils.isEmpty(sourceChannel)){
					serIdSb.append(serId).append(",");
					errCount++;
					continue;
				}
				if(!cityName.endsWith("市")){
					cityName = cityName + "市";
				}
				AppParam queryParam = new AppParam();
				queryParam.addAttr("telephone", telephone);
				queryParam.addAttr("recordDate", DateUtil.toStringByParttern(today,
														DateUtil.DATE_PATTERN_YYYY_MM_DD));
				AppResult qeuryResult = this.queryCount(queryParam);
				int count = NumberUtil.getInt(qeuryResult.getAttr(DuoduoConstant.TOTAL_SIZE), 1);
				
				if(count >0){
					serIdSb.append(serId).append(",");
					errCount++;
					continue;
				}
				
				queryParam = new AppParam("borrowApplyService", "query");
				queryParam.addAttr("telephone", telephone);
				qeuryResult = SoaManager.getInstance().invoke(queryParam);
				if(qeuryResult.getRows().size() > 0){
					serIdSb.append(serId).append(",");
					errCount++;
					isRepeat = true;
				}
	
				String grade = CountGradeUtil.getGrade(dswDataMap);
			
				// 保存第三方数据
				dswDataMap.put("grade", grade);//客户等级
				dswDataMap.put("cityName", cityName);
				dswDataMap.put("isRepeat", isRepeat ? 1 :0);
				dswDataMap.put("wagesType", income > 0 ? 1 :2);
				AppParam applyParams = new AppParam();
				applyParams.addAttrs(dswDataMap);
				oldValueToNew(dswDataMap);
				applyParams.addAttr("applyType", BorrowConstant.apply_type_1);
				AppResult dswResult = this.insert(applyParams);
				
				if(dswResult.isSuccess() && !isRepeat){
					applyParams.setService("borrowApplyService");
					applyParams.setMethod("saveAllApply");
					applyParams.addAttr("channelCode", sourceChannel);
					applyParams.addAttr("channelDetail", sourceChannel);
					applyParams.addAttr("pubManageLine", dswDataMap.get("pubAmount"));
					applyParams.addAttr("pageReferer", sourceChannel);
					applyParams.addAttr("applyTime", new Date());
					result = SoaManager.getInstance().invoke(applyParams);
					
					if(result.isSuccess()){
						sucCount ++ ;
					}
				}
		}
		
		result.putAttr("errCount", errCount);
		result.putAttr("sucCount", sucCount);
		result.setMessage(serIdSb.toString());
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
		return result;
	}
	
	/**
	 * 查询贷款线索数
	 */
	public AppResult queryApplyCount(AppParam params){
		return super.query(params, NAMESPACE, "queryApplyCount");
	}
	
	
	public AppResult queryApplyId (AppParam params) {
		return super.query(params, NAMESPACE, "queryApplyId");
	}
}
