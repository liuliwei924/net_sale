package org.xxjr.busiIn.utils;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.service.SoaManager;
import org.ddq.common.util.DateUtil;
import org.ddq.common.util.StringUtil;
import org.springframework.util.StringUtils;
import org.xxjr.busi.util.StoreSeparateUtils;
import org.xxjr.store.util.StoreApplyUtils;
import org.xxjr.sys.util.NumberUtil;
import org.xxjr.sys.util.OrgUtils;
import org.xxjr.sys.util.SysParamsUtil;

/***
 * 分单处理工具类
 * @author ZQH
 *
 */
@Slf4j
public class StoreAlllotUtils {

	public static boolean compareParam(Map<String,Object> custMap, Map<String,Object> gradeMap){
		if(custMap == null || custMap.size() <=0 || gradeMap == null || gradeMap.size() <=0) {
			return false;
		}
		String maxCount = StringUtil.getString(gradeMap.get("maxCount"));
		String visitCount = StringUtil.getString(gradeMap.get("visitCount"));
		String sucCount = StringUtil.getString(gradeMap.get("sucCount"));
		// 等级要求最大分单数
		int maxCountNum = 0;
		if(maxCount.contains("/")){
			String[] maxCountArr = maxCount.split("/");
			maxCountNum = NumberUtil.getInt(maxCountArr[0],0);
		}else{
			maxCountNum =  NumberUtil.getInt(maxCount,0);
		}
		// 业务员当前总分单数
		int curTotalCountNum = NumberUtil.getInt(custMap.get("allotSeniorCount"),0);
		
		//等级要求最大处理中单数
		int maxDealNum = NumberUtil.getInt(gradeMap.get("dealCount"),0);
		//业务员当前处理中单数
		int curDealCountNum = NumberUtil.getInt(custMap.get("dealOrderCount"),0);
		
		// 等级要求上门数
		int visitNum = 0;
		if(visitCount.contains("/")){
			String[] visitCountArr = visitCount.split("/");
			visitNum = NumberUtil.getInt(visitCountArr[0],0);
		}else{
			visitNum =  NumberUtil.getInt(visitCount,0);
		}
		// 业务员当前上门数
		int curVisitNum = NumberUtil.getInt(custMap.get("visitCount"),0);
		
		// 等级要求签单数
		int sucCountNum = 0;
		if(sucCount.contains("/")){
			String[] sucCountArr = sucCount.split("/");
			sucCountNum = NumberUtil.getInt(sucCountArr[0],0);
		}else{
			sucCountNum =  NumberUtil.getInt(sucCount,0);
		}
		// 业务员当前签单数
		int curSignNum = NumberUtil.getInt(custMap.get("signCount"),0);
		
		// 等级要求再分配单数
		int agAllotCount = NumberUtil.getInt(gradeMap.get("maxAgainCount"),0);
		// 当前再分配数
		int curAgAllotCount =  NumberUtil.getInt(custMap.get("agAllotCount"),0);
		
		// 订单类型
		int orderType = NumberUtil.getInt(custMap.get("orderType"),0);
		if(1 == orderType){
			if(curTotalCountNum >= maxCountNum || ( visitNum > curVisitNum
					|| sucCountNum > curSignNum || curDealCountNum > maxDealNum)){
				return false;
			}
		}else{
			if(curAgAllotCount >= agAllotCount ||  visitNum > curVisitNum
					|| sucCountNum >curSignNum || curDealCountNum > maxDealNum){
				return false;
			}
		}
		
		return true;
	}
	
