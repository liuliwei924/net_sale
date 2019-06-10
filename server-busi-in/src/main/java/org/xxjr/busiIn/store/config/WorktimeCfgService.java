package org.xxjr.busiIn.store.config;

import java.util.Date;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.service.SoaManager;
import org.ddq.common.exception.AppException;
import org.ddq.common.exception.DuoduoError;
import org.springframework.context.annotation.Lazy;
import org.ddq.common.web.session.DuoduoSession;
import org.llw.common.core.service.BaseService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.xxjr.sys.util.DBConst;
import org.xxjr.sys.util.NumberUtil;


@Lazy
@Service
public class WorktimeCfgService extends BaseService {
	private static final String NAMESPACE = "WORKTIMECFG";

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
	 * querys
	 * @param params
	 * @return
	 */
	public AppResult queryWorkTime(AppParam params) {
		return super.queryByPage(params, NAMESPACE, "queryWorkTime", "queryWorkTimeCount");
	}
	
	
	/**
	 * 添加工作时间配置
	 * @param params
	 * @return
	 */
	public AppResult insertWorkTime(AppParam params){
		//查询是否存在配置
		AppParam queryParam = new AppParam();
		queryParam.addAttr("orgId", params.getAttr("orgId"));
		AppResult result = this.query(queryParam);
		if(result.isSuccess() && result.getRows().size() >0){
			params.addAttr("recordId", result.getRow(0).get("recordId"));
			return super.update(params,NAMESPACE);
		}else{
			return this.insert(params);
		}
	}
	
	/**扣减余额
	 * subBalanceAmt
	 * @param params
	 * @return
	 */
	public AppResult subBalanceAmt(AppParam params) {
		int size = getDao().update(NAMESPACE, "subBalanceAmt", params.getAttr(), params.getDataBase());
		AppResult backContext = new AppResult();
		backContext.putAttr(DuoduoConstant.DAO_Update_SIZE, size);
		return backContext;
	}
	
	/**增加余额
	 * subBalanceAmt
	 * @param params
	 * @return
	 */
	public AppResult addBalanceAmt(AppParam params) {
		int size = getDao().update(NAMESPACE, "addBalanceAmt", params.getAttr(), params.getDataBase());
		AppResult backContext = new AppResult();
		backContext.putAttr(DuoduoConstant.DAO_Update_SIZE, size);
		return backContext;
	}
	
	/**门店充值
	 * subBalanceAmt
	 * @param params
	 * @return
	 */
	public AppResult orgCharge(AppParam params) {
		AppParam updateBaParam = new AppParam();
		updateBaParam.setDataBase(DBConst.Key_busi_in_DB);
		updateBaParam.addAttr("orgId", params.getAttr("orgId"));
		updateBaParam.addAttr("addBalanceAmt", params.getAttr("amount"));
		AppResult result = this.addBalanceAmt(updateBaParam);
		
		int updateSize = NumberUtil.getInt(result.getAttr(DuoduoConstant.DAO_Update_SIZE), 0);
		
		if(updateSize > 0) {
			params.setService("orgRechargeRecordService");
			params.setMethod("insert");
			SoaManager.getInstance().invoke(params);
		}
		return result;
	}
}
