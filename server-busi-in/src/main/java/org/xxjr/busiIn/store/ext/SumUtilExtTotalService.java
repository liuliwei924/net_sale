package org.xxjr.busiIn.store.ext;

import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.llw.common.core.service.BaseService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.xxjr.busiIn.utils.StoreOptUtil;

/**
 * 统计总计工具
 * @author chencx
 *
 */
@Lazy
@Service
public class SumUtilExtTotalService extends BaseService {
	public static final String NAMESPACE = "SUMUTILEXTTOTAL";

	/**
	 * 门店今日统计总计
	 * @param params
	 * @return
	 */
	public AppResult queryOrgToDayTotal(AppParam params) {
		String tableName = StoreOptUtil.getTableName(null);
		params.addAttr("tableName", tableName);
		return super.query(params, NAMESPACE, "queryOrgToDayTotal");
	}
	/**
	 * 门店人员今日统计总计
	 * @param params
	 * @return
	 */
	public AppResult queryStoreToDayTotal(AppParam params) {
		String tableName = StoreOptUtil.getTableName(null);
		params.addAttr("tableName", tableName);
		return super.query(params, NAMESPACE, "queryStoreToDayTotal");
	}
	/**
	 * 按城市维度本周相关数据统计
	 * @param params
	 * @return
	 */
	public AppResult queryThisWeekByCity(AppParam params) {
		return super.query(params, NAMESPACE, "queryThisWeekByCity");
	}
	
	/**
	 * 按城市维度本月相关数据统计
	 * @param params
	 * @return
	 */
	public AppResult queryThisMonthByCity(AppParam params) {
		return super.query(params, NAMESPACE, "queryThisMonthByCity");
	}
	/**
	 * 按城市维度月度相关数据统计
	 * @param params
	 * @return
	 */
	public AppResult queryMonthlyByCity(AppParam params) {
		return super.query(params, NAMESPACE, "queryMonthlyByCity");
	}
	
	/**
	 * 按门店维度本周相关数据统计
	 * @param params
	 * @return
	 */
	public AppResult queryThisWeekByOrg(AppParam params) {
		return super.query(params, NAMESPACE, "queryThisWeekByOrg");
	}
	
	/**
	 * 按门店维度本月相关数据统计
	 * @param params
	 * @return
	 */
	public AppResult queryThisMonthByOrg(AppParam params) {
		return super.query(params, NAMESPACE, "queryThisMonthByOrg");
	}
	/**
	 * 按门店维度月度相关数据统计
	 * @param params
	 * @return
	 */
	public AppResult queryMonthlyByOrg(AppParam params) {
		return super.query(params, NAMESPACE, "queryMonthlyByOrg");
	}
	/**
	 * 按日期维度查询相关数据统计
	 * @param params
	 * @return
	 */
	public AppResult queryDataAnalyByDate(AppParam params) {
		return super.query(params, NAMESPACE, "queryDataAnalyByDate");
	}
	
	/**
	 * 按门店维度查询门店回款相关数据统计
	 * @param params
	 * @return
	 */
	public AppResult queryOrgRepaymentByDate(AppParam params) {
		return super.query(params, NAMESPACE, "queryOrgRepaymentByDate");
	}
	
	/**
	 * 按城市维度查询城市回款相关数据统计
	 * @param params
	 * @return
	 */
	public AppResult queryCityRepaymentByDate(AppParam params) {
		return super.query(params, NAMESPACE, "queryCityRepaymentByDate");
	}
	
}
