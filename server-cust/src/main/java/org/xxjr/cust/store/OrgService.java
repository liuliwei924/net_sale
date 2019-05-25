package org.xxjr.cust.store;

import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.llw.common.core.service.BaseService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Lazy
@Service
public class OrgService extends BaseService {


	private static final String NAMESPACE = "ORG";
	
	/**
	 * 查询所有门店
	 * @param params
	 * @return
	 */
	public AppResult queryOrgList(AppParam params) {
		return super.query(params, NAMESPACE,"queryOrgList");
	}
	
	/**
	 * 分页所有门店
	 * @param params
	 * @return
	 */
	public AppResult queryOrgPage(AppParam params) {
		return super.queryByPage(params, NAMESPACE,"queryOrgList","queryOrgCount");
	}
	


	/**
	 * 查询用户管理门店
	 * @param params
	 * @return
	 */
	public AppResult queryUserOrgs(AppParam params) {
		return super.query(params, NAMESPACE,"queryUserOrgs");
	}




}
