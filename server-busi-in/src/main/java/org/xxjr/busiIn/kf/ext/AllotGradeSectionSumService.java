package org.xxjr.busiIn.kf.ext;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.llw.common.core.service.BaseService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Lazy
@Service
public class AllotGradeSectionSumService extends BaseService {
	
	public static final String NAMESPACE = "ALLOTGRADESECTIONSUM";
	
	/**日期  城市 等级 去向
	 * allotCity
	 * @param params
	 * @return
	 */
	public AppResult allotCity(AppParam params) {
		return super.queryByPage(params, NAMESPACE, "allotCity", "allotCityCount");
	}
	
	
	/**日期 渠道 等级 （ABCDEF） 去向
	 * allotChannelCode
	 * @param params
	 * @return
	 */
	public AppResult allotChannelCode(AppParam params) {
		return super.queryByPage(params, NAMESPACE, "allotChannelCode", "allotChannelCodeCount");
	}
	
	
	/**日期  等级 （ABCDEF） 去向
	 * allotGrade
	 * @param params
	 * @return
	 */
	public AppResult allotGrade(AppParam params) {
		return super.queryByPage(params, NAMESPACE, "allotGrade", "allotGradeCount");
	}
	
	/**城市
	 * 
	 * @param params
	 * @return
	 */
	public AppResult allotCityCount(AppParam params) {
		int size = getDao().count(NAMESPACE, "allotCityCount",params.getAttr(),params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
		return result;
	}
	/**渠道
	 * 
	 * @param params
	 * @return
	 */
	public AppResult allotChannelCodeCount(AppParam params) {
		int size = getDao().count(NAMESPACE, "allotChannelCodeCount",params.getAttr(),params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
		return result;
	}
	/**等级
	 * 
	 * @param params
	 * @return
	 */
	public AppResult allotGradeCount(AppParam params) {
		int size = getDao().count(NAMESPACE, "allotGradeCount",params.getAttr(),params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
		return result;
	}
}
