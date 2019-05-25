package org.xxjr.busiIn.store.treat;

import java.util.Date;
import java.util.Map;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.ddq.common.exception.AppException;
import org.ddq.common.exception.DuoduoError;
import org.ddq.common.util.StringUtil;
import org.ddq.common.web.session.DuoduoSession;
import org.llw.common.core.service.BaseService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.xxjr.busiIn.utils.StoreOptUtil;

@Lazy
@Service
public class TreatSuccessHistoryService extends BaseService {
	private static final String NAMESPACE = "TREATSUCCESSHISTORY";

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
		params.addAttr("updateTime",  new Date());
		params.addAttr("createBy", DuoduoSession.getUserName());
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
	 * 更新或者插入
	 * @param params
	 * @return
	 */
	public AppResult updateOrInsert(AppParam params) {
		AppResult result = new AppResult();
		int updateSize = getDao().update(NAMESPACE, "updateBackInfo",params.getAttr(),params.getDataBase());
		if(updateSize == 0){
			//更新回款数据到回款历史表
			Map<String, Object> applyInfo = StoreOptUtil.queryByApplyId(params.getAttr("applyId"));
			//申请时间
			String applyTime = StringUtil.getString(applyInfo.get("applyTime"));
			//渠道代号
			String channelCode = StringUtil.getString(applyInfo.get("channelCode"));
			//来自详细渠道
			String channelDetail = StringUtil.getString(applyInfo.get("channelDetail"));
			params.addAttr("applyTime", applyTime);
			params.addAttr("channelCode", channelCode);
			params.addAttr("channelDetail", channelDetail);
			result = this.insert(params);
		}else{
			result.putAttr(DuoduoConstant.DAO_Update_SIZE, updateSize);
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
		return result;
	}
	
	/**
	 * 删除回款
	 * @param params
	 * @return
	 */
	public AppResult deleteByRepayId(AppParam params) {
		AppResult  result = new AppResult();
		int deleteSize = getDao().delete(NAMESPACE, "deleteByRepayId",params.getAttr(),params.getDataBase());
		result.putAttr(DuoduoConstant.DAO_Delete_SIZE, deleteSize);
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
		return result;
	}
}
