package org.xxjr.busiIn.kf.ext;


import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.llw.common.core.service.BaseService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.xxjr.busi.util.AllotCityTemplateUtil;

@Lazy
@Service
public class AllotCityTemplateService extends BaseService {
	private static final String NAMESPACE = "ALLOTCITYTEMPLATE";

	/**
	 * querys
	 * @param params
	 * @return
	 */
	public AppResult query(AppParam params) {
		return super.query(params, NAMESPACE);
	}
	/**
	 * query2allot
	 * @param params
	 * @return
	 */
	public AppResult query2allot(AppParam params) {
		return super.query(params, NAMESPACE,"query2allot");
	}
	/**
	 * queryCityType
	 * @param params
	 * @return
	 */
	public AppResult queryCityType(AppParam params) {
		return super.query(params, NAMESPACE,"queryCityType");
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
		AppResult result = super.insert(params, NAMESPACE);
		if (result.isSuccess()) {
			AllotCityTemplateUtil.refershApplyTemp();
		}
		return result;
	}
	
	/**
	 * update
	 * @param params
	 * @return
	 */
	public AppResult update(AppParam params) {
		AppResult result = super.update(params, NAMESPACE);
		if(result.isSuccess()){
			AllotCityTemplateUtil.refershApplyTemp();
		}
		return result;
	}
	/**
	 * delete
	 * @param params
	 * @return
	 */
	public AppResult delete(AppParam params) {
		AppResult result = new AppResult();
		result = super.delete(params, NAMESPACE);
		if(result.isSuccess()){
			AllotCityTemplateUtil.refershApplyTemp();
		}
		return result;
	}
}
