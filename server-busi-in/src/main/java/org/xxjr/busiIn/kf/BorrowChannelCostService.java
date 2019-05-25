package org.xxjr.busiIn.kf;

import java.util.Date;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.ddq.common.exception.AppException;
import org.ddq.common.exception.DuoduoError;
import org.llw.common.core.service.BaseService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Lazy
@Service
@Slf4j
public class BorrowChannelCostService extends BaseService {
	private static final String NAMESPACE = "BORROWCHANNELCOST";

	/**
	 * querys
	 * @param params
	 * @return
	 */
	public AppResult query(AppParam params) {
		return super.query(params, NAMESPACE);
	}
	
	/**
	 * queryByPage
	 * @param params
	 * @return
	 */
	public AppResult queryByPage(AppParam params) {
		return super.queryByPage(params, NAMESPACE);
	}
	
	/**
	 * queryCount
	 * @param params
	 * @return
	 */
	public AppResult queryCount(AppParam params) {
		int size = getDao().count(NAMESPACE, super.COUNT,params.getAttr(),params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
		return result;
	}
	
	
	/**
	 * insert
	 * @param params
	 * @return
	 */
	public AppResult insert(AppParam params) {
		params.addAttr("createTime", new Date());
		return super.insert(params, NAMESPACE);
	}
	
	/**
	 * batchEdit
	 * @param params
	 * @return
	 */
	public AppResult batchEdit(AppParam params) {
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> channelCostList = (List<Map<String, Object>>)params.getAttr("channelCostList");
		if(channelCostList != null && !channelCostList.isEmpty()){
			for(Map<String, Object> map : channelCostList){
				try{
					if(StringUtils.isEmpty(map.get("recordDate")) || StringUtils.isEmpty(map.get("channelCode"))){
						continue;
					}
					map.put("createBy", params.getAttr("createBy"));
					AppParam params1 = new AppParam();
					params1.addAttrs(map);
					this.update(params1);
				
				}catch(Exception e){
					log.error("批量编辑渠道花费失败!,params=" + map.toString(), e);
				}
			}
		}
		return new AppResult();
	}
	
	/**
	 * update
	 * @param params
	 * @return
	 */
	public AppResult update(AppParam params) {
		params.addAttr("updateTime", new Date());
		AppResult updateResult = super.update(params, NAMESPACE);
		int count  = (Integer)updateResult.getAttr(DuoduoConstant.DAO_Update_SIZE);
		if(count <= 0){
			updateResult = this.insert(params);
		}
		return updateResult;
	}
	
	/**更新结算金额,点击量
	 * updateSucRetAmount
	 * @param params
	 * @return
	 */
	public AppResult updateCostAmt(AppParam params) {
		int size = getDao().update(NAMESPACE, "updateCostAmt", params.getAttr(), params.getDataBase());
		AppResult backContext = new AppResult();
		backContext.putAttr(DuoduoConstant.DAO_Update_SIZE, size);
		return backContext;
	}
	
	
	/**
	 * delete
	 * @param params
	 * @return
	 */
	public AppResult delete(AppParam params) {
		Object recordDate = params.getAttr("recordDate");
		Object channelCode = params.getAttr("channelCode");
		if(StringUtils.isEmpty(recordDate) || StringUtils.isEmpty(channelCode)){
			throw new AppException(DuoduoError.DELETE_NO_ID);
		}
		return super.delete(params, NAMESPACE);
	}
	/**
	 * batchImportData
	 * @param params
	 * @return
	 */
	public AppResult batchImportData(AppParam params) {
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> dataList = (List<Map<String, Object>>) params.getAttr("dataList");
		StringBuffer sb = new StringBuffer();
		AppResult result = new AppResult();
		for (Map<String, Object> dataMap : dataList) {
			try{
				AppParam appParam = new AppParam();
				appParam.addAttrs(dataMap);
				this.insert(appParam);
			}catch(Exception e){
				String errStr = "[渠道：" + dataMap.get("channelCode").toString() + ",日期：" + dataMap.get("recordDate").toString()+"]";
				
				sb.append(errStr).append(",");
			}
		}
		
		if(StringUtils.hasText(sb.toString())){
			result.setMessage(sb.toString()+"以上数据导入不成功，可能已经库中已存在!");
			result.setSuccess(false);
		}
		return result;
	}
}
