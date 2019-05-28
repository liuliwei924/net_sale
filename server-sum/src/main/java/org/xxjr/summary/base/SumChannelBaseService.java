package org.xxjr.summary.base;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.ddq.common.exception.AppException;
import org.ddq.common.exception.DuoduoError;
import org.ddq.common.web.session.DuoduoSession;
import org.llw.common.core.service.BaseService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.xxjr.sys.util.DBConst;

@Lazy
@Service
public class SumChannelBaseService extends BaseService {
	private static final String NAMESPACE = "SUMCHANNELBASE";

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
				param.addAttr("recordDate", id);
				param.setDataBase(DBConst.Key_sum_DB);
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
				param.addAttr("channelCode", id);
				param.setDataBase(DBConst.Key_sum_DB);
				result = super.delete(param, NAMESPACE);
			}
		} else if (!StringUtils.isEmpty(params.getAttr("channelCode"))) {
			result = super.delete(params, NAMESPACE);
		} else {
			throw new AppException(DuoduoError.DELETE_NO_ID);
		}
		return result;
	}
	
	/**
	 * 保存渠道基本统计数据
	 * @param params
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public AppResult save(AppParam params){
		AppResult result = new AppResult();
		//删除老的数据
		super.getDao().delete(NAMESPACE, "deleteByDay", params.getAttr(), params.getDataBase().toString());
		//插入新的数据
		List<Map<String, Object>> list = (List<Map<String, Object>>) params.getAttr("list");
		int size = super.getDao().batchInsert(NAMESPACE, "batchInsert",
				list, params.getDataBase());
		result.putAttr(DuoduoConstant.DAO_Insert_SIZE, size);
		return result;
	}
	
	/**
	 * 渠道数据统计
	 * @param params
	 * @return
	 */
	public AppResult channelSum(AppParam params){
		if(params.getCurrentPage() == -1){
			params.setEveryPage(1000);
			params.setCurrentPage(1);
			return super.queryByPage(params, NAMESPACE,"channelSum","channelSumCount");
			
		}else{
			return super.queryByPage(params, NAMESPACE,"channelSum","channelSumCount");
		}
	}
	
	/**
	 * 渠道月数据统计
	 * @param params
	 * @return
	 */
	public AppResult channelMonthSum(AppParam params){
		if(params.getCurrentPage() == -1){
			params.setEveryPage(1000);
			params.setCurrentPage(1);
			return super.queryByPage(params, NAMESPACE,"channelMonthSum","channelMonthSumCount");
		}else{
			return super.queryByPage(params, NAMESPACE,"channelMonthSum","channelMonthSumCount");
		}
	}
	
	
	/***
	 * 推广分类渠道按天统计
	 * @param params
	 * @return
	 */
	public AppResult tgChannelDay(AppParam params){
		if(params.getCurrentPage() == -1){
			params.setEveryPage(1000);
			params.setCurrentPage(1);
			return super.queryByPage(params, NAMESPACE,"tgChannelDay","tgChannelDayCount");
		}else{
			return super.queryByPage(params, NAMESPACE,"tgChannelDay","tgChannelDayCount");
		}
	}
	
	/***
	 * 推广分类渠道按月统计
	 * @param params
	 * @return
	 */
	public AppResult tgChannelMonth(AppParam params){
		if(params.getCurrentPage() == -1){
			params.setEveryPage(1000);
			params.setCurrentPage(1);
			return super.queryByPage(params, NAMESPACE,"tgChannelMonth","tgChannelMonthCount");
		}else{
			return super.queryByPage(params, NAMESPACE,"tgChannelMonth","tgChannelMonthCount");
		}
	}
	
	/**
	 * 更新渠道花费
	 * @param params
	 * @return
	 */
	public AppResult updateChannelCost(AppParam params){		
		AppResult result = new AppResult();
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> list = (List<Map<String, Object>>) params.getAttr("list");
		if(list.size()>0){
			for(Map<String,Object> paramVal: list){
				Map<String, Object> param = new HashMap<String, Object>();
				param.put("recordDate",paramVal.get("recordDate"));
				param.put("channelCode",paramVal.get("channelCode"));
				param.put("costAmount",paramVal.get("costAmount"));
				param.put("browseCount",paramVal.get("browseCount"));
				int size = super.getDao().update(NAMESPACE, "updateChannelCost", param, params.getDataBase());
				result.putAttr(DuoduoConstant.DAO_Update_SIZE, size);
			}
		}
		result.putAttr(DuoduoConstant.DAO_Update_SIZE, list.size());
		return result;
	}
}
