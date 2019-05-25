package org.xxjr.busi.util.store;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.service.RemoteInvoke;
import org.xxjr.sys.util.ServiceKey;

/***
 * 角色相关工具类
 * @author ZQH
 *
 */
public class StoreRoleUtils {
	/** 跟进平台角色缓存key  **/
	public static final String STORE_ROLE_KEY="store_roleKey_";
	
	/**
	 * 获取角色列表
	 * @return
	 */
	public static List<Map<String,Object>> getAllRoles(){
		AppParam params  = new AppParam();
		params.setService("sysRoleService");
		params.setMethod("query");
		params.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_cust));
		AppResult roleResult = RemoteInvoke.getInstance().callNoTx(params);
		
		List<Map<String,Object>> roleList =  new ArrayList<Map<String,Object>>();
		if(roleResult.getRows().size()>0){
			roleList =  (List<Map<String, Object>>) roleResult.getRows();
		}
		return roleList;
	}
	
}
