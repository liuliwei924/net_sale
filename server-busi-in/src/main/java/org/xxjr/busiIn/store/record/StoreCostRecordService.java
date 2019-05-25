package org.xxjr.busiIn.store.record;

import java.util.Date;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.service.SoaManager;
import org.ddq.common.exception.AppException;
import org.ddq.common.exception.DuoduoError;
import org.ddq.common.util.StringUtil;
import org.ddq.common.web.session.DuoduoSession;
import org.llw.common.core.service.BaseService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.xxjr.sys.util.NumberUtil;


@Lazy
@Service
public class StoreCostRecordService extends BaseService {
	private static final String NAMESPACE = "STORECOSTRECORD";

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
	 * 查询门店人员成本
	 * @param params
	 * @return
	 */
	public AppResult queryCostCount(AppParam params) {
		int size = getDao().count(NAMESPACE, "queryCostCount",params.getAttr(),params.getDataBase());
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
		params.addAttr("createBy", DuoduoSession.getUserName());
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
	
	/***
	 * 根据applyId及customerId删除成本记录
	 * @param params
	 * @return
	 */
	public AppResult delCostByApplyId(AppParam params){
		return super.delete(params, NAMESPACE);
	}
	
	
	/***
	 * 根据applyId及customerId修改成本记录
	 * @param params
	 * @return
	 */
	public AppResult updateCostByApplyId(AppParam params){
		AppResult result = new AppResult();
		params.addAttr("updateTime", new Date());
		int size = super.getDao().update(NAMESPACE, "updateCostByApplyId", params.getAttr(), params.getDataBase());
		result.putAttr(DuoduoConstant.DAO_Update_SIZE, size);
		return result;
	}
	
	/**
	 * 查询订单成本单量
	 * @param params
	 * @return
	 */
	public AppResult queryOrderCost(AppParam params) {
		return super.queryByPage(params, NAMESPACE,"queryOrderCost","queryOrderCostCount");
	}
	
	/**
	 * 查询订单成本单量不分页
	 * @param params
	 * @return
	 */
	public AppResult queryOrderCostList(AppParam params) {
		return super.query(params, NAMESPACE,"queryOrderCost");
	}
	
	/**
	 * 查询门店人员成本单量（查询指定时间内的）
	 * @param params
	 * @return
	 */
	public AppResult queryCostCountByCustId(AppParam params) {
		int size = getDao().count(NAMESPACE, "queryCostCountByCustId",params.getAttr(),params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
		return result;
	}
	
	/**
	 * 查询门店成本单量（查询指定时间内的）
	 * @param params
	 * @return
	 */
	public AppResult queryCostCountByOrgId(AppParam params) {
		int size = getDao().count(NAMESPACE, "queryCostCountByOrgId",params.getAttr(),params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
		return result;
	}
	
	/**
	 * 查询门店成本人数（查询指定时间内的）
	 * @param params
	 * @return
	 */
	public AppResult queryStoreCountByOrgId(AppParam params) {
		int size = getDao().count(NAMESPACE, "queryStoreCountByOrgId",params.getAttr(),params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
		return result;
	}
	
	/**
	 * 查询门店人员最终退单数
	 * @param params
	 * @return
	 */
	public AppResult queryStoreBackCostCount(AppParam params) {
		return super.query(params, NAMESPACE,"queryStoreBackCostCount");
	}
	
	/**
	 * 门店人员成本维护处理
	 * @param params
	 * @return
	 */
	public AppResult storeOrgOrderCostDeal(AppParam params) {
		AppResult result = new AppResult();
		String orgId = StringUtil.getString(params.getAttr("orgId"));
		String recordDate = StringUtil.getString(params.getAttr("recordDate"));
		String endRecordDate = StringUtil.getString(params.getAttr("endRecordDate"));
		String recordMonth = StringUtil.getString(params.getAttr("recordMonth"));
		//查询门店配置月最大成本
		AppParam workParams = new AppParam("worktimeCfgService","query");
		workParams.addAttr("orgId", orgId);
		AppResult workResult = SoaManager.getInstance().invoke(workParams);
		int monthMaxCost = 0;
		if(workResult.getRows().size() > 0){
			//门店月最大成本单量
			monthMaxCost = NumberUtil.getInt(workResult.getRow(0).get("monthMaxCost"),0);
		}
		//查询门店实际成本单量
		AppParam costParams = new AppParam();
		costParams.addAttr("orgId", orgId);
		costParams.addAttr("recordDate", recordDate);
		costParams.addAttr("endRecordDate", endRecordDate);
		AppResult costResult = this.queryCostCountByOrgId(costParams);
		//门店实际成本单量
		int orgCoustCount = NumberUtil.getInt(costResult.getAttr(DuoduoConstant.TOTAL_SIZE),0);
		//查询门店成本单人数
		AppParam orgParams = new AppParam("storeCostRecordService","queryStoreCountByOrgId");
		orgParams.addAttr("orgId", orgId);
		orgParams.addAttr("recordDate", recordDate);
		orgParams.addAttr("endRecordDate", endRecordDate);
		AppResult orgResult = this.queryStoreCountByOrgId(orgParams);
		//门店成本单人数
		int orgCount = NumberUtil.getInt(orgResult.getAttr(DuoduoConstant.TOTAL_SIZE),0);
		if(orgCoustCount > monthMaxCost && orgCount != 0){
			double orgCoustDouble = NumberUtil.getDouble(orgCoustCount);
			double orgCountDouble = NumberUtil.getDouble(orgCount);
			//平均退单数
			int avgBackCount = NumberUtil.getInt(Math.ceil((orgCoustDouble - monthMaxCost) / orgCountDouble));
			//平均成本单数
			int avgCostCount = NumberUtil.getInt(Math.ceil(orgCoustDouble / orgCountDouble));
			AppParam backParams = new AppParam();
			backParams.addAttr("orgId", orgId);
			backParams.addAttr("avgBackCount", avgBackCount);
			backParams.addAttr("avgCostCount", avgCostCount);
			backParams.addAttr("recordDate", recordDate);
			backParams.addAttr("endRecordDate", endRecordDate);
			backParams.addAttr("recordMonth", recordMonth);
			result = this.queryStoreBackCostCount(backParams);
		}
		return result;
	}
}
