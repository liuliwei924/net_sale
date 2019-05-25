package org.xxjr.busiIn.kf.ext;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.llw.common.core.service.BaseService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Lazy
@Service
public class ChannelMsSectionSumService extends BaseService {
	
	public static final String NAMESPACE = "CHANNELMSSECTIONSUM";
	
	/**大渠道基本情况统计(跟进后)
	 * channelBase
	 * @param params
	 * @return
	 */
	public AppResult channelBase(AppParam params) {
		return super.queryByPage(params, NAMESPACE, "channelBase", "channelBaseCount");
	}
	
	
	/**金额资质详细情况统计(跟进后)
	 * channelDtl
	 * @param params
	 * @return
	 */
	public AppResult channelDtl(AppParam params) {
		return super.queryByPage(params, NAMESPACE, "channelDtl", "channelBaseCount");
	}
	
	
	/**线索流向情况统计(跟进后)
	 * channelSale
	 * @param params
	 * @return
	 */
	public AppResult channelSale(AppParam params) {
		return super.queryByPage(params, NAMESPACE, "channelSale", "channelBaseCount");
	}
	
	
	/**ROI情况统计(跟进后)
	 * channelROI
	 * @param params
	 * @return
	 */
	public AppResult channelROI(AppParam params) {
		return super.queryByPage(params, NAMESPACE, "channelROI", "channelBaseCount");
	}
	
	/**网销门店情况统计(跟进后)
	 * channelNet
	 * @param params
	 * @return
	 */
	public AppResult channelNet (AppParam param) {
		return super.queryByPage(param, NAMESPACE, "channelNet", "channelBaseCount");
	}
	
	/**
	 * 共用的数量查询sql
	 * @param params
	 * @return
	 */
	public AppResult channelBaseCount(AppParam params) {
		int size = getDao().count(NAMESPACE, "channelBaseCount",params.getAttr(),params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
		return result;
	}
	
	
	/**
	 * 共用的数量查询sql(原始的)
	 * @param params
	 * @return
	 */
	public AppResult channelOrigBaseCount(AppParam params) {
		int size = getDao().count(NAMESPACE, "channelOrigBaseCount",params.getAttr(),params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
		return result;
	}
	
	/**大渠道基本情况统计(原始的)
	 * channelBase
	 * @param params
	 * @return
	 */
	public AppResult channelOrigBase(AppParam params) {
		return super.queryByPage(params, NAMESPACE, "channelOrigBase", "channelOrigBaseCount");
	}
	
	
	/**金额资质详细情况统计(原始的)
	 * channelDtl
	 * @param params
	 * @return
	 */
	public AppResult channelOrigDtl(AppParam params) {
		return super.queryByPage(params, NAMESPACE, "channelOrigDtl", "channelOrigBaseCount");
	}
	/**城市统计
	 * citySumary
	 * @param params
	 * @return
	 */
	public AppResult citySumary(AppParam params) {
		return super.queryByPage(params, NAMESPACE, "citySumary", "citySumaryCount");
	}
	/**网销（实时）
	 * citySumary
	 * @param params
	 * @return
	 */
	public AppResult channelNetReal(AppParam params) {
		return super.queryByPage(params, NAMESPACE, "channelNetReal", "channelNetRealCount");
	}
	
	/**
	 * 网销（实时）
	 * @param params
	 * @return
	 */
	public AppResult channelNetRealCount(AppParam params) {
		int size = getDao().count(NAMESPACE, "channelNetRealCount",params.getAttr(),params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
		return result;
	}
	
	/**=========================渠道城市情况统计(按实际日期)====================**/
	/**渠道城市情况统计(跟进后)
	 * channelCity
	 * @param params
	 * @return
	 */
	public AppResult realChannelCitySec(AppParam params) {
		return super.queryByPage(params, NAMESPACE, "realChannelCitySec", "realChannelCitySecCount");
	}
	
	/**渠道城市情况统计(跟进后-本月-月度)
	 * channelCityDate
	 * @param params
	 * @return
	 */
	public AppResult realChannelCityDate(AppParam params) {
		return super.queryByPage(params, NAMESPACE, "realChannelCityDate", "realChannelCityDateCount");
	}
	
	/**
	 * 渠道城市情况统计(跟进后-本月-月度)
	 * @param params
	 * @return
	 */
	public AppResult realChannelCityDateCount(AppParam params) {
		int size = getDao().count(NAMESPACE, "realChannelCityDateCount",params.getAttr(),params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
		return result;
	}
	/**
	 * 计数(渠道城市情况统计)
	 * @param params
	 * @return
	 */
	public AppResult realChannelCitySecCount(AppParam params) {
		int size = getDao().count(NAMESPACE, "realChannelCitySecCount",params.getAttr(),params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
		return result;
	}
	
	
	
}
