package org.xxjr.store.web.action.common;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ddq.active.mq.message.RmiServiceSend;
import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.SpringAppContext;
import org.ddq.common.core.service.RemoteInvoke;
import org.ddq.common.security.MD5Util;
import org.ddq.common.security.md5.Md5;
import org.ddq.common.util.JsonUtil;
import org.ddq.common.util.LogerUtil;
import org.ddq.common.util.StringUtil;
import org.ddq.common.web.session.DuoduoSession;
import org.ddq.common.web.session.RequestUtil;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.xxjr.busi.util.ApplyUnionUtil;
import org.xxjr.busi.util.BorrowChannelUtil;
import org.xxjr.busi.util.ThirdDataUtil;
import org.xxjr.busi.util.kf.BorrowApplyUtils;
import org.xxjr.cust.util.info.MapLocaltionUtil;
import org.xxjr.sys.util.AreaUtils;
import org.xxjr.sys.util.NumberUtil;
import org.xxjr.sys.util.ServiceKey;
import org.xxjr.sys.util.SysParamsUtil;
import org.xxjr.sys.util.ValidUtils;
import org.xxjr.tools.util.AESUtil;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/cooper/org/")
@Slf4j
public class CooperOrgAction {

	/**
	 * 亿科思奇数据接收
	 * @return
	 */
	@RequestMapping("thirdData/{corp}")
	@ResponseBody
	public Map<String, Object> thirdData(@PathVariable("corp") String corp, HttpServletRequest request, HttpServletResponse response){
		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");
		HashMap<String, Object> responseParam = new HashMap<String, Object>();
		try{
			AppResult checkChannel = ThirdDataUtil.isCheckChannel(corp);
			if (!checkChannel.isSuccess()) {
				responseParam.put("returnCode", checkChannel.getErrorCode());
				responseParam.put("errorMessage", checkChannel.getMessage());
				return responseParam;
			}
			AppParam appParam = new AppParam();
			RequestUtil.setAttr(appParam, request);
			Map<String,Object> params = appParam.getAttr();
			String thirdData = params.toString();
			LogerUtil.log(corp + " third data:" + params.toString());
			if(StringUtils.isEmpty(params.get("time")) || StringUtils.isEmpty(params.get("sign"))
					|| StringUtils.isEmpty(params.get("telephone"))
					|| StringUtils.isEmpty(params.get("applyName"))
					|| StringUtils.isEmpty(params.get("loanAmount"))
					|| StringUtils.isEmpty(params.get("cityName"))
					|| StringUtils.isEmpty(params.get("applyIp"))
					|| StringUtils.isEmpty(params.get("haveWeiLi"))){
				responseParam.put("returnCode", "001");
				responseParam.put("errorMessage", "缺少必要参数");
				return responseParam;
			}
			// 验证手机号
			if (!ValidUtils.validateTelephone(StringUtil.objectToStr(params.get("telephone")))) {
				responseParam.put("returnCode", "009");
				responseParam.put("errorMessage", "手机号格式不正确!");
				return responseParam;
			}
			
			boolean gzSpecial = false;
			// 判断广州渠道
			if(!StringUtils.isEmpty(params.get("gzSpecial")) 
					&& "1".equals(params.get("gzSpecial").toString())){
				gzSpecial = true;
			}
			
			Double loanAmount = Double.valueOf(params.get("loanAmount").toString());
			int maxLoanAmount = SysParamsUtil.getIntParamByKey("thirdMaxLoanAmount", 1000);
			int defaultLoanAmount = SysParamsUtil.getIntParamByKey("thirdDefaultLoanAmount", 1000);
			
			if (loanAmount > maxLoanAmount) {//大于第三方配置最大的贷款金给一个默认的
				//loanAmount = loanAmount / 10000;
				params.put("loanAmount", defaultLoanAmount);
			}
			
			if(gzSpecial && (Double.valueOf(params.get("loanAmount").toString()) < 3 
							|| !params.get("cityName").toString().contains("广州"))){
				responseParam.put("returnCode", "007");
				responseParam.put("errorMessage", "非广州数据或金额小于3W");
				return responseParam;
			}
			String merchId = null;
			if("yksq".equals(corp)){
				merchId = SysParamsUtil.getStringParamByKey("yksqMerchId", "yksq10180");
			}else if("suyi".equals(corp)){
				merchId = SysParamsUtil.getStringParamByKey("suyiMerchId", "suyi10180");
			}else if("najia".equals(corp)){
				merchId = SysParamsUtil.getStringParamByKey("najiaMerchId", "najia10180");
			}
			if(StringUtils.isEmpty(merchId)){
				Map<String,Object> channel = BorrowChannelUtil.getChannelByCode(corp);
				merchId = org.ddq.common.util.StringUtil.getString(channel.get("merchId"));
			}
			String time = params.remove("time").toString();
			String telephone = params.get("telephone").toString();
			String fromSign = params.remove("sign").toString();
			String xxSign = MD5Util.getEncryptByKey(telephone + "&" + time, merchId);
			if(!fromSign.equalsIgnoreCase(xxSign) && !corp.equals("testApi")){
				responseParam.put("returnCode", "002");
				responseParam.put("errorMessage", "签名有误");
			}else{
				
				// 效验suyi城市名称
				if("suyi".equals(corp) || "sy".equals(corp)){
					Object cityName = queryCityName(params.get("cityName"));
					if(StringUtils.isEmpty(cityName)){
						responseParam.put("returnCode", "008");
						responseParam.put("errorMessage", "城市code：" + params.get("cityName") + "未找到对应的城市");
						return responseParam;
					}
					params.put("cityName", cityName);
				}else{
					// 查找 深圳
					//判断城市是否查找，存在传入数据库的参数，不存在
					Object cityCode = this.queryCityCode(params.get("cityName"));
					if (cityCode == null) {
						responseParam.put("returnCode", "008");
						responseParam.put("errorMessage", "城市：" + params.get("cityName") + "未找到对应的城市");
						return responseParam;
					}
				}
				boolean isRepeat = false;
				boolean isHandling = false;// 是否正在处理中
			
				//渠道去重处理
				isRepeat = isThirdRepeat(telephone, corp);
				
				
				if("yksq".equals(corp)){
					ThirdDataUtil.transParams(params);
					LogerUtil.log("after trans:" + params.toString());
				}
				
				// 插入数据到中间表
				AppParam applyParam = new AppParam();
				applyParam.setService("thirdDataService");
				applyParam.setMethod("save");
				applyParam.addAttr("channelDetail", corp);
				applyParam.addAttr("params", thirdData);
				if(gzSpecial){
					applyParam.addAttr("channelDetail", corp+"Gz");
				}

				ThirdDataUtil.getAge(StringUtil.getString(params.get("birthday")), params);
                applyParam.addAttr("applyIp", params.get("applyIp"));
				applyParam.addAttr("isRepeat", isRepeat ? "1" : "0" );
				applyParam.addAttr("isHandling", isHandling ? "1" : "0");
				applyParam.addAttr("pageReferer", corp);
				applyParam.addAttr("sourceChannel", corp);
				applyParam.addAttr("mediaSource", params.get("mediaSource"));
				applyParam.addAttrs(params);
				if (StringUtils.isEmpty(applyParam.getAttr("channelDetail"))) {
					applyParam.addAttr("channelDetail", corp);
				}
				applyParam.setRmiServiceName(AppProperties
						.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
				if (!StringUtils.isEmpty(applyParam.getAttr("carType"))) {
					int carType = Integer.valueOf(params.get("carType").toString());
					if (carType == 0) {
						applyParam.addAttr("carType", "2");
					}
				}
				boolean randomRepeat = false;
				int deduction = NumberUtil.getInt(checkChannel.getAttr("deduction"));
				if (deduction > 0 && !isRepeat) {
					randomRepeat = ThirdDataUtil.randomRepeat(deduction, corp, StringUtil.objectToStr(checkChannel.getAttr("randoms")));
				}
				if (randomRepeat) {
					//再保存改变了渠道的数据
					applyParam.addAttr("channelDetail", applyParam.getAttr("channelDetail") + "_sale");
				}
				AppResult saveResult = RemoteInvoke.getInstance().call(applyParam);
				if(saveResult.isSuccess()){
					if(isRepeat || randomRepeat){
						responseParam.put("returnCode", "003");
						responseParam.put("errorMessage", "申请重复");
						responseParam.put("telephone", telephone);
					}else{
						responseParam.put("unionId", saveResult.getAttr("unionId"));
						responseParam.put("returnCode", "000");
						responseParam.put("errorMessage", "接收成功");
					}
				}else{
					if (!StringUtils.isEmpty(saveResult.getErrorCode()) && "003".equals(saveResult.getErrorCode())) {
						responseParam.put("returnCode", "003");
						responseParam.put("errorMessage", "申请重复");
						responseParam.put("telephone", telephone);
					}else {
						responseParam.put("returnCode", "004");
						responseParam.put("errorMessage", "接收异常");
					}
				}
			}
		}catch(Exception e){
			LogerUtil.error(this.getClass(), e, "thirdData error！");
			responseParam.put("returnCode", "005");
			responseParam.put("errorMessage", "其他异常");
		}
		finally{
			LogerUtil.log("response:" + responseParam.toString());
		}
		return responseParam;
	}
	
	/**
	 * 第三方数据修改操作
	 * @return
	 */
	@RequestMapping("thirdUpdateData/{corp}")
	@ResponseBody
	public Map<String, Object> thirdUpdateData(@PathVariable("corp") String corp, HttpServletRequest request, HttpServletResponse response){
		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");
		HashMap<String, Object> responseParam = new HashMap<String, Object>();
		try{
			AppResult checkChannel = ThirdDataUtil.isCheckChannel(corp);
			if (!checkChannel.isSuccess()) {
				responseParam.put("returnCode", checkChannel.getErrorCode());
				responseParam.put("errorMessage", checkChannel.getMessage());
				return responseParam;
			}
			AppParam appParam = new AppParam();
			RequestUtil.setAttr(appParam, request);
			Map<String,Object> params = appParam.getAttr();
			LogerUtil.log(corp + " thirdUpdateData data:" + params.toString());
			if(StringUtils.isEmpty(params.get("time")) || StringUtils.isEmpty(params.get("sign"))
					|| StringUtils.isEmpty(params.get("unionId"))){
				responseParam.put("returnCode", "001");
				responseParam.put("errorMessage", "缺少必要参数");
				return responseParam;
			}
			
		/*	if (!StringUtils.isEmpty(params.get("loanAmount"))) {
				Double loanAmount = NumberUtil.getDouble(params.get("loanAmount"), 0);
				int maxLoanAmount = SysParamsUtil.getIntParamByKey("thirdMaxLoanAmount", 1000);
				int defaultLoanAmount = SysParamsUtil.getIntParamByKey("thirdDefaultLoanAmount", 1000);
				if (loanAmount > maxLoanAmount) {//大于第三方配置最大的贷款金给一个默认的
					//loanAmount = loanAmount / 10000;
					params.put("loanAmount", defaultLoanAmount);
				}
			}*/
			
			String merchId = null;
			if("yksq".equals(corp)){
				merchId = SysParamsUtil.getStringParamByKey("yksqMerchId", "yksq10180");
			}else if("suyi".equals(corp)){
				merchId = SysParamsUtil.getStringParamByKey("suyiMerchId", "suyi10180");
			}else if("najia".equals(corp)){
				merchId = SysParamsUtil.getStringParamByKey("najiaMerchId", "najia10180");
			}
			if(StringUtils.isEmpty(merchId)){
				Map<String,Object> channel = BorrowChannelUtil.getChannelByCode(corp);
				merchId = org.ddq.common.util.StringUtil.getString(channel.get("merchId"));
			}
			
			String time = params.remove("time").toString();
			String unionId = params.get("unionId").toString();
			String fromSign = params.remove("sign").toString();
			String xxSign = MD5Util.getEncryptByKey(unionId + "&" + time, merchId);
			if(!fromSign.equalsIgnoreCase(xxSign) && !corp.equals("testApi")){
				responseParam.put("returnCode", "002");
				responseParam.put("errorMessage", "签名有误");
			}else{
				params.remove("applyTime");
				params.remove("applyName");
				params.remove("loanAmount");
				params.remove("cityName");
				
				// 插入数据到中间表
				AppParam applyParam = new AppParam();
				applyParam.setService("thirdDataService");
				applyParam.setMethod("updateDate");
				ThirdDataUtil.getAge(StringUtil.getString(params.get("birthday")), params);
				applyParam.addAttr("channelCode", corp);
				applyParam.addAttrs(params);
				if (StringUtils.isEmpty(applyParam.getAttr("channelDetail"))) {
					applyParam.addAttr("channelDetail", corp);
				}
				applyParam.setRmiServiceName(AppProperties
						.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
				AppResult saveResult = RemoteInvoke.getInstance().call(applyParam);
				if(saveResult.isSuccess()){
					responseParam.put("returnCode", "000");
					responseParam.put("errorMessage", "操作成功");
				}else{
					if (!StringUtils.isEmpty(saveResult.getErrorCode())) {
						responseParam.put("returnCode", saveResult.getErrorCode());
						responseParam.put("errorMessage", saveResult.getMessage());
					}else {
						responseParam.put("returnCode", "004");
						responseParam.put("errorMessage", "服务器异常!");
					}
				}
			}
		}catch(Exception e){
			LogerUtil.error(this.getClass(), e, "thirdData error！");
			responseParam.put("returnCode", "005");
			responseParam.put("errorMessage", "其他异常");
		}
		finally{
			LogerUtil.log("response:" + responseParam.toString());
		}
		return responseParam;
	}
	
	@RequestMapping("queryHasUser/{corp}")
	@ResponseBody
	public Map<String, Object> queryHasUser (@PathVariable("corp") String corp, HttpServletRequest request, HttpServletResponse response) {
		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");
		HashMap<String, Object> responseParam = new HashMap<String, Object>();
		boolean isRepeat = false;
		AppParam appParam = new AppParam();
		RequestUtil.setAttr(appParam, request);
		Map<String,Object> params = appParam.getAttr();
		try {
			String channelCodes = SysParamsUtil.getStringParamByKey("queryHasUser_channels", "zhongna,");
			if (channelCodes.contains((corp+","))) {
				responseParam.put("returnCode", "0021");
				responseParam.put("errorMessage", "没有权限");
				return responseParam;
			}
			
			LogerUtil.log(corp + " third data:" + params.toString());
			if(StringUtils.isEmpty(params.get("time")) || StringUtils.isEmpty(params.get("sign"))
					|| StringUtils.isEmpty(params.get("telephone"))){
				responseParam.put("returnCode", "001");
				responseParam.put("errorMessage", "缺少必要参数");
				return responseParam;
			}
			String time = StringUtil.getString(appParam.getAttr("time"));
			String telephone = StringUtil.getString(appParam.getAttr("telephone"));
			String fromSign = StringUtil.getString(appParam.getAttr("sign"));
			String merchId = null;
			if("yksq".equals(corp)){
				merchId = SysParamsUtil.getStringParamByKey("yksqMerchId", "yksq10180");
			}else if("suyi".equals(corp)){
				merchId = SysParamsUtil.getStringParamByKey("suyiMerchId", "suyi10180");
			}else if("najia".equals(corp)){
				merchId = SysParamsUtil.getStringParamByKey("najiaMerchId", "najia10180");
			}
			if(StringUtils.isEmpty(merchId)){
				Map<String,Object> channel = BorrowChannelUtil.getChannelByCode(corp);
				merchId = org.ddq.common.util.StringUtil.getString(channel.get("merchId"));
			}
			String xxSign = MD5Util.getEncryptByKey(time, merchId);
			if(!fromSign.equalsIgnoreCase(xxSign)){
				responseParam.put("returnCode", "002");
				responseParam.put("errorMessage", "签名有误");
			}
			
			try {
				//判断如果不是完整手机号保不保存
				boolean flag = SysParamsUtil.getBoleanByKey("save_validate_tel", false);
				if (ValidUtils.validateTelephone(telephone) || flag) {
					RmiServiceSend send = SpringAppContext.getBean(RmiServiceSend.class);
					Map<String, Object> sendMap = new HashMap<String, Object>();
					sendMap.put("service", "borrowChannelLogService");
					sendMap.put("method", "save");
					sendMap.put("rmiServiceName", AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
					sendMap.put("channelCode", corp);
					sendMap.put("telephone", telephone);
					send.sendExecuteMessage(sendMap);
				}
			} catch (Exception e) {
				LogerUtil.error(getClass(), e, "RmiServiceSend error");
			}
			
			AppParam queryParam = new AppParam("borrowApplyService", "query");
			queryParam.addAttr("telephoneLike", telephone);
			queryParam.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			AppResult qeuryResult = RemoteInvoke.getInstance().call(queryParam);
			if(qeuryResult.isSuccess()){
				responseParam.put("returnCode", "000");
				responseParam.put("errorMessage", "调用成功");
				if (qeuryResult.getRows().size() > 0) {
					isRepeat = true;
				}
			}else{
				responseParam.put("returnCode", "004");
				responseParam.put("errorMessage", "接口调用失败");
			}
			responseParam.put("isRepeat", isRepeat);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "thirdData error！");
			responseParam.put("returnCode", "005");
			responseParam.put("errorMessage", "其他异常");
		}
		return responseParam;
	}
	
	
	/**
	 * 根据code查询城市名称
	 * @param cityCode
	 * @return
	 */
	private Object queryCityName(Object cityCode){
		List<Map<String,Object>> listAll = AreaUtils.getAllInfo();
		if(StringUtils.isEmpty(cityCode)){
			return null;
		}
		for(Map<String,Object> map:listAll){
			if(cityCode.toString().equals(map.get("cityCode"))){
				return map.get("cityName");
			}
		}
		return null;
	}
	
	/**
	 * 根据code查询城市名称
	 * @param cityCode
	 * @return
	 */
	private Object queryCityCode(Object cityName){
		if(StringUtils.isEmpty(cityName)){
			return null;
		}
		List<Map<String,Object>> listAll = AreaUtils.getAllInfo();
		for(Map<String,Object> map:listAll){
			if(cityName.toString().equals(map.get("cityName")))
			return map.get("cityCode");
		}
		
		return null;
	}
	
	@SuppressWarnings("unchecked")
	@RequestMapping("batchApplyInsert/{corp}")
	@ResponseBody
	public Map<String, Object> batchApplyInsert (@RequestBody String body, @PathVariable("corp") String corp, HttpServletRequest request, HttpServletResponse response) {
		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");
		HashMap<String, Object> responseParam = new HashMap<String, Object>();
		try {
			Map<String, Object> context = JsonUtil.getInstance().json2Object(body, Map.class);
			if(StringUtils.isEmpty(corp) || context == null || context.isEmpty() || context.get("applyList") == null){
				responseParam.put("returnCode", "001");
				responseParam.put("returnMessage", "缺少必要参数");
				return responseParam;
			}
			LogerUtil.log(CooperOrgAction.class, "json :" + context.toString());
			Map<String,Object> channel = BorrowChannelUtil.getChannelByCode(corp);
			String channels = SysParamsUtil.getStringParamByKey("test_data_channel", "cccj,ccdx,aijjk,soho");
			if(channel == null || channel.isEmpty() || !channels.contains(corp)){
				responseParam.put("returnCode", "002");
				responseParam.put("returnMessage", "渠道不正确!");
				return responseParam;
			}
			
			List<Map<String, Object>> applyList = (List<Map<String, Object>>) context.get("applyList");
			LogerUtil.log(corp + " applyList size:" + applyList.size());
			int suc = 0;
			int err = 0;
			for (Map<String, Object> apply : applyList) {
				String telephone = StringUtil.objectToStr(apply.get("telephone"));
				if (StringUtils.isEmpty(telephone) || !ValidUtils.validateTelephone(telephone)) {
					err++;
					continue;
				}
				boolean isRepeat = false;
				AppParam applyParam = new AppParam("thirdDataService", "save");
				applyParam.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
				applyParam.addAttr("sourceChannel", corp);
				applyParam.addAttr("channelDetail", corp);
				applyParam.addAttrs(getRandomAsset());
				applyParam.addAttrs(apply);
				
				String channelDetail = StringUtil.objectToStr(apply.get("channelDetail"));
				if (channelDetail.indexOf(corp) == -1) {
					applyParam.addAttr("channelDetail", corp);
				}
				
				applyParam.addAttr("params", apply.toString());
				if (StringUtils.isEmpty(applyParam.getAttr("cityName"))) {
					applyParam.addAttr("cityName", MapLocaltionUtil.getTelInfo(telephone).get("cityName"));;
				}
				/*Map<String, Object> queryMap = BorrowApplyUtils.queryApplyInfo(null, telephone);
				if(queryMap != null){
					isRepeat = true;
				}*/
				//渠道去重处理
				isRepeat = isThirdRepeat(telephone, corp);
				
				applyParam.addAttr("pageReferer", corp);
				applyParam.addAttr("isRepeat", isRepeat ? "1" : "0" );
				applyParam.addAttr("nowOrderAllotStatus", "0");//不立即分单
				AppResult saveResult = RemoteInvoke.getInstance().call(applyParam);
				if(saveResult.isSuccess()){
					if(isRepeat){
						err++;
					}else{
						suc++;
					}
				}else{
					err++;
				}
			}
			responseParam.put("returnCode", "000");
			responseParam.put("returnMessage", "调用成功");
			responseParam.put("suc", suc);
			responseParam.put("err", err);
			LogerUtil.log("response:" + responseParam.toString());
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "queryAppStatus error！");
			responseParam.put("returnCode", "003");
			responseParam.put("returnMessage", "其他异常");
		}
		return responseParam;
	}
	
	private static final int[] incomes = new int[]{3000,5000,10000};//1500:2千以下 3000:2千-4千 5000:4千-8千 10000:8千-1.5万 15000:1.5万以上
	
	private static Map<String, Object> getRandomAsset() {
		Random r = new Random();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("loanAmount", (r.nextInt(27)+3));//3-30万之间
		map.put("workType", 4);//默认工薪族
		map.put("wagesType", 1);
		map.put("income", incomes[r.nextInt(3)]);
		map.put("socialType", (r.nextInt(2) + 1));
		map.put("fundType", (r.nextInt(2) + 1));
		map.put("houseType", (r.nextInt(4) + 1));
		map.put("carType", (r.nextInt(4) + 1));
		map.put("creditType", (r.nextInt(5) + 1));
		map.put("insurType", r.nextInt(3));
		map.put("applyIp", DuoduoSession.getClientIp());
		return map;
	}
	
	@RequestMapping("/queryNetCitys")
	@ResponseBody
	public Map<String, Object> queryNetCitys (HttpServletRequest request, HttpServletResponse response) {
		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");
		HashMap<String, Object> responseParam = new HashMap<String, Object>();
		String formSign = request.getParameter("sign");
		String key = SysParamsUtil.getStringParamByKey("import_sign_key", "import");
		String sign = Md5.getInstance().encrypt(key);
		try {
			if (sign.equals(formSign)) {
				String citys = SysParamsUtil.getStringParamByKey("third_test_data_netCitys", "深圳市,上海市,东莞市,北京市,苏州市,广州市,杭州市,成都市,珠海市,中山市");
				responseParam.put("returnCode", "000");
				responseParam.put("configCitys", citys);
				responseParam.put("returnMessage", "调用成功");
			}else {
				responseParam.put("returnCode", "001");
				responseParam.put("returnMessage", "加密错误!");
			}
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "queryNetCitys error！");
			responseParam.put("returnCode", "003");
			responseParam.put("returnMessage", "其他异常");
		}
		return responseParam;
	}
	
	
	
	
	/**
	 * 第三方数据接收，加密方式
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping("insertData/{corp}")
	@ResponseBody
	public Map<String, Object> insertData(@PathVariable("corp") String corp,
			@RequestBody String body, HttpServletRequest request){
		HashMap<String, Object> responseParam = new HashMap<String, Object>();
		try{
			Map<String,Object> channel = BorrowChannelUtil.getChannelByCode(corp);
			if(channel == null || channel.isEmpty()){
				responseParam.put("returnCode", "0020");
				responseParam.put("errorMessage", "渠道不存在");
				return responseParam;
			}
			
			AppResult checkChannel = ThirdDataUtil.isCheckChannel(corp);
			if (!checkChannel.isSuccess()) {
				responseParam.put("returnCode", checkChannel.getErrorCode());
				responseParam.put("errorMessage", checkChannel.getMessage());
				return responseParam;
			}
			
			if(StringUtils.isEmpty(body)){
				responseParam.put("returnCode", "001");
				responseParam.put("errorMessage", "缺少必要参数");
				return responseParam;
			}
			
			Map<String, Object> data = null;
			try {
				data = JsonUtil.getInstance().json2Object(body, Map.class);
			} catch (Exception e) {
				log.error("json2Object error"+ e.getMessage());
			}
			if(data == null || data.isEmpty()){
				responseParam.put("returnCode", "001");
				responseParam.put("errorMessage", "缺少必要参数");
				return responseParam;
			}
			
			String key = StringUtil.getString(channel.get("merchId"));
			String decrypt = null;
			try {
				decrypt = AESUtil.decrypt(StringUtil.objectToStr(data.get("sign")), key);
			} catch (Exception e) {
				log.error("decrypt error"+ e.getMessage());
			}
			
			log.info("encrypt data :" + decrypt);
			if(StringUtils.isEmpty(decrypt)){
				responseParam.put("returnCode", "002");
				responseParam.put("errorMessage", "签名有误");
				return responseParam;
			}
			Map<String, Object> params = JsonUtil.getInstance().json2Object(decrypt, Map.class);
			String thirdData = params.toString();
			log.info(corp + " third data:" + params.toString());
			if(StringUtils.isEmpty(params.get("telephone"))
					|| StringUtils.isEmpty(params.get("applyName"))
					|| StringUtils.isEmpty(params.get("loanAmount"))
					|| StringUtils.isEmpty(params.get("cityName"))
					|| StringUtils.isEmpty(params.get("applyIp"))
					|| StringUtils.isEmpty(params.get("haveWeiLi"))){
				responseParam.put("returnCode", "001");
				responseParam.put("errorMessage", "缺少必要参数");
				return responseParam;
			}
			
			// 验证手机号
			if (!ValidUtils.validateTelephone(StringUtil.objectToStr(params.get("telephone")))) {
				responseParam.put("returnCode", "009");
				responseParam.put("errorMessage", "手机号格式不正确!");
				return responseParam;
			}
			
			Double loanAmount = Double.valueOf(params.get("loanAmount").toString());
			int maxLoanAmount = SysParamsUtil.getIntParamByKey("thirdMaxLoanAmount", 1000);
			int defaultLoanAmount = SysParamsUtil.getIntParamByKey("thirdDefaultLoanAmount", 1000);
			
			if (loanAmount > maxLoanAmount) {//大于第三方配置最大的贷款金给一个默认的
				params.put("loanAmount", defaultLoanAmount);
			}
			// 查找 深圳
			//判断城市是否查找，存在传入数据库的参数，不存在
			Object cityCode = this.queryCityCode(params.get("cityName"));
			if (cityCode == null) {
				responseParam.put("returnCode", "008");
				responseParam.put("errorMessage", "城市：" + params.get("cityName") + "未找到对应的城市");
				return responseParam;
			}
			boolean isRepeat = false;
			boolean isHandling = false;// 是否正在处理中
			String telephone = StringUtil.objectToStr(params.get("telephone"));
			/*Map<String, Object> queryMap = BorrowApplyUtils.queryApplyInfo(null, telephone);
			if(queryMap != null){
				isRepeat = true;
			}*/
			//渠道去重处理
			isRepeat = isThirdRepeat(telephone, corp);
			// 插入数据到中间表
			AppParam applyParam = new AppParam();
			applyParam.setService("thirdDataService");
			applyParam.setMethod("save");
			applyParam.addAttr("channelDetail", corp);
			applyParam.addAttr("params", thirdData);
			ThirdDataUtil.getAge(StringUtil.getString(params.get("birthday")), params);
            applyParam.addAttr("applyIp", params.get("applyIp"));
			applyParam.addAttr("isRepeat", isRepeat ? "1" : "0" );
			applyParam.addAttr("isHandling", isHandling ? "1" : "0");
			applyParam.addAttr("pageReferer", corp);
			applyParam.addAttr("sourceChannel", corp);
			applyParam.addAttr("mediaSource", params.get("mediaSource"));
			applyParam.addAttrs(params);
			if (StringUtils.isEmpty(applyParam.getAttr("channelDetail"))) {
				applyParam.addAttr("channelDetail", corp);
			}
			applyParam.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			if (!StringUtils.isEmpty(applyParam.getAttr("carType"))) {
				int carType = Integer.valueOf(params.get("carType").toString());
				if (carType == 0) {
					applyParam.addAttr("carType", "2");
				}
			}
			boolean randomRepeat = false;
			int deduction = NumberUtil.getInt(checkChannel.getAttr("deduction"));
			if (deduction > 0 && !isRepeat) {
				randomRepeat = ThirdDataUtil.randomRepeat(deduction, corp, StringUtil.objectToStr(checkChannel.getAttr("randoms")));
			}
			if (randomRepeat) {
				//再保存改变了渠道的数据
				applyParam.addAttr("channelDetail", applyParam.getAttr("channelDetail") + "_sale");
			}
			AppResult saveResult = RemoteInvoke.getInstance().call(applyParam);
			if(saveResult.isSuccess()){
				if(isRepeat || randomRepeat){
					responseParam.put("returnCode", "003");
					responseParam.put("errorMessage", "申请重复");
					responseParam.put("telephone", telephone);
				}else{
					responseParam.put("unionId", saveResult.getAttr("unionId"));
					responseParam.put("returnCode", "000");
					responseParam.put("errorMessage", "接收成功");
				}
			}else{
				if (!StringUtils.isEmpty(saveResult.getErrorCode()) && "003".equals(saveResult.getErrorCode())) {
					responseParam.put("returnCode", "003");
					responseParam.put("errorMessage", "申请重复");
					responseParam.put("telephone", telephone);
				}else {
					responseParam.put("returnCode", "004");
					responseParam.put("errorMessage", "接收异常");
				}
			}
		}catch(Exception e){
			LogerUtil.error(this.getClass(), e, "insertData error！");
			responseParam.put("returnCode", "005");
			responseParam.put("errorMessage", "其他异常");
		}
		finally{
			LogerUtil.log("response:" + responseParam.toString());
		}
		return responseParam;
	}
	
	/**
	 * 判断某个渠道是否去重
	 * @param telephone
	 * @param channelCode
	 * @return true 重复 false 非重复
	 */
	public static boolean isThirdRepeat(String telephone, String channelCode){
		boolean isRepeat = false;
		String notForeverRepeatChannels = SysParamsUtil.getStringParamByKey("notForeverRepeatChannels", "lnde,");
		if (notForeverRepeatChannels.contains(channelCode)) {//判断是不是非永久去重
			String repeat1MonthChannels = SysParamsUtil.getStringParamByKey("third_data_repeat1Month_channels", "testApi,");
			if (repeat1MonthChannels.contains(channelCode)) {//判断需不需要和落地页一样的一个月去重方法
				String canApply = ApplyUnionUtil.isCanApply(telephone);
				isRepeat = (!"1,4,".contains(canApply));
			}
		} else {
			Map<String, Object> queryMap = BorrowApplyUtils.queryApplyInfo(null, telephone);
			if(queryMap != null){
				isRepeat = true;
			}
		}
		return isRepeat;
	}
	
	
	
	public static void main(String[] args) throws Exception {
		// testApi 24d0b0e74f220304
//		System.out.println(Md5.getInstance().encrypt("185****5851" + "_" + "20180816808173000" + "_" + "用户" +  "_" + "saleShandai11018123_5648667"));
//		System.out.println(CustomerUtil.getEncrypt("1234556"));
//		String substring = Md5.getInstance().encrypt("testApi10180").substring(0, 16);
//		System.out.println(substring + "    " + substring.length());
		//time=20180420101756&sign=不需要加密&telephone=15812343359&applyName=mxx&sex=1&loanAmount=12&cityName=深圳市&workType=4
		//&socialType=1&fundType=1&houseType=3&carType=3&creditType=4&income=30000&wagesType=1&insurType=1&applyIp=127.0.0.1&
		//age=20&haveWeiLi=0
		Map<String, Object> msg = new HashMap<String, Object>();
		msg.put("telephone", "15812343360");
		msg.put("applyName", "mxx");
		msg.put("sex", "1");
		msg.put("loanAmount", "12");
		msg.put("cityName", "深圳市");
		msg.put("workType", "4");
		msg.put("socialType", "1");
		msg.put("fundType", "1");
		msg.put("houseType", "3");
		msg.put("carType", "3");
		msg.put("creditType", "4");
		msg.put("income", "30000");
		msg.put("wagesType", "1");
		msg.put("insurType", "1");
		msg.put("applyIp", "127.0.0.1");
		msg.put("haveWeiLi", "0");
		msg.put("age", "2");
		String object2json = JsonUtil.getInstance().object2JSON(msg);
		String encrypt = AESUtil.encrypt(object2json, "24d0b0e74f220304");
		System.out.println(encrypt);
	}
}
