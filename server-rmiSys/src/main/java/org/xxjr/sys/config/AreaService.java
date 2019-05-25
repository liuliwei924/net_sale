package org.xxjr.sys.config;


import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.llw.common.core.service.BaseService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;;

@Lazy
@Service
public class AreaService extends BaseService {
	private static final String NAMESPACE = "AREA";

	public AppResult query(AppParam context) {
		return super.query(context, NAMESPACE);
	}
	
	public AppResult queryByPage(AppParam context) {
		return super.queryByPage(context, NAMESPACE);
	}
	
	public AppResult queryAreaShow(AppParam context) {
		return super.query(context, NAMESPACE, "queryAreaShow");
	}
	
	/***
	 * 根据树形结构查寻
	 * @param context
	 * @return
	 */
	public AppResult querySelectTree(AppParam context) {
		return super.query(context, NAMESPACE, "querySelectTree");
	}
	
	/***
	 * 分组查寻省份信息
	 * @param context
	 * @return
	 */
	public AppResult queryProvice(AppParam context) {
		return super.query(context, NAMESPACE, "queryProvice");
	}
	
	/**
	 * 查询所有信息(包括省份、城市、区域)
	 * @param context
	 * @return
	 */
	public AppResult queryAllInfo(AppParam context) {
		return super.query(context, NAMESPACE, "queryAllInfo");
	}
	
	/***
	 * 分组查寻城市信息
	 * @param context
	 * @return
	 */
	public AppResult queryGroupCity(AppParam context) {
		return super.query(context, NAMESPACE, "queryGroupCity");
	}
	
	/***
	 * 分组查寻城市的区域信息
	 * @param context
	 * @return
	 */
	public AppResult queryGroupDistrict(AppParam context) {
		return super.query(context, NAMESPACE, "queryGroupDistrict");
	}
	
	
	public AppResult queryCount(AppParam context) {
		int size = getDao().count(NAMESPACE, super.COUNT,context.getAttr(),context.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
		return result;
	}
	
	/***
	 * 查寻区域
	 * @param paramInfo
	 * @return
	 */
	public AppResult queryTreeArea(AppParam paramInfo) {
		if(paramInfo.getAttr("loanId")==null){
			//throw new AppException(DuoduoError.UPDATE_PARAM_NO_FIEDS,new Object[]{"loanId"});
		}
		AppResult result = super.query(paramInfo, NAMESPACE, "queryTreeArea");
		return result;
	} 
	
	public AppResult queryProvinceByCity (AppParam params){
		AppResult result = new AppResult();
		AppResult queryResult = this.query(params, NAMESPACE, "queryProvinceByCity");
		if (queryResult.getRows().size() > 0) {
			result.putAttr("province", queryResult.getRow(0).get("nameCn"));
		}
		return result;
	}
}
