package org.xxjr.busi.util.kefu;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.service.RemoteInvoke;
import org.ddq.common.util.StringUtil;
import org.llw.model.cache.RedisUtils;
import org.springframework.util.StringUtils;
import org.xxjr.cust.util.info.CustomerIdentify;
import org.xxjr.sys.util.ServiceKey;

public class KefuUserUtil {
	
	/** 客服平台登录用户菜单树缓存key **/
	public static final String KEFU_USER_TREE_MENUS_KEY = "kefu_userMenusTreeKey_";
	/** 客服平台登录用户审核权限缓存key **/
	public static final String KEFU_USER_CHECK_MENUS_KEY = "kefu_userCheckMenusKey_";
	/** 客服平台登录用户审核权限code缓存key **/
	public static final String KEFU_USER_CHECK_MENUS_CODE_KEY = "kefu_userCheckMenusKey_code_";
	/** 客服平台登录用户基本菜单缓存key **/
	public static final String KEFU_USER_BASE_MENUS_KEY = "kefu_userBaseMenusKey_";
	/** 跟进平台登录用户修改权限缓存key **/
	public static final String KEFU_USER_MODIFY_MENUS_KEY = "kefu_userModifyMenusKey_";
	/**
	 * 获取用户菜单树
	 * 
	 * @return
	 */
	public static List<Map<String, Object>> getUserMenusTree(String roleId) {
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> menus = (List<Map<String, Object>>) RedisUtils.getRedisService().get(KEFU_USER_TREE_MENUS_KEY + roleId);
		
		if (menus == null || menus.size() == 0) {
			menus = refreshUserMenusTree(roleId);
		}
	
		return menus;
	}
	
