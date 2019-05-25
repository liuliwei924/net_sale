package org.xxjr.mq.listener.receiver;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Map;

import javax.annotation.PreDestroy;

import org.ddq.common.context.AppProperties;
import org.ddq.common.util.LogerUtil;
import org.llw.mq.rabbitmq.RabbitMqConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.xxjr.mq.listener.consumer.RabbitMqConsumer;
import org.xxjr.mq.listener.util.XxjrInitAnnotation;

@Component
@XxjrInitAnnotation(beanName="fileMessageReceiver",initMethod="init")
public class FileMessageReceiver extends RabbitMqConsumer{
	
	@Autowired
	private RabbitMqConfig rabbitMqConfig;
	
	@Value("${rabbit.queue.fileUpload}")
	private String queueName;

	public void onMessage(Map<String, Object> messageInfo) {
		try {
			LogerUtil.log("FileMessageReceiver params:" + messageInfo.toString());
			byte[] fileBytes = (byte[])messageInfo.get("fileBytes");
			String workPath = AppProperties.getProperties("sys.static.path");
			Object filePath = workPath + messageInfo.get("filePath");
			if(fileBytes==null || fileBytes.length <= 0 ){
				LogerUtil.error(FileMessageReceiver.class, "not found fileBytes:"+fileBytes+"filePath:"+filePath);
				return;
			}
			File file = new File(filePath.toString());
			FileOutputStream fileOutPutStream = new FileOutputStream(file);
			fileOutPutStream.write(fileBytes);
			fileOutPutStream.close();
		} catch (Exception e) {
			LogerUtil.error(FileMessageReceiver.class, e, "FileMessageReceiver mq execute error!");
		}

	}

	@Override
    public void init(String queueName ,RabbitMqConfig rabbitMqConfig) {
	   super.init(queueName,rabbitMqConfig);
    }
	
    public void init() {
	   init(queueName,rabbitMqConfig);
    }
	
    @PreDestroy
    public void destroy(){
	   reaseResource();
    }
	
	
}
