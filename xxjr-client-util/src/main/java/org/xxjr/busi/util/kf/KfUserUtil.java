package org.xxjr.busi.util.kf;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.service.RemoteInvoke;
import org.ddq.common.util.StringUtil;
import org.llw.model.cache.RedisUtils;
import org.springframework.util.StringUtils;
import org.xxjr.sys.util.ServiceKey;


/**
 * 客服管理 工具类
 * @author Administrator
 *
 */
public class KfUserUtil {

	/** 客服管理登录用户菜单缓存key  **/
	public static final String KF_USER_MENU = "kf_userMenu_";
	/** 客服管理登录用户权限缓存key  **/
	public static final String KF_USER_RIGHT = "kf_userRight_";
	
	/** 客服sign前缀  **/
	public static final String KF_SIGN_PRE = "kf";
	
	/** 客服sign 有效时间  **/
	public static final int  KF_SIGN_CACHE_TIME = 30*60;
	
	/**
	 * 获取customerId
	 * @param request
	 * @return
	 */
	public static String getCustomerId(HttpServletRequest request){
		String signId = request.getParameter("signId");
		return (String)RedisUtils.getRedisService().get(signId);
	}
	
	/**
	 * 获取用户菜单权限控制信息
	 *
	 */
	@SuppressWarnings("unchecked")
	public static List<Map<String,Object>> getTreeMenus(String customerId) {
		AppParam menuParam = new AppParam();
		menuParam.addAttr("customerId", customerId);
		menuParam.setService("custMenuService");
		menuParam.setMethod("queryTreeMenus");
		menuParam.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START
						+ ServiceKey.Key_cust));
		AppResult paramsResult = RemoteInvoke.getInstance().callNoTx(menuParam);
		List<Map<String,Object>>  menus = new ArrayList<Map<String,Object>>();
		Map<String,Map<String,Object>> menuCodes = new HashMap<String,Map<String,Object> >();
		for(Map<String,Object> map: paramsResult.getRows()){
			if( map.get("parentId")==null){
				Map<String,Object> menuCode = new HashMap<String,Object>();
				menuCode.put("menuId", map.get("menuId"));
				menuCode.put("menuName", map.get("menuName"));
				menuCode.put("checked", "1".equals(map.get("checked").toString()));
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
				subMenu.put("menuId", map.get("menuId"));
				subMenu.put("menuName", map.get("menuName"));
				subMenu.put("checked", "1".equals(map.get("checked").toString()));
				subMenus.add(subMenu);
			}
		}
		return menus;
	}
	
	/**
	 * 获取用户菜单权限控制信息
	 *
	 */
	@SuppressWarnings("unchecked")
	public static List<Map<String,Object>> getMenus() {
		AppParam menuParam = new AppParam();
		menuParam.setService("custMenuService");
		menuParam.setMethod("queryMenus");
		menuParam.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START
						+ ServiceKey.Key_cust));
		AppResult paramsResult = RemoteInvoke.getInstance().callNoTx(menuParam);
		List<Map<String,Object>>  menus = new ArrayList<Map<String,Object>>();
		Map<String,Map<String,Object>> menuCodes = new HashMap<String,Map<String,Object> >();
		for(Map<String,Object> map: paramsResult.getRows()){
			if( map.get("parentId")==null){
				Map<String,Object> menuCode = new HashMap<String,Object>();
				menuCode.put("menuId", map.get("menuId"));
				menuCode.put("menuName", map.get("menuName"));
				menuCode.put("checked", "1".equals(map.get("enable").toString()));
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
				subMenu.put("menuId", map.get("menuId"));
				subMenu.put("menuName", map.get("menuName"));
				subMenu.put("checked", "1".equals(map.get("enable").toString()));
				subMenus.add(subMenu);
			}
		}
		return menus;
	}
	
	
	/***
	 * 获取用户 菜单权限
	 * @param customerId 用户ID
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<Map<String,Object>> getUserMenus(String customerId) {
		List<Map<String,Object>>  list = 
				(List<Map<String, Object>>) RedisUtils.getRedisService().get(KF_USER_MENU+customerId);
		if(list !=null && list.size()>0){
			return list;
		}
		AppParam queryParams = new AppParam();
		queryParams.setService("custMenuService");
		queryParams.setMethod("queryUserMenus");
		queryParams.addAttr("customerId", customerId);
		queryParams.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START
						+ ServiceKey.Key_cust));
		AppResult paramsResult = RemoteInvoke.getInstance().callNoTx(queryParams);
		
		
		//List<Map<String,Object>>  menus = new ArrayList<Map<String,Object>>();
		LinkedHashMap<String,Map<String,Object>> menuCodes = new LinkedHashMap<String,Map<String,Object> >();
		
		List<Map<String, Object>> rows = paramsResult.getRows();
		
		for(Map<String,Object> map: rows){//循环二级
			if(StringUtils.isEmpty(map.get("parentId"))){
				Map<String,Object> menuCode = new HashMap<String,Object>();
				menuCode.put("subCode", map.get("subCode"));
				menuCode.put("menuCode", map.get("menuCode"));
				menuCode.put("menuName", map.get("menuName"));
				menuCode.put("menuUrl", map.get("menuUrl"));
				menuCodes.put(map.get("menuId").toString(), menuCode);
				//menus.add(menuCode);
			}
			
		}
		
		for (int i = 0; i < rows.size(); i++) {//循环三级集合并放入二级
			if(menuCodes.containsKey(StringUtil.getString(rows.get(i).get("parentId")))){
				Map<String,Object> parentMenu = menuCodes.get(StringUtil.getString(rows.get(i).get("parentId")));//获取二级菜单
				List<Map<String,Object>> subMenus = ((List<Map<String, Object>>) parentMenu.get("subMenus"));//二级菜单是否包含三级菜单集合
				if (subMenus == null) {//没有创建,有的话直接使用
					subMenus = new ArrayList<Map<String,Object>>();
				}
				Map<String,Object> subMenu = new HashMap<String,Object>();
				subMenu.put("subCode", rows.get(i).get("subCode"));
				subMenu.put("menuCode", rows.get(i).get("menuCode"));
				subMenu.put("menuName", rows.get(i).get("menuName"));
				subMenu.put("menuUrl", rows.get(i).get("menuUrl"));
				subMenus.add(subMenu);//将三级菜单放入三级菜单集合
				parentMenu.put("subMenus", subMenus);//放入二级
				//menus.add(parentMenu);//放入二级菜单集合
			}
		}
		
		List<Map<String,Object>>  menusAll = new ArrayList<Map<String,Object>>();
		Map<String,Object> menuMap = new HashMap<String, Object>();
		for(Entry<String, Map<String, Object>> map : menuCodes.entrySet()){//分类二级菜单,需要使用有序集合排序
			List<Map<String,Object>>  menuList = (List<Map<String, Object>>) menuMap.get(map.getValue().get("subCode").toString());//分类中是否存在二级菜单集合
			if (menuList == null) {//没有创建，有，直接使用
				menuList = new ArrayList<Map<String,Object>>();
			}
			menuList.add(map.getValue());//创建到二级菜单集合
			menuMap.put(map.getValue().get("subCode").toString(), menuList);//放入分类中
		}
		menusAll.add(menuMap);
		RedisUtils.getRedisService().set(KF_USER_MENU+customerId, (Serializable) menusAll, 60 * 60 * 1);
		return menusAll;
	}
	
	/***
	 * 获取用户权限
	 * @param customerId 用户ID
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Map<String,Object> getUserRight(String customerId) {
		Map<String,Object>  map = 
				 (Map<String, Object>) RedisUtils.getRedisService().get(KF_USER_RIGHT+customerId);
		if(map !=null && map.size()>0){
			return map;
		}
		Map<String, Object> right =  new HashMap<String,Object>();
		AppParam queryParams = new AppParam();
		queryParams.setService("custRightService");
		queryParams.setMethod("query");
		queryParams.addAttr("customerId", customerId);
		queryParams.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START
						+ ServiceKey.Key_cust));
		AppResult paramsResult = RemoteInvoke.getInstance().callNoTx(queryParams);
		if(paramsResult.getRows().size() > 0){
			right = paramsResult.getRow(0);
			RedisUtils.getRedisService().set(KF_USER_RIGHT+customerId, (Serializable) right, 60 * 60 * 1);
		}
		return right;
	}
}
