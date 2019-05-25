package org.xxjr.busiIn.kf.config;

import java.util.Date;
import java.util.List;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.service.SoaManager;
import org.ddq.common.exception.AppException;
import org.ddq.common.exception.DuoduoError;
import org.ddq.common.web.session.DuoduoSession;
import org.llw.common.core.service.BaseService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.xxjr.sys.util.NumberUtil;
@Lazy
@Service
public class InsurancePushPoolService extends BaseService {
	private static final String NAMESPACE = "INSURANCEPUSHPOOL";

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
		int allotFlag = NumberUtil.getInt(params.getAttr("allotFlag"), 0);
		if (allotFlag == 5 || allotFlag == 7) {
			params.addAttr("immediate", 1);
		}
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
	@SuppressWarnings("unchecked")
	public AppResult delete(AppParam params) {
		List<String> ids = (List<String>) params.getAttr("ids");
		AppResult  result = null;
		if (!StringUtils.isEmpty(ids)) {
			for (String id : ids) {
				AppParam param = new AppParam();
				param.addAttr("pushId", id);
				
				result = super.delete(param, NAMESPACE);
			}
		} else if (!StringUtils.isEmpty(params.getAttr("pushId"))) {
			result = super.delete(params, NAMESPACE);
		} else {
			throw new AppException(DuoduoError.DELETE_NO_ID);
		}
		return result;
	}
	
	/**
	 * 保存结果并修改单子为已推送
	 * @param param
	 * @return
	 */
	public AppResult save (AppParam param) {
		AppResult result = new AppResult();
		result = this.insert(param);
		//int count = NumberUtil.getInt(result.getAttr(DuoduoConstant.DAO_Insert_SIZE), 0);
		return result;
	}
	
	/**
	 * 查询待推送列表
	 * @param params
	 * @return
	 */
	public AppResult queryPushData(AppParam params) {
		AppResult result = new AppResult();
		if (StringUtils.isEmpty(params.getAttr("size"))) {
			result.setMessage("请传入查询行数");
			result.setSuccess(false);
			return result;
		}
		return this.query(params, NAMESPACE, "queryPushData");
	}
	
	/**
	 * 记录推送结果并从池中删除记录
	 * @param param
	 * @return
	 */
	public AppResult updateStatus (AppParam param) {
		AppResult result = new AppResult();
		int status = NumberUtil.getInt(param.getAttr("status"));
		//int pushType = NumberUtil.getInt(param.getAttr("pushType"));
		
		AppParam insertParam = new AppParam("borrowApplyPushService", "insert");//记录这次推送的情况
		insertParam.addAttrs(param.getAttr());
		insertParam.addAttr("updateTime", new Date());
		SoaManager.getInstance().invoke(insertParam);
		
		if (status == 1) {//保险成功才删除
			int sourceType = NumberUtil.getInt(param.getAttr("sourceType"), -1);
			if (sourceType > 0) {
				super.delete(param, PushdataPoolService.NAMESPACE);
			}else {
				this.delete(param);
			}
		}
		return result;
	}
	
	public AppResult failDataRestore (AppParam param) {
		AppResult result = new AppResult();
		int size = this.getDao().insert(NAMESPACE, "failDataRestore", param.getAttr(), param.getDataBase());
		result.putAttr(DuoduoConstant.DAO_Insert_SIZE, size);
		return result;
	}
}
