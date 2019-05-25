package org.xxjr.busi.util;

import java.util.Map;

@FunctionalInterface
public interface ThirdSaleUtil {
	public String getTelByOrderId(String orderId, String applyName, String telephone, Map<String, Object> row);
}
