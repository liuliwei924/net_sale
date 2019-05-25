package org.xxjr.busiIn.store.record;

import java.util.Date;

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
public class BorrowRecordHistoryService extends BaseService {
	private static final String NAMESPACE = "BORROWRECORDHISTORY";

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
	 * 插入跟进记录
	 * @param params
	 * @return
	 */
	public AppResult insertByRecord(AppParam params) {
		params.addAttr("updateTime", new Date());
		return super.insert(params, NAMESPACE);
	}
	
	/**
	 * 插入跟进记录
	 * @param params
	 * @return
	 */
	public AppResult insertBySelect(AppParam params) {
		AppResult result = new AppResult();
		String curDate = StringUtil.getString(params.getAttr("curDate"));
		String tableName = StoreOptUtil.getTableName(curDate);
		params.addAttr("tableName", tableName);
		int size = super.getDao().insert(NAMESPACE, "insertBySelect", params.getAttr(), params.getDataBase());
		result.putAttr(DuoduoConstant.DAO_Insert_SIZE, size);
		return result;
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
	
	/**
	 * 查询插入
	 * @param params
	 * @return
	 */
	public AppResult insertByBorrowStoreRecord(AppParam params) {
		AppResult result = new AppResult();
		int size = super.getDao().insert(NAMESPACE, "insertByBorrowApply", params.getAttr(), params.getDataBase());
		result.putAttr(DuoduoConstant.DAO_Insert_SIZE, size);
		return result;
	}
}
