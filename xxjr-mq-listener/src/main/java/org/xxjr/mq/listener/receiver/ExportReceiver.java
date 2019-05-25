package org.xxjr.mq.listener.receiver;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Map;

import javax.annotation.PreDestroy;

import org.apache.commons.io.FileUtils;
import org.ddq.active.mq.XxjrMqSendUtil;
import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.core.SpringAppContext;
import org.ddq.common.core.service.RemoteInvoke;
import org.ddq.common.util.DateUtil;
import org.ddq.common.util.JsonUtil;
import org.ddq.common.util.LogerUtil;
import org.ddq.common.util.StringUtil;
import org.llw.mq.rabbitmq.RabbitMqConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.xxjr.busi.util.kefu.KfExportUtil;
import org.xxjr.mq.listener.consumer.RabbitMqConsumer;
import org.xxjr.mq.listener.util.XxjrInitAnnotation;
import org.xxjr.sys.util.NumberUtil;
import org.xxjr.sys.util.ServiceKey;
import org.xxjr.sys.util.ThreadLogUtil;

import lombok.extern.slf4j.Slf4j;

@Component
@XxjrInitAnnotation(beanName="exportReceiver",initMethod="init")
@Slf4j
public class ExportReceiver extends RabbitMqConsumer{
	
	@Autowired
	private RabbitMqConfig rabbitMqConfig;
	
	private static ThreadPoolTaskExecutor taskExecutor;

	public static ThreadPoolTaskExecutor getInstance(){
		if(taskExecutor == null){
			synchronized (ThreadLogUtil.class) {
				if(taskExecutor == null){
					taskExecutor = SpringAppContext.getApplicationContext().getBean(ThreadPoolTaskExecutor.class);
				}
			}
		}
		return taskExecutor;
	}
	
	public ExportReceiver(){
		
	}
	
	@Value("${rabbit.queue.export}")
	private String queueName;

	public void onMessage(Map<String, Object> param) {
		try {
			log.info("ExportReceiver start ..... params:" + param.toString());
			AppParam params = JsonUtil.getInstance().json2Object(param.get("params").toString(), AppParam.class);
			int deleteFlag = NumberUtil.getInt(params.getAttr("deleteFlag"),0);
			if (deleteFlag == 1) {
				String dateStr = StringUtil.getString(params.getAttr("dateStr"));
				deleteExpireDir(dateStr);
				return;
			}
			exportExcel(params);
		} catch (Exception e) {
			log.error( "ExportReceiver  params:" + param.toString(),e);
			XxjrMqSendUtil.saveFailureLog("ExportReceiver", param);
		}
	}

	/**
    * 定期删除文件夹
    * @throws IOException 
    */
   public static void deleteExpireDir(String dateStr) throws IOException {
	   String dirPath = AppProperties
				.getProperties("kf.static.path")+"/upfile/export/"+dateStr;
	   File file = new File(dirPath);
	   FileUtils.deleteDirectory(file);
	   updateFileStatus(dateStr);
   }
   /**
    * 修改文件状态
    * @param dateStr
    */
   private static void updateFileStatus(String dateStr) {
	   Date date = DateUtil.toDateByString(dateStr, DateUtil.DATE_PATTERN_YYYYMMDD);
	   String deleteDateStr = DateUtil.toStringByParttern(date, DateUtil.DATE_PATTERN_YYYY_MM_DD);
	   AppParam param = new AppParam("exportRecordService","updateExpireStatus");
	   param.addAttr("deleteDateStr", deleteDateStr);
	   param.addAttr("status", 3);
	   param.setRmiServiceName(AppProperties.getProperties(
			   DuoduoConstant.RMI_SERVICE_START+ServiceKey.Key_sum));
	   RemoteInvoke.getInstance().call(param);
   }

   /**
    * 异步生成文件
    * @throws IOException 
    */
   public static void exportExcel(AppParam params) {
	   getInstance().execute(new Runnable() {
			public void run() {
				try{
					KfExportUtil.exportExcel(params);
				}catch(Exception e){
					LogerUtil.error(ExportReceiver.class, e, "exportExcel error");
				}
			}
		});
   }

   @Override
   public void init(String queueName ,RabbitMqConfig rabbitMqConfig) {
	   super.init(queueName,rabbitMqConfig,5);
   }
	
   public void init() {
	   init(queueName,rabbitMqConfig);
   }
	
   @PreDestroy
   public void destroy(){
	   reaseResource();
   }
}
