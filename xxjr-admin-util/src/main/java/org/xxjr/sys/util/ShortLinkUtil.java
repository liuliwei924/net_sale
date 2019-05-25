package org.xxjr.sys.util;

import java.io.Serializable;
import java.util.Map;

import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.SpringAppContext;
import org.ddq.common.core.service.RemoteInvoke;
import org.ddq.common.core.service.SoaManager;
import org.llw.model.cache.RedisUtils;

public class ShortLinkUtil {
	//public static final String SHORT_LINK = "short_link";
	//原来key值存的是string类型数据 现在key值存的是map，预防转换报错，修改key
	public static final String SHORT_LINK = "short_links_";
	
	
	/**
	 * 获取短链接
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Map<String,Object> getShortLink(String linkName){
		Map<String,Object> urlMap =  (Map<String, Object>) RedisUtils.getRedisService().get(SHORT_LINK + linkName);
		if (urlMap == null) {
			urlMap = refreshShortLink(linkName);
		}
		return urlMap;
	}

	public static Map<String,Object> refreshShortLink(String linkName) {
		AppParam param = new AppParam();
		param.setService("shortLinkService");
		param.setMethod("query");
		param.addAttr("linkName", linkName);
		AppResult result = null;
		Map<String,Object> urlMap = null;
		if (SpringAppContext.getBean("shortLinkService") == null) {
			result = RemoteInvoke.getInstance().call(param);
		}else{
			result = SoaManager.getInstance().invoke(param);
		}
		if (result.getRows().size() > 0) {
			urlMap =  result.getRow(0);
			RedisUtils.getRedisService().set(SHORT_LINK + linkName, (Serializable)urlMap,60 * 60 * 24);
		}
		return urlMap;
	}
}
