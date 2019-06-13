package org.xxjr.store.web.action.common;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.service.RemoteInvoke;
import org.ddq.common.security.MD5Util;
import org.ddq.common.security.md5.Md5;
import org.ddq.common.util.LogerUtil;
import org.ddq.common.util.StringUtil;
import org.ddq.common.web.session.RequestUtil;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.xxjr.busi.util.ApplyUnionUtil;
import org.xxjr.busi.util.BorrowChannelUtil;
import org.xxjr.busi.util.ThirdDataUtil;
import org.xxjr.busi.util.kf.BorrowApplyUtils;
import org.xxjr.sys.util.AreaUtils;
import org.xxjr.sys.util.NumberUtil;
import org.xxjr.sys.util.ServiceKey;
import org.xxjr.sys.util.SysParamsUtil;
import org.xxjr.sys.util.ValidUtils;

@Controller
@RequestMapping("/cooper/org/")
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
			AppParam qryParam = new AppParam();
			qryParam.addAttr("telephone", telephone);
			int count = BorrowApplyUtils.queryCount(qryParam);
			if(count > 0){
				isRepeat = true;
			}
		}
		return isRepeat;
	}
	
	
	@RequestMapping("/queryTelByMd5")
	@ResponseBody
	public  AppResult queryTelByMd5(HttpServletRequest request){
		AppResult result  = new AppResult();
		String unionId = request.getParameter("unionId");
		String channelCode = request.getParameter("channelCode");
		
		if(StringUtils.isEmpty(unionId) || StringUtils.isEmpty(channelCode)) {
			result.setSuccess(false);
			result.setMessage("缺少必要参数【unionId，channelCode 两者必传】");
			return result;
		}
		AppResult resultTmp = ThirdDataUtil.isCheckChannel(channelCode);
		if (resultTmp.isSuccess()) {
			AppParam queryParams = new AppParam();
			queryParams.addAttr("unionId", unionId);
			int count = BorrowApplyUtils.queryCount(queryParams);
			
			if(count > 0) {
				result.setSuccess(false);
				result.setMessage("此号码库中已存在");
			}
		}else {
			result.setErrorCode(resultTmp.getErrorCode());
			result.setMessage(resultTmp.getMessage());
			result.setSuccess(false);
		}
		
		return result;
	}
}
