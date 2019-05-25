package org.ddq.active.mq.message;

import java.util.HashMap;
import java.util.Map;

import org.ddq.active.mq.MQNames;
import org.ddq.active.mq.XxjrMqSendUtil;
import org.ddq.common.context.AppParam;
import org.ddq.common.util.JsonUtil;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * 客服系统导出异步处理
 * @author LHS
 *
 */

@Lazy
@Component
public class ExportSend {

    /**
	 * 请求实际导出方法
	 * @param type
	 * @param customerId
	 * @param coinValue
	 * @param typeId
	 */
	public void export(AppParam params){
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("params", JsonUtil.getInstance().object2JSON(params));
		this.sendInfo(map);
	}
    
    
    
	private void sendInfo(final Map<String,Object> map) {
		XxjrMqSendUtil.sendMessage(map, MQNames.export);
    }
	
}
