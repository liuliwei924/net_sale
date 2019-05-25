package org.xxjr.busi.util.store;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.service.RemoteInvoke;
import org.llw.model.cache.RedisUtils;
import org.springframework.util.StringUtils;
import org.xxjr.sys.util.ServiceKey;

/***
 * 菜单工具类
 * @author sty
 *
 */
public class StoreMenuUtils {
	/** 跟进平台菜单树缓存key  **/
	public static final String STORE_TREE_MENUS_KEY="store_menusTreeKey_";
	public static final String STORE_PRODUCT_MENUS_KEY="store_productTreeKey_";
	/**
	 * 获取所有菜单
	 * @return
	 */
	public static AppResult getAllMenusTree(){

		AppParam params  = new AppParam();
		params.setService("sysStoreMenuService");
		params.setMethod("queryAllMenusTree");
		params.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_cust));
		AppResult menuResult = RemoteInvoke.getInstance().callNoTx(params);

		return menuResult;
	}	
	
	/**
	 * 获取角色所有菜单
	 * @return
	 */
	public static List<Map<String,Object>> getAllMenusByRole(int roleId){

		AppParam params = new AppParam();
		params.setService("sysStoreMenuService");
		params.setMethod("queryAllMenusByRole");
		params.addAttr("roleId", roleId);
		params.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_cust));
		AppResult menuResult = RemoteInvoke.getInstance().callNoTx(params);

		List<Map<String,Object>> menuList=null;
 		List<Map<String,Object>> mainMenu =  new ArrayList<Map<String,Object>>();
		if (menuResult.getRows().size()>0) {
			menuList = menuResult.getRows();
			//转换成菜单树
			for(Map<String,Object> map:menuList){
				if(StringUtils.isEmpty(map.get("parentId"))){
					mainMenu.add(map);
				}
			}
			for(Map<String,Object> map:mainMenu){
				List<Map<String,Object>> subList = getChild(map.get("menuId").toString(), menuList);
				if(subList != null){
					map.put("subMenus",subList);
				}
			}
		}
	
		return mainMenu;
	}

	/**
	 * 递归查找子菜单
	 * 
	 * @param id
	 *            当前菜单id
	 * @param rootMenu
	 *            要查找的列表
	 * @return
	 */
	private static List<Map<String,Object>> getChild(String id, List<Map<String,Object>> rootMenu) {
		// 子菜单
		List<Map<String,Object>> childList = new ArrayList<>();
		for (Map<String,Object> map : rootMenu) {
			// 遍历所有节点，将父菜单id与传过来的id比较
			if (!StringUtils.isEmpty(map.get("parentId"))) {
				if (map.get("parentId").toString().equals(id)) {
					childList.add(map);
				}
			}
		}
		// 把子菜单的子菜单再循环一遍
		for (Map<String,Object> menu : childList) {
			// 递归
			List<Map<String,Object>> subList = getChild(menu.get("menuId").toString(), rootMenu);
			if(subList != null){
				menu.put("subMenus",subList);
			}
		} // 递归退出条件
		if (childList.size() == 0) {
			return null;
		}
		return childList;
	}

	/**
	 * 获取产品目录树
	 * 
	 * @return
	 */
	public static List<Map<String, Object>> getProductMenusTree() {
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> menus = (List<Map<String, Object>>) RedisUtils.getRedisService().get(STORE_PRODUCT_MENUS_KEY);
		
		if (menus == null || menus.size() == 0) {
			menus = refreshProductMenusTree();
		}
	
		return menus;
	}
	
	/**
	 * 刷新产品菜单
	 * 
	 * @return
	 */
	public static List<Map<String, Object>> refreshProductMenusTree() {
		AppParam params = new AppParam();
		params.setService("productTypeService");
		params.setMethod("queryProTypeMenus");
		params.setOrderBy("level");
		params.setOrderValue("asc");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START
						+ ServiceKey.Key_busi_in));
		AppResult menuResult = RemoteInvoke.getInstance().callNoTx(params);

		List<Map<String, Object>> proTypeList = null;
		List<Map<String, Object>> mainMenu = new ArrayList<Map<String, Object>>();
		if (menuResult.getRows().size() > 0) {
			proTypeList = menuResult.getRows();
			// 转换成菜单树
			for (Map<String, Object> map : proTypeList) {
				if (StringUtils.isEmpty(map.get("parentId"))) {
					mainMenu.add(map);
				}
			}
			for (Map<String, Object> map : mainMenu) {
				List<Map<String, Object>> subList = getProductChild(map.get("typeId")
						.toString(), proTypeList);
				if (subList != null) {
					map.put("children", subList);
				}
			}	
		}
		RedisUtils.getRedisService().set(STORE_PRODUCT_MENUS_KEY ,(Serializable) mainMenu, 60 * 30 * 12);
		return mainMenu;
	}
	
	/**
	 * 递归查找子菜单
	 * 
	 * @param id
	 *            当前菜单id
	 * @param rootMenu
	 *            要查找的列表
	 * @return
	 */
	private static List<Map<String,Object>> getProductChild(String id, List<Map<String,Object>> rootMenu) {
		// 子菜单
		List<Map<String,Object>> childList = new ArrayList<>();
		for (Map<String,Object> map : rootMenu) {
			// 遍历所有节点，将父菜单id与传过来的id比较
			if (!StringUtils.isEmpty(map.get("parentId"))) {
				if (map.get("parentId").toString().equals(id)) {
					childList.add(map);
				}
			}
		}
		// 把子菜单的子菜单再循环一遍
		for (Map<String,Object> menu : childList) {
			// 递归
			List<Map<String,Object>> subList = getProductChild(menu.get("typeId").toString(), rootMenu);
			if(subList != null){
				menu.put("children",subList);
			}
		} // 递归退出条件
		if (childList.size() == 0) {
			return null;
		}
		return childList;
	}

}
