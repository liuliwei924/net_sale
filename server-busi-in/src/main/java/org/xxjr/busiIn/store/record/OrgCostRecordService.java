package org.xxjr.busiIn.store.record;

import java.util.Date;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.service.SoaManager;
import org.ddq.common.exception.SysException;
import org.ddq.common.util.StringUtil;
import org.llw.common.core.service.BaseService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.xxjr.sys.util.NumberUtil;


@Lazy
@Service
public class OrgCostRecordService extends BaseService {
	private static final String NAMESPACE = "ORGCOSTRECORD";

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
		return super.insert(params, NAMESPACE);
	}
	
	/**
	 * update
	 * @param params
	 * @return
	 */
	public AppResult update(AppParam params) {
		params.addAttr("updateTime", new Date());
		return super.update(params, NAMESPACE);
	}
	
	/**
	 * 计算门店成本
	 * @param params
	 * @return
	 */
	public AppResult jsOrgCost(AppParam params) {
		
		AppResult result = new AppResult();
		String orgId = StringUtil.getString(params.getAttr("orgId"));
		double price = NumberUtil.getDouble(params.getAttr("price"),0);
		if(StringUtils.isEmpty(orgId) || price <=0) 
			throw new SysException("更新门店成本缺少必要参数");
		
		AppParam workCfgParam = new AppParam("worktimeCfgService","subBalanceAmt");
		workCfgParam.addAttr("orgId", orgId);
		workCfgParam.addAttr("subBalanceAmt", price);
		
		AppResult qResult = SoaManager.getInstance().invoke(workCfgParam);
		int updateSize = NumberUtil.getInt(qResult.getAttr(DuoduoConstant.DAO_Update_SIZE),0);
		
		if(updateSize == 1) {
			this.insert(params);
		}else {
			result.setSuccess(false);
			result.setMessage("扣除门店余额失败！");
		}
		return result;
	}

	
	/***
	 * 根据applyId及orgId删除成本记录
	 * @param params
	 * @return
	 */
	public AppResult delete(AppParam params){
		return super.delete(params, NAMESPACE);
	}
	
	/***********门店成本统计***********/
	
	/**门店成本统计(每天和月度)
	 * channelNet
	 * @param params
	 * @return
	 */
	public AppResult queryOrgCost(AppParam params) {
		return super.queryByPage(params, NAMESPACE, "queryOrgCost", "queryOrgCostCount");
	}
	/**
	 * 计数-门店成本统计(每天和月度)
	 * @param params
	 * @return
	 */
	public AppResult queryOrgCostCount(AppParam params) {
		int size = getDao().count(NAMESPACE, "queryOrgCostCount",params.getAttr(),params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
		return result;
	}
	
	/**门店成本统计(区间)
	 * channelNet
	 * @param params
	 * @return
	 */
	public AppResult queryOrgCostRange(AppParam params) {
		return super.queryByPage(params, NAMESPACE, "queryOrgCostRange", "queryOrgCostRangeCount");
	}
	/**
	 * 计数-门店成本统计(区间)
	 * @param params
	 * @return
	 */
	public AppResult queryOrgCostRangeCount(AppParam params) {
		int size = getDao().count(NAMESPACE, "queryOrgCostRangeCount",params.getAttr(),params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
		return result;
	}
	
	
	/***********渠道数据成本统计***********/
	
	
	/**渠道数据成本统计(每天和月度)
	 * channelNet
	 * @param params
	 * @return
	 */
	public AppResult queryChannelCost(AppParam params) {
		return super.queryByPage(params, NAMESPACE, "queryChannelCost", "queryChannelCostCount");
	}
	/**
	 * 计数-渠道数据成本统计(每天和月度)
	 * @param params
	 * @return
	 */
	public AppResult queryChannelCostCount(AppParam params) {
		int size = getDao().count(NAMESPACE, "queryChannelCostCount",params.getAttr(),params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
		return result;
	}
	
	/**渠道数据成本统计(区间)
	 * channelNet
	 * @param params
	 * @return
	 */
	public AppResult queryChannelCostRange(AppParam params) {
		return super.queryByPage(params, NAMESPACE, "queryChannelCostRange", "queryChannelCostRangeCount");
	}
	/**
	 * 计数-渠道数据成本统计(区间)
	 * @param params
	 * @return
	 */
	public AppResult queryChannelCostRangeCount(AppParam params) {
		int size = getDao().count(NAMESPACE, "queryChannelCostRangeCount",params.getAttr(),params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
		return result;
	}
	
	
	/***********渠道数据成本统计***********/
	
	
	/**门店人员数据成本统计(每天和月度)
	 * channelNet
	 * @param params
	 * @return
	 */
	public AppResult queryStoreCost(AppParam params) {
		return super.queryByPage(params, NAMESPACE, "queryStoreCost", "queryStoreCostCount");
	}
	/**
	 * 计数-渠道数据成本统计(每天和月度)
	 * @param params
	 * @return
	 */
	public AppResult queryStoreCostCount(AppParam params) {
		int size = getDao().count(NAMESPACE, "queryStoreCostCount",params.getAttr(),params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
		return result;
	}
	
	/**渠道数据成本统计(区间)
	 * channelNet
	 * @param params
	 * @return
	 */
	public AppResult queryStoreCostRange(AppParam params) {
		return super.queryByPage(params, NAMESPACE, "queryStoreCostRange", "queryStoreCostRangeCount");
	}
	/**
	 * 计数-渠道数据成本统计(区间)
	 * @param params
	 * @return
	 */
	public AppResult queryStoreCostRangeCount(AppParam params) {
		int size = getDao().count(NAMESPACE, "queryStoreCostRangeCount",params.getAttr(),params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
		return result;
	}
}
