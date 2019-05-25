package org.ddq.active.mq.file;

import java.util.Map;
import org.ddq.common.core.SpringAppContext;

public class FileMessageUtil {
	
	public static Boolean activity = true;
	public static long SleepActiveTime = 10*1000;
	
	
	/***
	 * 发送消息到活动引擎
	 * @param map 执行参数
	 */
	public static void sendMessage(Map<String,Object> map){
		FileMessageSend send = SpringAppContext.getBean(FileMessageSend.class);
		send.sendInfo(map);
	}
}
