package org.xxjr.busiIn.store.ability;

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
import org.xxjr.busi.util.AbilityCfgUtil;

@Lazy
@Service
public class AbilityGradeCfgService extends BaseService {
	private static final String NAMESPACE = "ABILITYGRADECFG";

	/**
	 * 查寻数据
	 * @param params
	 * @return
	 */
	public AppResult query(AppParam params) {
		return super.query(params, NAMESPACE);
	}
	
	/**
	 * 分页查寻数据
	 * @param params
	 * @return
	 */
	public AppResult queryByPage(AppParam params) {
		return super.queryByPage(params, NAMESPACE);
	}
	
	/**
	 * 查寻分页统计数据
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
	 * 添加数据处理
	 * @param params
	 * @return
	 */
	public AppResult insert(AppParam params) {
		params.addAttr("createTime", new Date());
		params.addAttr("createBy", DuoduoSession.getUserName());
		return super.insert(params, NAMESPACE);
	}
	
	/**
	 * 修改数据处理
	 * @param params
	 * @return
	 */
	public AppResult update(AppParam params) {
		AppResult result = new AppResult();
		params.addAttr("updateTime", new Date());
		params.addAttr("serviceName", DuoduoSession.getUserName());
		result = super.update(params, NAMESPACE);
		if (result.isSuccess()) {
			AbilityCfgUtil.refreshAbilityGradeCfg();
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
				param.addAttr("gradeCode", id);
				
				result = super.delete(param, NAMESPACE);
			}
		} else if (!StringUtils.isEmpty(params.getAttr("gradeCode"))) {
			result = super.delete(params, NAMESPACE);
		} else {
			throw new AppException(DuoduoError.DELETE_NO_ID);
		}
		AbilityCfgUtil.refreshAbilityGradeCfg();
		return result;
	}
	
	/**
	 * 保存能力值等级配置
	 * @param params
	 * @return
	 */
	public AppResult save(AppParam params){
		AppResult result = new AppResult();
		Object gradeCode = params.getAttr("gradeCode");
		Object gradeName = params.getAttr("gradeName");
		Object minScore = params.getAttr("minScore");
		Object maxScore = params.getAttr("maxScore");
		Object successCount = params.getAttr("successCount");
	
		if (StringUtils.isEmpty(gradeName) 
				|| StringUtils.isEmpty(minScore)
				|| StringUtils.isEmpty(maxScore)
				|| StringUtils.isEmpty(successCount)
				|| StringUtils.isEmpty(gradeCode)) {
			throw new AppException(DuoduoError.UPDATE_NO_PARAMS);
		}
		result = this.insert(params);
		if (result.isSuccess()) {
			AbilityCfgUtil.refreshAbilityGradeCfg();
		}
		result.putAttr("gradeCode", params.getAttr("gradeCode"));
		return result;
	}
	
}
