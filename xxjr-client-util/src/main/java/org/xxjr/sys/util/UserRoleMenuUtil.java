package org.xxjr.sys.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.service.RemoteInvoke;
import org.ddq.common.web.session.DuoduoSession;
import org.llw.model.cache.RedisUtils;
import org.springframework.util.StringUtils;


public class UserRoleMenuUtil {

	public final static String Key_cache = "UserRole_";
	
	
	/**
	 * 判断URL权限是否存在
	 * @param context
	 * @return
	 */
	public static boolean validateURL(AppParam params){
		if(StringUtils.isEmpty(params.getService())||StringUtils.isEmpty(params.getMethod())){
			return false;
		}
		Object userId = DuoduoSession.getUser().getSessionData().get("userId");
		List<String> urls = queryUserRightUrls(userId);
		if(urls.contains(params.getService()+"/"+params.getMethod())){
			return true;
		}
		return false;
	}
	
	/***
	 * 获取用户 菜单和权限
	 * @param userId 用户ID
	 * @param subCode 子系统
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<Map<String,Object>> getUserMenus(Object userId,String subCode) {
		List<Map<String,Object>>  list = 
				(List<Map<String, Object>>) RedisUtils.getRedisService().get(Key_cache+userId + subCode);
		if(list !=null && list.size()>0){
			return list;
		}
		AppParam queryParams = new AppParam();
		queryParams.setService("userMenuService");
		queryParams.setMethod("queryUserMenus");
		queryParams.addAttr("userId", userId);
		queryParams.addAttr("subCode", subCode);
		AppResult paramsResult = RemoteInvoke.getInstance().call(queryParams);
		List<Map<String,Object>>  menus = new ArrayList<Map<String,Object>>();
		Map<String,Map<String,Object>> menuCodes = new HashMap<String,Map<String,Object> >();
		for(Map<String,Object> map: paramsResult.getRows()){
			if( map.get("parentId")==null){
				Map<String,Object> menuCode = new HashMap<String,Object>();
				menuCode.put("menuCode", map.get("menuCode"));
				menuCode.put("menuName", map.get("menuName"));
				menuCode.put("menuUrl", map.get("menuUrl"));
				menus.add(menuCode);
				menuCodes.put(map.get("menuId").toString(), menuCode);
			}else if(menuCodes.get(map.get("parentId").toString())!=null){
				List<Map<String,Object>> subMenus =  new ArrayList<Map<String,Object>>();
				Map<String,Object> parentMenu = menuCodes.get(map.get("parentId").toString());
				if(parentMenu.get("subMenus")!=null){
					subMenus = ((List<Map<String, Object>>) parentMenu.get("subMenus"));
				}else{
					parentMenu.put("subMenus", subMenus);
				}
				Map<String,Object> subMenu = new HashMap<String,Object>();
				subMenu.put("menuCode", map.get("menuCode"));
				subMenu.put("menuName", map.get("menuName"));
				subMenu.put("menuUrl", map.get("menuUrl"));
				subMenus.add(subMenu);
			}
		}
		
		RedisUtils.getRedisService().set(Key_cache+userId + subCode, (Serializable) menus, 60 * 60 * 1);
		return menus;
	}
	
	
	/***
	 * 获取用户URL权限
	 * @param userId 用户ID
	 * @return
	 */
	public static List<String> queryUserRightUrls(Object userId) {
		AppParam queryParams = new AppParam();
		queryParams.setService("userRightService");
		queryParams.setMethod("queryUserRightUrls");
		queryParams.addAttr("userId", userId);
		AppResult result = RemoteInvoke.getInstance().call(queryParams);
		List<String> roleRights =  new ArrayList<String> ();
		if (result.getRows().size()>0 && !StringUtils.isEmpty(result.getRow(0).get("rights"))) {
			roleRights = Arrays.asList(result.getRow(0).get("rights").toString().split(","));
		}
		return roleRights;
	}
	
	
	/***
	 * 获取用户 服务权限
	 * @param userId 用户ID
	 * @param subCode 子系统
	 * @return
	 */
	public static List<String> queryUserRights(Object userId,String subCode) {
		AppParam queryParams = new AppParam();
		queryParams.setService("userRightService");
		queryParams.setMethod("queryUserRights");
		queryParams.addAttr("userId", userId);
		queryParams.addAttr("subCode",subCode);
		AppResult result = RemoteInvoke.getInstance().call(queryParams);
		List<String> roleRights =  new ArrayList<String> ();
		if (result.getRows().size()>0 && !StringUtils.isEmpty(result.getRow(0).get("rights"))) {
			roleRights = Arrays.asList(result.getRow(0).get("rights").toString().split(","));
		}
		return roleRights;
	}
	/***
	 * 获取用户 的子系统列表
	 * @param userId 用户ID
	 * @return
	 */
	public static List<Map<String,Object>> getUserSubCodes(Object userId) {
		AppParam queryParams = new AppParam();
		queryParams.setService("userMenuService");
		queryParams.setMethod("queryUserSubCode");
		queryParams.addAttr("userId", userId);
		AppResult paramsResult = RemoteInvoke.getInstance().call(queryParams);
		return paramsResult.getRows();
	}
	
}
