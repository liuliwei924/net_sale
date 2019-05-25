package org.xxjr.busi.util.kefu;

import java.util.HashMap;
import java.util.Map;

import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.xxjr.sys.util.ServiceKey;

public class ExportCfgUtil {
	/**
	 * 查询导出配置信息
	 * @param exportType
	 * @return
	 * @throws Exception
	 */
	public static Map<String, Object> queryExportCfg(String exportType) {
		AppParam param = new AppParam("exportCfgService","query");
		param.addAttr("exportType", exportType);
		AppResult result = ServiceKey.doCallNoTx(param, ServiceKey.Key_sum);
		if (result.getRows().size() > 0) {
			return result.getRow(0);
		}
		return new HashMap<String, Object>();
	}
}
