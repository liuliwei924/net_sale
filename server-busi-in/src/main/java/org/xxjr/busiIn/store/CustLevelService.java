package org.xxjr.busiIn.store;

import java.util.Date;

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

@Lazy
@Service
public class CustLevelService extends BaseService {
	private static final String NAMESPACE = "CUSTLEVEL";

	/**
	 * querys
	 * @param params
	 * @return
	 */
	public AppResult query(AppParam params) {
		return super.query(params, NAMESPACE);
	}
	
	/**
	 * queryLess8Cust
	 * @param params
	 * @return
	 */
	public AppResult queryLess8Cust(AppParam params) {
		return super.query(params, NAMESPACE, "queryLess8Cust");
	}
	
	/**
	 * queryMore8Cust
	 * @param params
	 * @return
	 */
	public AppResult queryMore8Cust(AppParam params) {
		return super.query(params, NAMESPACE, "queryMore8Cust");
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
		AppResult updateResult = new AppResult();
		params.addAttr("updateTime", new Date());
		updateResult = super.update(params, NAMESPACE);
		int count = (Integer) updateResult.getAttr(DuoduoConstant.DAO_Update_SIZE);
		if (count <= 0) {
			updateResult = this.insert(params);
		}
		return updateResult;
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
				param.addAttr("customerId", id);
				
				result = super.delete(param, NAMESPACE);
			}
		} else if (!StringUtils.isEmpty(params.getAttr("customerId"))) {
			result = super.delete(params, NAMESPACE);
		} else {
			throw new AppException(DuoduoError.DELETE_NO_ID);
		}
		return result;
	}
	
	
	/**
	 * 查询未满足基本单人员
	 * @param params
	 * @return
	 */
	public AppResult queryLessBaseCust(AppParam params) {
		return super.query(params, NAMESPACE, "queryLessBaseCust");
	}
	
	/**
	 * 查询已满足基本单人员
	 * @param params
	 * @return
	 */
	public AppResult queryMoreBaseCust(AppParam params) {
		return super.query(params, NAMESPACE, "queryMoreBaseCust");
	}
	
	/***
	 * 查询分单信息列表
	 * @param params
	 * @return
	 */
	public AppResult queryAllotOrderList(AppParam params){
		return super.queryByPage(params,NAMESPACE,"queryAllotOrder","queryAllotOrderCount");
	}
	
	
	/**
	 * 查询关闭分单原因
	 * @param params
	 * @return
	 */
	public AppResult queryCloseAllotDesc(AppParam params) {
		return super.query(params, NAMESPACE, "queryCloseAllotDesc");
	}
	
	/**
	 * 查询客户等级以及登录状态
	 * @param params
	 * @return
	 */
	public AppResult queryLoginStatus(AppParam params) {
		return super.query(params, NAMESPACE, "queryLoginStatus");
	}
	
	/**
	 * 更新暂停分单时间
	 * @param params
	 * @return
	 */
	public AppResult updateAllortTime(AppParam params) {
		AppResult result = new AppResult();
		params.addAttr("updateTime", new Date());
		result = super.update(params, NAMESPACE);
		return result;
	}
	
	/**
	 * 查询满足条件新订单立即分配的人
	 * @param params
	 * @return
	 */
	public AppResult queryNewOrderAllot(AppParam params) {
		return super.query(params, NAMESPACE, "queryNewOrderAllot");
	}
	
	/**
	 * 查询未满足基本分单立即分配的人
	 * @param params
	 * @return
	 */
	public AppResult queryLessOrderAllot(AppParam params) {
		return super.query(params, NAMESPACE, "queryLessOrderAllot");
	}
	
	/**
	 * 查询已满足基本分单立即分配的人
	 * @param params
	 * @return
	 */
	public AppResult queryMoreOrderAllot(AppParam params) {
		return super.query(params, NAMESPACE, "queryMoreOrderAllot");
	}
	
}
