package org.xxjr.cust.identify;

import java.util.Date;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.ddq.common.exception.AppException;
import org.springframework.context.annotation.Lazy;
import org.ddq.common.web.session.DuoduoSession;
import org.llw.common.core.service.BaseService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.xxjr.sys.util.DBConst;
import org.ddq.common.exception.DuoduoError;

@Lazy
@Service
public class CustMobileService extends BaseService {
	private static final String NAMESPACE = "CUSTMOBILE";

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
	 * queryView
	 * @param params
	 * @return
	 */
	public AppResult queryView(AppParam params) {
		return super.queryByPage(params, NAMESPACE, "queryView", "queryViewCount");
	}
	
	/**
	 * queryViewCount
	 * @param params
	 * @return
	 */
	public AppResult queryViewCount(AppParam params) {
		int size = getDao().count(NAMESPACE, "queryViewCount",params.getAttr(),params.getDataBase());
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
				param.setDataBase(DBConst.Key_cust_DB);
				param.addAttr("changeId", id);
				
				result = super.delete(param, NAMESPACE);
			}
		} else if (!StringUtils.isEmpty(params.getAttr("changeId"))) {
			result = super.delete(params, NAMESPACE);
		} else {
			throw new AppException(DuoduoError.DELETE_NO_ID);
		}
		return result;
	}
	
	/***
	 * 审核
	 * @param context
	 * @return
	 */
	public AppResult audit(AppParam context){
		String changeId = context.getAttr("changeId").toString();
		String status =  context.getAttr("status").toString();
		String auditDesc = context.getAttr("auditDesc").toString();
		if( status.equals("1") || status.equals("2")){
			AppParam param = new AppParam();
			param.addAttr("changeId", changeId);
			param.addAttr("status", status);
			param.addAttr("auditDesc", auditDesc);
			param.addAttr("auditTime", new Date());
			param.addAttr("auditBy", DuoduoSession.getUserName());
			return super.update(param, NAMESPACE);
		}
		throw new AppException(DuoduoError.UPDATE_NO_PARAMS);
	}
}
