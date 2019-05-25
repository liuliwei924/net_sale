package org.xxjr.busiIn.store.treat;

import java.util.Date;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.ddq.common.exception.AppException;
import org.ddq.common.exception.DuoduoError;
import org.ddq.common.util.StringUtil;
import org.llw.common.core.service.BaseService;
import org.llw.model.cache.RedisUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.xxjr.busiIn.utils.StoreOptUtil;
import org.xxjr.store.util.StoreApplyUtils;

@Lazy
@Service
public class TreatInfoService extends BaseService {
	private static final String NAMESPACE = "TREATINFO";

	/**
	 * querys
	 * @param params
	 * @return
	 */
	public AppResult query(AppParam params) {
		return super.query(params, NAMESPACE);
	}
	
	/**
	 * 查询合同信息
	 * @param params
	 * @return
	 */
	public AppResult queryTreatInfo(AppParam params) {
		return super.query(params, NAMESPACE, "queryTreatInfo");
	}
	
	/**
	 *  查询简单的信息
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
	
	/**客服系统 业务员交单列表 
	 * queryShowByPage
	 * @param params
	 * @return
	 */
	public AppResult queryShowByPage(AppParam params) {
		return super.queryByPage(params, NAMESPACE,"queryShow","queryShowCount");
	}
	
	/**客服系统 业务员交单列表 
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
		params.addAttr("updateTime", new Date());
		result  = super.insert(params, NAMESPACE);
		result.putAttr("treatyNo", params.getAttr("treatyNo"));
		//删除缓存
		RedisUtils.getRedisService().del(StoreApplyUtils.STORE_APPLY_TREAT_SIGN_RECORD + params.getAttr("applyId"));
		return result;
	}
	
	/**
	 * update
	 * @param params
	 * @return
	 */
	public AppResult update(AppParam params) {
		AppResult result = new AppResult();
		params.addAttr("updateTime", new Date());
		result = super.update(params, NAMESPACE);
		//删除缓存
		RedisUtils.getRedisService().del(StoreApplyUtils.STORE_APPLY_TREAT_SIGN_RECORD + params.getAttr("applyId"));
		return result;
	}
	
	/**
	 * updateSignInfo
	 * @param params
	 * @return
	 */
	public AppResult updateSignInfo(AppParam params) {
		AppResult result = new AppResult();
		AppParam queryParams = new AppParam();
		queryParams.addAttr("applyId", params.getAttr("applyId"));
		AppResult queryResult = this.query(queryParams);
		if(queryResult.getRows().size() > 0 && !StringUtils.isEmpty(queryResult.getRow(0))){
			//状态一致时不修改时间
			String status = StringUtil.getString(queryResult.getRow(0).get("status"));
			if(!StringUtils.isEmpty(params.getAttr("status")) && !status.equals(params.getAttr("status"))){
				result = this.update(params);
			}else{
				result = super.update(params, NAMESPACE);
				//删除缓存
				RedisUtils.getRedisService().del(StoreApplyUtils.STORE_APPLY_TREAT_SIGN_RECORD + params.getAttr("applyId"));
			}
		}
		return result;
	}
	
	/**
	 * updateSign
	 * @param params
	 * @return
	 */
	public AppResult updateSign(AppParam params) {
		int size = getDao().update(NAMESPACE, "updateSign",params.getAttr(),params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.DAO_Update_SIZE, size);
		//删除缓存
		RedisUtils.getRedisService().del(StoreApplyUtils.STORE_APPLY_TREAT_SIGN_RECORD + params.getAttr("applyId"));
		return result;
	}
	
	/**
	 * deleteSign
	 * @param params
	 * @return
	 */
	public AppResult deleteSign(AppParam params) {
		int size = getDao().delete(NAMESPACE, "deleteSign",params.getAttr(),params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
		//删除缓存
		RedisUtils.getRedisService().del(StoreApplyUtils.STORE_APPLY_TREAT_SIGN_RECORD + params.getAttr("applyId"));
		return result;
	}
	
	/**处理签单状态
	 * handleTreat
	 * @param params
	 * @return
	 */
	public AppResult handleTreat(AppParam params) {
		return StoreOptUtil.signDeal(params);
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
				param.addAttr("applyId", id);
				
				result = super.delete(param, NAMESPACE);
			}
		} else if (!StringUtils.isEmpty(params.getAttr("applyId"))) {
			result = super.delete(params, NAMESPACE);
			//删除缓存
			RedisUtils.getRedisService().del(StoreApplyUtils.STORE_APPLY_TREAT_SIGN_RECORD + params.getAttr("applyId"));
		} else {
			throw new AppException(DuoduoError.DELETE_NO_ID);
		}
		return result;
	}
	
	/**
	 * 查询最新的放款信息
	 * @param params
	 * @return
	 */
	public AppResult queryNewTreat(AppParam params) {
		return super.query(params, NAMESPACE, "queryNewTreat");
	}
}