	/**
	 * 刷新用户主菜单权限
	 * 
	 * @return
	 */
	public static List<Map<String, Object>> refreshUserMenusTree(String roleId) {

		AppParam params = new AppParam();
		params.setService("sysKfMenuService");
		params.setMethod("queryUserMenusTree");
		params.addAttr("roleId", roleId);
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START
						+ ServiceKey.Key_cust));
		AppResult menuResult = RemoteInvoke.getInstance().callNoTx(params);

		List<Map<String, Object>> menuList = null;
		List<Map<String, Object>> mainMenu = new ArrayList<Map<String, Object>>();
		if (menuResult.getRows().size() > 0) {
			menuList = menuResult.getRows();
			// 转换成菜单树
			for (Map<String, Object> map : menuList) {
				if (StringUtils.isEmpty(map.get("parentId"))) {
					mainMenu.add(map);
				}
			}
			for (Map<String, Object> map : mainMenu) {
				List<Map<String, Object>> subList = getChild(map.get("menuId")
						.toString(), menuList);
				if (subList != null) {
					map.put("subMenus", subList);
				}
			}
			
			RedisUtils.getRedisService().set(KEFU_USER_TREE_MENUS_KEY + roleId,(Serializable) mainMenu, 60 * 30 * 12);
			
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
	private static List<Map<String, Object>> getChild(String id,
			List<Map<String, Object>> rootMenu) {
		// 子菜单
		List<Map<String, Object>> childList = new ArrayList<>();
		for (Map<String, Object> map : rootMenu) {
			// 遍历所有节点，将父菜单id与传过来的id比较
			if (!StringUtils.isEmpty(map.get("parentId"))) {
				if (map.get("parentId").toString().equals(id)) {
					childList.add(map);
				}
			}
		}
		// 把子菜单的子菜单再循环一遍
		for (Map<String, Object> menu : childList) {
			// 递归
			List<Map<String, Object>> subList = getChild(menu.get("menuId")
					.toString(), rootMenu);
			if (subList != null) {
				menu.put("subMenus", subList);
			}
		} // 递归退出条件
		if (childList.size() == 0) {
			return null;
		}
		return childList;
	}
	
	/**
	 * 获取用户审核URL权限
	 * 
	 * @return
	 */
	public static String getUserCheckUrls(String roleId) {
		String menuUrls = (String) RedisUtils.getRedisService().get(
				KEFU_USER_CHECK_MENUS_KEY + roleId);
		if (StringUtils.isEmpty(menuUrls)) {
			menuUrls = refreshUserCheckUrls(roleId);
		}
		return menuUrls;
	}
	
	/**
	 * 刷新用户URL权限
	 * 
	 * @return
	 */
	public static String refreshUserCheckUrls(String roleId) {

		AppParam param = new AppParam();
		param.setService("sysKfMenuService");
		param.setMethod("queryUserCheckUrls");
		param.addAttr("roleId", roleId);
		param.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START
						+ ServiceKey.Key_cust));
		AppResult menuResult = RemoteInvoke.getInstance().callNoTx(param);

		String menuUrls = "";
		if (!StringUtils.isEmpty(menuResult.getRow(0))) {
			menuUrls = (String) menuResult.getRow(0).get("menuUrls");
			RedisUtils.getRedisService().set(KEFU_USER_CHECK_MENUS_KEY + roleId,
					menuUrls, 30 * 60 * 12);
		}

		return menuUrls;
	}
	
	/**
	 * 获取用户审核URL code
	 * 
	 * @return
	 */
	public static String getUserCheckCode(String roleId) {
		String menuUrls = (String) RedisUtils.getRedisService().get(
				KEFU_USER_CHECK_MENUS_CODE_KEY + roleId);
		if (StringUtils.isEmpty(menuUrls)) {
			menuUrls = refreshUserCheckCode(roleId);
		}
		return menuUrls;
	}
	
	/**
	 * 刷新用户URL code
	 * 
	 * @return
	 */
	public static String refreshUserCheckCode(String roleId) {

		AppParam param = new AppParam();
		param.setService("sysKfMenuService");
		param.setMethod("queryUserCheckCode");
		param.addAttr("roleId", roleId);
		param.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START
						+ ServiceKey.Key_cust));
		AppResult menuResult = RemoteInvoke.getInstance().callNoTx(param);

		String menuUrls = "";
		if (!StringUtils.isEmpty(menuResult.getRow(0))) {
			menuUrls = (String) menuResult.getRow(0).get("menuUrls");
			RedisUtils.getRedisService().set(KEFU_USER_CHECK_MENUS_CODE_KEY + roleId,
					menuUrls, 30 * 60 * 12);
		}

		return menuUrls;
	}
	
	/**
	 * 菜单权限判断
	 * 
	 * @param url
	 * @param userId
	 * @return
	 */
	public static boolean validateURL(String userId, String url) {
		String keyVal = "/account/";

		String btnUrl = url.substring(url.indexOf(keyVal)
				+ keyVal.length() - 1);// 方法url
		String btnUrl2 = btnUrl.substring(btnUrl.lastIndexOf("/") + 1);
		String menuUrl = btnUrl.substring(0, btnUrl.lastIndexOf("/"));// 菜单url
		
		Map<String, Object> custInfo = CustomerIdentify.getCustIdentify(userId);
		String roleId = "";
		if(custInfo != null){
			roleId =   StringUtil.getString(custInfo.get("kfAuthType"));
		}
		// 用户url权限
		String menuRole = getUserMenuUrls(roleId);
		String checkRole = getUserCheckUrls(roleId);
		String modifyRole = getUserModifyUrls(roleId);

		String subUrl = org.apache.commons.lang.StringUtils.substringBetween(url, "/account/", "/");
		if ("user".equals(subUrl)) {
			return true;
		}
		if (btnUrl2.contains("query")) {
			if (!menuRole.contains(menuUrl)) {
				return false;
			}
		} else if (btnUrl2.contains("check")) {
			if (!checkRole.contains(menuUrl)) {
				return false;
			}
		} else {
			if (!modifyRole.contains(menuUrl)) {
				return false;
			}
		}

		return true;
	}
	
	/**
	 * 获取用户URL权限
	 * 
	 * @return
	 */
	public static String getUserMenuUrls(String roleId) {
		String menuUrls = (String) RedisUtils.getRedisService().get(
				KEFU_USER_BASE_MENUS_KEY + roleId);
		if (StringUtils.isEmpty(menuUrls)) {
			menuUrls = refreshUserMenuUrls(roleId);
		}
		return menuUrls;
	}
	
	/**
	 * 刷新用户URL权限
	 * 
	 * @return
	 */
	public static String refreshUserMenuUrls(String roleId) {

		AppParam param = new AppParam();
		param.setService("sysKfMenuService");
		param.setMethod("queryUserMenuUrls");
		param.addAttr("roleId", roleId);
		param.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START
						+ ServiceKey.Key_cust));
		AppResult menuResult = RemoteInvoke.getInstance().callNoTx(param);

		String menuUrls = "";
		if (!StringUtils.isEmpty(menuResult.getRow(0))) {
			menuUrls = (String) menuResult.getRow(0).get("menuUrls");
			RedisUtils.getRedisService().set(KEFU_USER_BASE_MENUS_KEY + roleId,
					menuUrls, 30 * 60);
		}

		return menuUrls;
	}

	
	/**
	 * 获取用户审核URL权限
	 * 
	 * @return
	 */
	public static String getUserModifyUrls(String roleId) {
		String menuUrls = (String) RedisUtils.getRedisService().get(
				KEFU_USER_MODIFY_MENUS_KEY + roleId);
		if (StringUtils.isEmpty(menuUrls)) {
			menuUrls = refreshUserModifyUrls(roleId);
		}
		return menuUrls;
	}
	
	/**
	 * 刷新用户URL权限
	 * 
	 * @return
	 */
	public static String refreshUserModifyUrls(String roleId) {

		AppParam param = new AppParam();
		param.setService("sysKfMenuService");
		param.setMethod("queryUserModifyUrls");
		param.addAttr("roleId", roleId);
		param.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START
						+ ServiceKey.Key_cust));
		AppResult menuResult = RemoteInvoke.getInstance().callNoTx(param);

		String menuUrls = "";
		if (!StringUtils.isEmpty(menuResult.getRow(0))) {
			menuUrls = (String) menuResult.getRow(0).get("menuUrls");
			RedisUtils.getRedisService().set(KEFU_USER_MODIFY_MENUS_KEY + roleId,
					menuUrls, 30 * 60 * 12);
		}

		return menuUrls;
	}

}