	/***
	 * 准分单参数比较
	 * @param custMap
	 * @param gradeMap
	 * @return
	 */
	public static boolean compareSureParam(Map<String,Object> custMap, Map<String,Object> gradeMap){
		if(custMap == null || custMap.size() <=0 || gradeMap == null || gradeMap.size() <=0) {
			return false;
		}
		
		String maxCount = StringUtil.getString(gradeMap.get("maxCount"));
		// 等级要求最大分单数
		int maxCountNum = 0;
		if(maxCount.contains("/")){
			String[] maxCountArr = maxCount.split("/");
			maxCountNum = NumberUtil.getInt(maxCountArr[0],0);
		}else{
			maxCountNum =  NumberUtil.getInt(maxCount,0);
		}
		// 业务员当前总分单数
		int curTotalCountNum = NumberUtil.getInt(custMap.get("allotSeniorCount"),0);
		
		// 等级要求再分配单数
		int agAllotCount = NumberUtil.getInt(gradeMap.get("maxAgainCount"),0);
		// 当前再分配数
		int curAgAllotCount =  NumberUtil.getInt(custMap.get("agAllotCount"),0);
		
		// 订单类型
		int orderType = NumberUtil.getInt(custMap.get("orderType"),0);
		if(1 == orderType){
			if(curTotalCountNum > maxCountNum){
				return false;
			}
		}else{
			if(curAgAllotCount > agAllotCount){
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * 更新分单数量
	 * @param customerId
	 * @param recordDate
	 * @param totalSize
	 */
	public static void updateAllotCount(AppParam param){
		String customerId = StringUtil.getString(param.getAttr("customerId"));
		String recordDate = StringUtil.getString(param.getAttr("recordDate"));
		int totalSize = NumberUtil.getInt(param.getAttr("totalSize"));
		int orderType = NumberUtil.getInt(param.getAttr("orderType"));
		//更新分配数量的记录
		AppParam updateParam = new AppParam("storeAllotRecordService","queryCount");
		updateParam.addAttr("customerId", customerId);
		updateParam.addAttr("recordDate", recordDate);
		AppResult updateResult = SoaManager.getInstance().invoke(updateParam);
		int count = NumberUtil.getInt(updateResult.getAttr(DuoduoConstant.TOTAL_SIZE),0);
		if(count > 0){
			updateParam.setMethod("update");
			updateParam.addAttr("addCount", totalSize);
			updateParam.addAttr("addTotalCount", totalSize);
			if(orderType == 1){
				updateParam.addAttr("addAllotSeniorCount", totalSize);
			}else{
				updateParam.addAttr("addAllotNotFillCount", totalSize);
			}
		}else{
			updateParam.setMethod("insert");
			updateParam.addAttr("totalCount", totalSize);
			if(orderType == 1){
				updateParam.addAttr("allotSeniorCount", totalSize);
			}else{
				updateParam.addAttr("allotNotFillCount", totalSize);
			}
		}
		SoaManager.getInstance().invoke(updateParam);
	}
	
	/***
	 * 判断门店是否超量
	 * @param orgId
	 * @return
	 */
	public static boolean isOverAllot(String orgId){
		AppParam queryParam  = new AppParam("borrowStoreRecordService","queryOrgAllotCount");
		queryParam.addAttr("orgId", orgId);
		//测试渠道不占分单总数
		String channelCodeIn = SysParamsUtil.getStringParamByKey("storeTestChannelCode", "'xzccd','xjccd','soho'");
		queryParam.addAttr("channelCodeIn", channelCodeIn);
		AppResult orgResult = SoaManager.getInstance().callNoTx(queryParam);
		if(orgResult.isSuccess()){
			int allotCount = NumberUtil.getInt(orgResult.getAttr(DuoduoConstant.TOTAL_SIZE),0);
			Map<String,Object> orgMap = StoreSeparateUtils.getOrgWorkByOrgId(NumberUtil.getInt(orgId,0));
			if(orgMap != null){
				int orgAllotMaxCount = NumberUtil.getInt(orgMap.get("orgMaxCount"),0);
				if(allotCount > orgAllotMaxCount && orgAllotMaxCount != 0){
					log.info("can't sureAllot : reach the maximum orgId:" + orgId) ;
					return true;
				}
			}
		}
		return false;
	}
	
	/***
	 * 判断订单是否能转信贷经理
	 * @param orgId 转信给贷经理的门店Id
	 * @param userOrgs 登录人管理的门店
	 * @return
	 */
	public static boolean isOrderTransOther(String orgId,String userOrgs){
		if(!StringUtils.isEmpty(userOrgs)){
			if("all".equals(userOrgs)){
				return true;
			}else if(userOrgs.contains(",")){
				String[] orgIdArray = userOrgs.split(",");
				List<String> orgList = Arrays.asList(orgIdArray);
				for(String usrOrgId : orgList){
					if(usrOrgId.equals(orgId)){
						return true;
					}
				}
			}else if(orgId.equals(userOrgs)){
				return true;
			}
		}
		return false;
	}
	
	/***
	 * 查询接单人上次接单时间是否在3分钟之内，是则不立即分单
	 * @param customerId 接单人
	 * @return
	 */
	public static boolean isNowAllotFlag(String customerId){
		if(!StringUtils.isEmpty(customerId)){
			AppParam recordQueryParam = new AppParam("borrowStoreRecordService","queryCustAllotRecord");
			recordQueryParam.addAttr("customerId", customerId);
			AppResult recordResult = SoaManager.getInstance().invoke(recordQueryParam);
			if(recordResult.getRows().size() > 0){
				String createTime = StringUtil.getString(recordResult.getRow(0).get("createTime"));
				String nowTime = DateUtil.toStringByParttern(DateUtil.getNextMinutes(new Date(), -3), 
						DateUtil.DATE_PATTERN_YYYY_MM_DD_HHMMSS);
				int dateCompare = createTime.compareTo(nowTime);
				if(dateCompare > 0){
					return true;
				}
			}
		}
		return false;
	}
	/**
	 * 判断单子是否是异地单
	 * @param customerId
	 * @return
	 */
	public static boolean isNotPlaceFlag(String orgId,String applyId){
		//获取门店城市
		String orgCityName = OrgUtils.getCityNameByOrgId(orgId);
		if(!StringUtils.isEmpty(orgCityName)){
			Map<String,Object> applyMap = StoreApplyUtils.getApplyMainInfo(applyId);
			if(applyMap != null){
				String cityName = StringUtil.getString(applyMap.get("cityName"));
				if(!StringUtils.isEmpty(cityName) && orgCityName.equals(cityName)){
					return true;
				}
			}
		}
		return false;
	}
	/**
	 * 查询分单人员信息(仅对上海市)
	 * @param customerId
	 * @return
	 */
	public static AppResult queryStoreAllotByCity(AppParam storeParams){
		AppResult storeAllotResult = new AppResult();
		//上海市的新申请单不限制门店随机分给上海的各个门店人员
		storeParams.removeAttr("orgId");
		//取出需要分配给业务员的再分配单
		storeParams.setDataBase("main_");
		storeAllotResult = SoaManager.getInstance().invoke(storeParams);
		return storeAllotResult;
	}

}
