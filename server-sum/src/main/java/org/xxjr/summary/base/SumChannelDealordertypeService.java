package org.xxjr.summary.base;

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

@Lazy
@Service
public class SumChannelDealordertypeService extends BaseService {
	private static final String NAMESPACE = "SUMCHANNELDEALORDERTYPE";

	/**
	 * querys
	 * @param params
	 * @return
	 */
	public AppResult query(AppParam params) {
		return super.query(params, NAMESPACE);
	}
	
	/**
	 * 订单状态渠道 本月统计
	 * @param params
	 * @return
	 */
	public AppResult queryChannelDealordertypeDay(AppParam params) {
		return super.queryByPage(params, NAMESPACE,"queryChannelDealordertypeDay", "queryChannelDealordertypeDayCount");
	}
	/**
	 * queryChannelDealordertypeDayCount
	 * @param params
	 * @return
	 */
	public AppResult queryChannelDealordertypeDayCount(AppParam params) {
		int size = getDao().count(NAMESPACE, "queryChannelDealordertypeDayCount",params.getAttr(),params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
		return result;
	}
	
	/**
	 * 订单状态渠道  本月统计总计
	 * @param params
	 * @return
	 */
	public AppResult queryChannelDealordertypeDaySum(AppParam params) {
		return super.query(params, NAMESPACE,"queryChannelDealordertypeDaySum");
	}
	
	
	/**
	 * 订单状态渠道 月度统计
	 * @param params
	 * @return
	 */
	public AppResult queryChannelDealordertypeMonth(AppParam params) {
		return super.queryByPage(params, NAMESPACE,"queryChannelDealordertypeMonth", "queryChannelDealordertypeMonthCount");
	}
	/**
	 * queryChannelDealordertypeMonthCount
	 * @param params
	 * @return
	 */
	public AppResult queryChannelDealordertypeMonthCount(AppParam params) {
		int size = getDao().count(NAMESPACE, "queryChannelDealordertypeDayCount",params.getAttr(),params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
		return result;
	}
	/**
	 * 订单状态渠道 本月统计总计
	 * @param params
	 * @return
	 */
	public AppResult queryChannelDealordertypeMonthSum(AppParam params) {
		return super.query(params, NAMESPACE,"queryChannelDealordertypeMonthSum");
	}
	
	/**
	 * 订单状态渠道统计
	 * @param params
	 * @return
	 */
	public AppResult save(AppParam params){
		AppResult result = new AppResult();
		// 删除旧数据
		super.getDao().delete(NAMESPACE, "deleteByDay", params.getAttr(), params.getDataBase());
		//插入新的数据
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> list = (List<Map<String, Object>>) params.getAttr("list");
		int size = super.getDao().batchInsert(NAMESPACE, "batchInsert",
				list, params.getDataBase());
		result.putAttr(DuoduoConstant.DAO_Insert_SIZE, size);
		return result;
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
				param.addAttr("recordDate", id);
				
				result = super.delete(param, NAMESPACE);
			}
		} else if (!StringUtils.isEmpty(params.getAttr("recordDate"))) {
			result = super.delete(params, NAMESPACE);
		} else {
			throw new AppException(DuoduoError.DELETE_NO_ID);
		}
		return result;
	}
	
}
