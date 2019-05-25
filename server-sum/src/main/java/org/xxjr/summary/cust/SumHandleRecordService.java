package org.xxjr.summary.cust;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.ddq.common.exception.AppException;
import org.ddq.common.exception.DuoduoError;
import org.llw.common.core.service.BaseService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.xxjr.sys.util.DBConst;
import org.xxjr.sys.util.NumberUtil;

@Lazy
@Service
public class SumHandleRecordService extends BaseService {
	private static final String NAMESPACE = "SUMHANDLERECORD";

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
				param.setDataBase(DBConst.Key_sum_DB);
				param.addAttr("recordDate", id);
				
				result = super.delete(param, NAMESPACE);
			}
		} else if (!StringUtils.isEmpty(params.getAttr("recordDate"))) {
			result = super.delete(params, NAMESPACE);
		} else {
			throw new AppException(DuoduoError.DELETE_NO_ID);
		}
		if (!StringUtils.isEmpty(ids)) {
			for (String id : ids.split(",")) {
				AppParam param = new AppParam();
				param.addAttr("customerId", id);
				
				result = super.delete(param, NAMESPACE);
			}
		} else if (!StringUtils.isEmpty(params.getAttr("customerId"))) {
			result = super.delete(params, NAMESPACE);
		} else {
			throw new AppException(DuoduoError.DELETE_NO_ID);
		}
		return result;
	}
	
	/**
	 * 保存门店操作记录统计数据
	 * @param params
	 * @return
	 */
	public AppResult save(AppParam params){
		AppResult result = new AppResult();
		result = this.update(params);
		int updateSize = NumberUtil.getInt(result.getAttr(DuoduoConstant.DAO_Update_SIZE),0);
		if(updateSize == 0){
			result = this.insert(params);
		}
		return result;
	}
	/**
	 * 查询今日、本月门店操作记录
	 * @param params
	 * @return
	 */
	public AppResult queryHandRecordByDate(AppParam params) {
		return super.queryByPage(params, NAMESPACE,"queryHandRecordByDate","queryHandRecordByDateCount");
	}
	
	/**
	 * 查询今日、本月、月度门店操作记录总计
	 * @param params
	 * @return
	 */
	public AppResult queryHandleRecordSum(AppParam params) {
		return super.query(params, NAMESPACE,"queryHandleRecordSum");
	}
	
	/**
	 * 查询月度门店操作记录
	 * @param params
	 * @return
	 */
	public AppResult queryHandRecordMonth(AppParam params) {
		return super.queryByPage(params, NAMESPACE,"queryHandRecordMonth","queryHandRecordMonthCount");
	}
	/**
	 * 批量保存操作记录
	 * @param params
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public AppResult batchSaveHandleRecord(AppParam params) {
		AppResult result = new AppResult();
		List<Map<String,Object>> dataList = (List<Map<String, Object>>) params.getAttr("dataList");
		int sucSize = 0;
		for(Map<String,Object> map : dataList){
			AppParam saveParam = new AppParam();
			saveParam.addAttr("recordDate", map.get("recordDate"));
			saveParam.addAttr("customerId", map.get("customerId"));
			saveParam.addAttr("realName", map.get("realName"));
			saveParam.addAttr("traOrderCount", map.get("traOrderCount"));
			saveParam.addAttr("backOrderCount", map.get("backOrderCount"));
			saveParam.addAttr("authCount", map.get("authCount"));
			saveParam.addAttr("leaDealCount", map.get("leaDealCount"));
			saveParam.addAttr("cfsCount", map.get("cfsCount"));
			saveParam.addAttr("audioCount", map.get("audioCount"));
			saveParam.addAttr("exportCount", map.get("exportCount"));
			saveParam.setDataBase(DBConst.Key_sum_DB);
			result = this.save(saveParam);
			if(result.isSuccess()){
				sucSize ++;
			}
		}
		result.putAttr("sucSize", sucSize);
		return result;
	}
}
