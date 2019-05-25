package org.xxjr.sys.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.service.RemoteInvoke;
import org.ddq.common.util.StringUtil;
import org.llw.model.cache.RedisUtils;
import org.springframework.util.StringUtils;
import org.xxjr.cust.util.info.CustomerIdentify;

/**
 * 门店
 * @author Administrator
 *
 */
public class OrgUtils {


	/** 门店缓存 */
	public static final String KEY_ORG_INFO = "key_org_info";


	/**
	 * 获取门店列表
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<Map<String, Object>> getOrgList(){
		List<Map<String, Object>> list = (List<Map<String, Object>>)RedisUtils.getRedisService().get(KEY_ORG_INFO);
		if(list == null || list.size() == 0){
			list = refreshOrgList();
		}
		return list;
	}

	/**
	 * 获取网销门店列表
	 * @return
	 */
	public static List<Map<String, Object>> getIsNetOrgList(){
		List<Map<String, Object>> listMap = getOrgList();
		List<Map<String, Object>> newMap = new ArrayList<Map<String,Object>>();
		for(Map<String, Object> map : listMap){
			int gradeCodeTemp = NumberUtil.getInt(map.get("isNet"));
			if(1 == gradeCodeTemp){
				newMap.add(map);
			}
		}
		return newMap;
	}
	/**
	 * 获取网销门店列表(新-运用java 8  新特性)
	 * @return
	 */
	public static List<Map<String, Object>> getNetOrgList(){
		List<Map<String, Object>> listMap = getOrgList();
		List<Map<String, Object>> netOrgMap = new ArrayList<Map<String,Object>>();
		if(listMap != null && !StringUtils.isEmpty(listMap.get(0))){
			Stream<Map<String,Object>> mapStream = listMap.stream();
			netOrgMap = mapStream.filter(m -> "1".equals(StringUtil.getString(m.get("isNet")))).collect(Collectors.toList());
		}
		return netOrgMap;
	}
	
	/**
	 * 通过城市获取网销门店列表(新-运用java 8  新特性 )
	 * @return
	 */
	public static List<Map<String, Object>> getNetOrgListByCity(String cityName){
		List<Map<String, Object>> listMap = getOrgList();
		List<Map<String, Object>> netOrgMap = new ArrayList<Map<String,Object>>();
		if(listMap != null && !StringUtils.isEmpty(listMap.get(0)) 
				&& !StringUtils.isEmpty(cityName)){
			Stream<Map<String,Object>> mapStream = listMap.stream();
			netOrgMap = mapStream.filter(m -> "1".equals(StringUtil.getString(m.get("isNet"))) && 
					cityName.equals(StringUtil.getString(m.get("cityName")))).collect(Collectors.toList());
		}
		return netOrgMap;
	}
	
	/**
	 * 获取网销城市列表
	 * @return
	 */
	public static List<Map<String, Object>> getIsNetCityList(){
		List<Map<String, Object>> listMap = getOrgList();
		List<Map<String, Object>> cityList = new ArrayList<Map<String, Object>>();
		List<String> cityList2 = new ArrayList<String>();
		for(Map<String, Object> map : listMap){
			int gradeCodeTemp = NumberUtil.getInt(map.get("isNet"));
			if(1 == gradeCodeTemp){
				Map<String, Object> cityMap = new HashMap<String,Object>();
				String cityName = StringUtil.getString(map.get("cityName"));
				if(!cityList2.contains(cityName)){
					cityList2.add(cityName);
					cityMap.put("cityName", map.get("cityName"));
					cityList.add(cityMap);
				}
			}
		}
		return cityList;
	}
	
	/**
	 * 获取统计门店列表
	 * @return
	 */
	public static List<Map<String, Object>> getIsCountOrgList(){
		List<Map<String, Object>> listMap = getOrgList();
		List<Map<String, Object>> newMap = new ArrayList<Map<String,Object>>();
		for(Map<String, Object> map : listMap){
			int gradeCodeTemp = NumberUtil.getInt(map.get("isCount"),0);
			if(1 == gradeCodeTemp){
				newMap.add(map);
			}
		}
		return newMap;
	}
	
	/**
	 * 刷新门店
	 * @return
	 */
	public static List<Map<String, Object>> refreshOrgList(){
		AppParam param = new AppParam();
		param.setService("orgService");
		param.setMethod("queryOrgList");
		param.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_cust));
		AppResult result = RemoteInvoke.getInstance().call(param);
		List<Map<String, Object>> list =  result.getRows();
		RedisUtils.getRedisService().set(KEY_ORG_INFO, (Serializable)list);
		return list;
	}


	/**
	 * 获取用户管理门店列表
	 * @return
	 */
	public static List<Map<String, Object>> getUserOrgList(String customerId){
		List<Map<String, Object>> list = getOrgList();
		List<Map<String, Object>> userOrgList = new ArrayList<Map<String,Object>>();
		//获取用户信息
		Map<String, Object> custInfo = CustomerIdentify.getCustIdentify(customerId);
		if(custInfo != null){
			String userOrgs =  StringUtil.getString(custInfo.get("userOrgs"));
			if("all".equals(userOrgs)){
				return list;
			}else if(StringUtils.isEmpty(userOrgs)){
				for(Map<String,Object> map:list){
					if(map.get("orgId").equals(custInfo.get("orgId"))){
						userOrgList.add(map);
						break;
					}
				}
			}else{
				for(String orgId:userOrgs.split(",")){
					if(orgId.trim().length()==0) continue;
					for(Map<String,Object> map:list)
						if(StringUtil.getString(map.get("orgId")).equals(orgId)){
							userOrgList.add(map);
							break;
						}
				}
			}
		}

		return userOrgList;
	}
	
	/**
	 * 通过orgId获取门店编号(orgNo)
	 * @return
	 */
	public static String getOrgNoByOrgId(String orgId){
		String orgNo = "";
		List<Map<String, Object>> listMap = getOrgList();
		for(Map<String, Object> map : listMap){
			String orgTemp = StringUtil.getString(map.get("orgId"));
			if(orgId.equals(orgTemp)){
				orgNo = StringUtil.getString(map.get("orgNo"));
				break;
			}
		}
		return orgNo;
	}
	
	/**
	 * 通过orgId获取门店名称
	 * @return
	 */
	public static String getOrgNameByOrgId(String orgId){
		String orgName = "";
		List<Map<String, Object>> listMap = getOrgList();
		for(Map<String, Object> map : listMap){
			String orgTemp = StringUtil.getString(map.get("orgId"));
			if(orgId.equals(orgTemp)){
				orgName = StringUtil.getString(map.get("orgName"));
				break;
			}
		}
		return orgName;
	}
	
	/**
	 * 通过门店Id获取城市
	 * @return
	 */
	public static String getCityNameByOrgId(String orgId){
		String cityName = "";
		if(!StringUtils.isEmpty(orgId)){
			List<Map<String, Object>> listMap = getOrgList();
			for(Map<String, Object> map : listMap){
				String orgTemp = StringUtil.getString(map.get("orgId"));
				if(orgId.equals(orgTemp)){
					cityName = StringUtil.getString(map.get("cityName"));
					break;
				}
			}
		}
		return cityName;
	}
}
