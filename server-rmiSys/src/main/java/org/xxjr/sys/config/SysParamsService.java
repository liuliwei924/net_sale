package org.xxjr.sys.config;

import java.util.Date;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.ddq.common.web.session.DuoduoSession;
import org.llw.common.core.service.BaseService;
import org.llw.model.cache.RedisUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.xxjr.sys.util.NumberUtil;
import org.xxjr.sys.util.SysParamsUtil;

@Lazy
@Service
public class SysParamsService extends BaseService {
	private static final String NAMESPACE = "SYSPARAMS";

	public AppResult query(AppParam context) {
		return super.query(context, NAMESPACE);
	}
	
	public AppResult queryByPage(AppParam context) {
		return super.queryByPage(context, NAMESPACE);
	}
	
	public AppResult queryCount(AppParam context) {
		int size = getDao().count(NAMESPACE, super.COUNT,context.getAttr(), context.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
		return result;
	}
	
	public AppResult insert(AppParam context) {
		context.addAttr("createTime", new Date());
		context.addAttr("updateBy", DuoduoSession.getUserName());
		AppResult result =  super.insert(context, NAMESPACE);
		
		SysParamsUtil.refreshValue(context.getAttr("paramCode").toString(),context.getAttr("paramValue").toString());
		return result;
	}
	public AppResult update(AppParam context) {
		context.addAttr("updateTime", new Date());
		context.addAttr("updateBy", DuoduoSession.getUserName());
		AppResult result =  super.update(context, NAMESPACE);
		SysParamsUtil.refreshValue(context.getAttr("paramCode").toString(),context.getAttr("paramValue").toString());
		return result;
	}
	public AppResult delete(AppParam params) {
		String paramCode = (String) params.getAttr("paramCode");
		AppResult result = super.delete(params, NAMESPACE);
		int deleteSize = NumberUtil.getInt(result.getAttr(DuoduoConstant.DAO_Delete_SIZE),0);
		if (deleteSize > 0) {
			RedisUtils.getRedisService().del(paramCode);
		}
		return result;
	}
}
