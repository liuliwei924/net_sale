package org.xxjr.store.web.action.account.user;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.exception.ExceptionUtil;
import org.ddq.common.util.LogerUtil;
import org.ddq.common.util.StringUtil;
import org.ddq.common.web.session.DuoduoSession;
import org.ddq.common.web.session.RequestUtil;
import org.ddq.common.core.service.RemoteInvoke;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.xxjr.busi.util.store.StoreMenuUtils;
import org.xxjr.busi.util.store.StoreUserUtil;
import org.xxjr.cust.util.CustConstant;
import org.xxjr.cust.util.info.CustomerIdentify;
import org.xxjr.store.util.StoreApplyUtils;
import org.xxjr.sys.util.AreaUtils;
import org.xxjr.sys.util.OrgUtils;
import org.xxjr.sys.util.ServiceKey;

/***
 * 公共查询相关
 * @author ZQH
 *
 */
@Controller()
@RequestMapping("/account/user/comm/")
public class StoreCommInfoAction {
	
	/***
	 * 通过orgId获取门店人员(roleType=3、7、8、9)
	 * @param request
	 * @return
	 */
	@RequestMapping("getOrgCustList")
	@ResponseBody
	public AppResult getOrgCustList(HttpServletRequest request){
		AppResult result = new AppResult();
		String orgId = request.getParameter("orgId");
		if(StringUtils.isEmpty(orgId)){
			result.setSuccess(false);
			result.setMessage("门店ID不能为空");
			return result;
		}
		AppParam param  = new AppParam();
		String customerId = StoreUserUtil.getCustomerId(request);
		StoreUserUtil.dealUserAuthParam(param, customerId, null);
		param.setService("customerExtService");
		param.setMethod("queryStoreCustInfo");
		param.addAttr("orgId",orgId);
		param.addAttr("roleTypeIn","3,6,7,8,9");
		param.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START 
				+ ServiceKey.Key_cust));
		result = RemoteInvoke.getInstance().call(param);
		return result;
	}
	
	/***
	 * 通过groupname获取门店人员
	 * @param request
	 * @return
	 */
	@RequestMapping("getGroupCustList")
	@ResponseBody
	public AppResult getGroupCustList(HttpServletRequest request){
		AppResult result = new AppResult();
		String orgId = request.getParameter("orgId");
		if(StringUtils.isEmpty(orgId)){
			result.setSuccess(false);
			result.setMessage("门店ID不能为空");
			return result;
		}
		try{
			AppParam param  = new AppParam();
			String customerId = StoreUserUtil.getCustomerId(request);
			StoreUserUtil.dealUserAuthParam(param, customerId, null);
			String groupName = request.getParameter("groupName");
			param.setService("customerExtService");
			param.setMethod("queryStoreCustInfo");
			param.addAttr("orgId",orgId);
			param.addAttr("groupName",groupName);
			//离职人ID
			String leavelCustId = request.getParameter("leavelCustId");
			if(!StringUtils.isEmpty(leavelCustId)){
				param.addAttr("leavelCustId",leavelCustId);
			}
			param.addAttr("roleTypeIn","3,6,7,8,9");
			param.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_cust));
			result = RemoteInvoke.getInstance().call(param);
		}catch (Exception e){
			LogerUtil.error(this.getClass(),e,"getGroupCustList error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	
	/***
	 * 通过orgId获取门店所有组
	 * @param request
	 * @return
	 */
	@RequestMapping("queryOrgGroupList")
	@ResponseBody
	public AppResult queryOrgGroupList(HttpServletRequest request){
		AppResult result = new AppResult();
		String orgId = request.getParameter("orgId");
		if(StringUtils.isEmpty(orgId)){
			result.setSuccess(false);
			result.setMessage("门店不能为空");
			return result;
		}
		try{
			AppParam param  = new AppParam();
			RequestUtil.setAttr(param, request);
			List<Map<String,Object>> listMap = StoreApplyUtils.getStoreOrgGroup(param);
			if(listMap.size() > 0){
				result.addRows(listMap);
			}
		}catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "queryOrgGroupList error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	/***
	 * 通过orgId获取门店所有队
	 * @param request
	 * @return
	 */
	@RequestMapping("queryOrgTeamList")
	@ResponseBody
	public AppResult queryOrgTeamList(HttpServletRequest request){
		AppResult result = new AppResult();
		String orgId = request.getParameter("orgId");
		if(StringUtils.isEmpty(orgId)){
			result.setSuccess(false);
			result.setMessage("门店不能为空");
			return result;
		}
		String groupName = request.getParameter("groupName");
		if(StringUtils.isEmpty(groupName)){
			result.setSuccess(false);
			result.setMessage("组名不能为空");
			return result;
		}
		try{
			AppParam param  = new AppParam();
			RequestUtil.setAttr(param, request);
			List<Map<String,Object>> listMap = StoreApplyUtils.getStoreOrgTeam(param);
			if(listMap.size() > 0){
				result.addRows(listMap);
			}
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "queryOrgTeamList error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		
		return result;
	}
	
	/**
	 * 获取所有城市(剔除省份)
	 * @return
	 */
	@RequestMapping("allCity")
	@ResponseBody
	public AppResult allCity(){
		AppResult result = new AppResult();
		try {
			result.putAttr("allCity", AreaUtils.getAllCity());
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, " Execute error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 获取所有区域信息(包括省份)
	 * @return
	 */
	@RequestMapping("allAreaInfo")
	@ResponseBody
	public AppResult allAreaInfo(){
		AppResult result = new AppResult();
		try {
			List<Map<String,Object>> allArea = new ArrayList<Map<String,Object>>();
			List<Map<String,Object>> provinceList = AreaUtils.getAllInfo();
			Map<String,List<Map<String,Object>>> citysMap = new HashMap<String, List<Map<String,Object>>>();
			for(Map<String,Object> row : provinceList){
				String provinceName = row.remove("provinceName").toString();
				String[] districts = row.remove("districtNames").toString().split(",");
				row.put("districts", districts);
				if(citysMap.containsKey(provinceName)){
					citysMap.get(provinceName).add(row);
				}else{
					List<Map<String,Object>> temp = new ArrayList<Map<String,Object>>();
					temp.add(row);
					citysMap.put(provinceName, temp);
				}
			}
			for (String key : citysMap.keySet()) {
				Map<String,Object> item = new HashMap<String, Object>();
				item.put("provinceName", key);
				item.put("citys", citysMap.get(key));
				allArea.add(item);
			}
			result.putAttr("allArea", allArea);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, " Execute allAreaInfo error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	
	/**
	 *  查询所有外部渠道人员
	 * @param request
	 * @return
	 */
	@RequestMapping("queryExternalUser")
	@ResponseBody
	public AppResult queryExternalUser(HttpServletRequest request){
		AppResult result = new AppResult();
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			params.addAttr("roleType", 10);//外部渠道人员
			params.setService("customerService");
			params.setMethod("query");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_cust));
			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "查询所有外部渠道人员！");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 获取网销城市
	 * @param request
	 * @return
	 */
	@RequestMapping("queryNetCity")
	@ResponseBody
	public AppResult queryNetCity(HttpServletRequest request){
		AppResult result = new AppResult();
		try {
			//门店列表
			result.putAttr("cityNameList", OrgUtils.getIsNetCityList());
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "获取网销城市出错！");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	@RequestMapping("getRoteFile")
	public void getRoteFile(HttpServletRequest request, HttpServletResponse response) {  
		String strUrl = request.getParameter("urlPath").trim();  
		  try {    
			  InputStream fis=null;  
              request.setCharacterEncoding("utf-8");  
              response.setCharacterEncoding("utf-8");  
              URL url=new URL(strUrl);  
              //打开请求连接  
              URLConnection connection = url.openConnection();  
              HttpURLConnection conn=(HttpURLConnection) connection;  
              conn.setConnectTimeout(20000);  
              conn.setReadTimeout(20000);  
              conn.connect();  
              response.setContentType("application/pdf");  
              ServletOutputStream sos=response.getOutputStream();  
              fis=conn.getInputStream();  
              int b;  
              while((b=fis.read())!=-1){  
                  sos.write(b);  
              }  
              sos.close();   
              fis.close();  
          } catch (IOException e) {    
    		LogerUtil.error(StoreCommInfoAction.class,e, "getRoteFile error");
			ExceptionUtil.setExceptionMessage(e, null, DuoduoSession.getShowLog());
          }    
	}
	
	/***
	 * 获取产品类型目录结构
	 * @param request
	 * @return
	 */
	@RequestMapping("queryProductMenu")
	@ResponseBody
	public AppResult queryProductMenu(HttpServletRequest request){
		AppResult result = new AppResult();
		result.putAttr("proTypeData" ,StoreMenuUtils.getProductMenusTree());
		return result;
	}
	
	/**
	 * 获取网销门店
	 * @param request
	 * @return
	 */
	@RequestMapping("queryNetOrg")
	@ResponseBody
	public AppResult queryNetOrg(HttpServletRequest request){
		AppResult result = new AppResult();
		try {
			//网销门店列表
			result.putAttr("netOrgList", OrgUtils.getNetOrgList());
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "获取网销门店出错！");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 获取网销城市(非管理员只能查看门店所在城市)
	 * @param request
	 * @return
	 */
	@RequestMapping("getNetCityByOrgId")
	@ResponseBody
	public AppResult getNetCityByOrgId(HttpServletRequest request){
		AppResult result = new AppResult();
		try {
			String customerId = StoreUserUtil.getCustomerId(request);
			String roleType = StoreUserUtil.getCustomerRole(customerId);
			if(!CustConstant.CUST_ROLETYPE_1.equals(roleType)){
				Map<String, Object> custInfo = CustomerIdentify.getCustIdentify(customerId);
				String orgId = StringUtil.getString(custInfo.get("orgId"));
				List<Map<String, Object>> cityList = new ArrayList<Map<String,Object>>();
				Map<String, Object> map = new HashMap<String,Object>();
				map.put("cityName", OrgUtils.getCityNameByOrgId(orgId));
				cityList.add(map);
				result.putAttr("cityNameList",cityList);
			}else{
				result.putAttr("cityNameList", OrgUtils.getIsNetCityList());
			}
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "获取网销城市出错！");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
}
