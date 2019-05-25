package org.xxjr.busiIn.store;

import java.util.Date;
import java.util.Map;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.service.SoaManager;
import org.ddq.common.exception.AppException;
import org.ddq.common.exception.DuoduoError;
import org.ddq.common.util.StringUtil;
import org.llw.common.core.service.BaseService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.xxjr.sys.util.NumberUtil;

@Lazy
@Service
public class BorrowApplyPushService extends BaseService {
	private static final String NAMESPACE = "BORROWAPPLYPUSH";

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
		params.addAttr("updateTime", new Date());
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
				param.addAttr("applyId", id);
				
				result = super.delete(param, NAMESPACE);
			}
		} else if (!StringUtils.isEmpty(params.getAttr("applyId"))) {
			result = super.delete(params, NAMESPACE);
		} else {
			throw new AppException(DuoduoError.DELETE_NO_ID);
		}
		return result;
	}
	
	/***
	 * 查询推送列表
	 * @param params
	 * @return
	 */
	public AppResult queryPushList(AppParam params){
		return this.query(params, NAMESPACE, "queryPushList");
	}
	
	/***
	 * 查询推送数据，通用
	 * @param params
	 * @return
	 */
	public AppResult queryPushData(AppParam params){
		AppResult result = new AppResult();
		if (StringUtils.isEmpty(params.getAttr("size"))) {
			result.setMessage("请传入查询行数");
			result.setSuccess(false);
			return result;
		}
		if (StringUtils.isEmpty(params.getAttr("pushType"))) {
			result.setMessage("请传入推送平台");
			result.setSuccess(false);
			return result;
		}
		return this.query(params, NAMESPACE, "queryPushData");
	}
	
	 
	/***
	 * 查询推送列表All
	 * @param params
	 * @return
	 */
	public AppResult queryPushListAll(AppParam params){
		return this.queryByPage(params, NAMESPACE, "queryPushListAll","queryPushListAllCount");
	}
	
	public AppResult sumPushData (AppParam params) {
		return this.query(params, NAMESPACE, "sumPushData");
	}
	
	public AppResult sumChannelPushData (AppParam params) {
		return this.query(params, NAMESPACE, "sumChannelPushData");
	}
	
	public AppResult sumDistinctData (AppParam params) {
		return this.query(params, NAMESPACE, "sumDistinctData");
	}
	
	/***
	 * 处理推送结果
	 * @param params
	 * @return
	 */
	public AppResult updatePushResult(AppParam params){
		AppResult result = new AppResult();
		String pushId = StringUtil.getString(params.getAttr("pushId"));
		int code = NumberUtil.getInt(params.getAttr("code"),-1);
		
		AppParam updateParam = new AppParam("borrowApplyPushService","update");
		updateParam.addAttr("pushId", pushId);
		updateParam.addAttr("message", params.getAttr("message"));
		updateParam.addAttr("status", 1);//推送成功
		if(0 != code){
			updateParam.addAttr("status", 2);//推送失败
		}
		SoaManager.getInstance().invoke(updateParam);
		return result;
	}
	
	
	public AppResult updateStatus (AppParam params) {
		AppResult result = new AppResult();
		int size = this.getDao().update(NAMESPACE, "updateStatus", params.getAttr(), params.getDataBase());
		result.putAttr(DuoduoConstant.DAO_Update_SIZE, size);
		return result;
	}
	
	/**
	 * 查询某个渠道推送数量
	 * @param params
	 * @return
	 */
	public AppResult queryPushCount(AppParam params) {
		AppResult result = new AppResult();
		int sucCount = 0;
		int totalCount = 0;
		AppResult queryResult = super.query(params, NAMESPACE, "queryPushCount");
		if (queryResult.getRows().size() > 0) {
			Map<String, Object> row = queryResult.getRow(0);
			sucCount = NumberUtil.getInt(row.get("sucCount"), 0);
			totalCount = NumberUtil.getInt(row.get("totalCount"), 0);
		}
		result.putAttr("sucCount", sucCount);
		result.putAttr("totalCount", totalCount);
		return result;
	}
	
	/**
	 * 查询某个第三方接口推送渠道数据的比例
	 * @param params
	 * @return
	 */
	public AppResult queryChannelProportion(AppParam params) {
		AppResult result = new AppResult();
		int apiCount = 0;
		int otherCount = 0;
		AppResult queryResult = super.query(params, NAMESPACE, "queryChannelProportion");
		if (queryResult.getRows().size() > 0) {
			Map<String, Object> row = queryResult.getRow(0);
			apiCount = NumberUtil.getInt(row.get("apiCount"), 0);
			otherCount = NumberUtil.getInt(row.get("otherCount"), 0);
		}
		result.putAttr("apiCount", apiCount);
		result.putAttr("otherCount", otherCount);
		return result;
	}
}
