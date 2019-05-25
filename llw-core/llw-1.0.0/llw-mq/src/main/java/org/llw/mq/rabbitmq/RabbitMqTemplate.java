package org.llw.mq.rabbitmq;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import javax.annotation.PreDestroy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.SerializationUtils;
import org.springframework.util.StringUtils;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

public class RabbitMqTemplate {
	Logger logger = LogManager.getLogger(this.getClass());
	
	private RabbitMqProperties rabbitMqProperties;
	
	private RabbitMqConfig rabbitMqConfig;
	/**生产者连接*/
	private  Connection proRabbitConnection;
	
	public RabbitMqTemplate(){}
	
	public RabbitMqTemplate(RabbitMqProperties rabbitMqProperties,RabbitMqConfig rabbitMqConfig){
		this.rabbitMqProperties = rabbitMqProperties;
		this.rabbitMqConfig = rabbitMqConfig;
	}
	
	/**
	 * 获取生产者rabbit 连接
	 * @return
	 * @throws IOException
	 * @throws TimeoutException
	 */
    public  Connection getProRabbitConnection() throws IOException, TimeoutException{
    	if(proRabbitConnection == null){
			synchronized (RabbitMqTemplate.class) {
				if(proRabbitConnection == null){
					proRabbitConnection = rabbitMqConfig.getProRabbitConnection();
				}
			}
		}
    	if(!proRabbitConnection.isOpen()){
    		proRabbitConnection = rabbitMqConfig.getProRabbitConnection();
    	}
    	return  proRabbitConnection;
    	
    }
    
    /**
	 * 获取rabbit路由名称
	 * @return
	 */
	public String getExchangeName(){
		String exchangeName = rabbitMqProperties.getExchangeName();
		return StringUtils.hasText(exchangeName) ? exchangeName : RabbitMqConstant.RABBIT_DEFAULT_EX;
	}
	
	
	public void sendDefaultMsg(Map<String,Object> params) throws Exception{
		   this.sendDefaultMsg(RabbitMqConstant.RABBIT_DEFAULT_QUEUE, params);
    }
	 
    
    public void sendDefaultMsg(String queueName, Map<String,Object> params) throws Exception{
    	String exchangeName = getExchangeName();
    	Channel channel = getProRabbitConnection().createChannel();
    	channel.exchangeDeclare(exchangeName, BuiltinExchangeType.DIRECT, true);
    	channel.queueDeclare(queueName, true, false, false, null);
    	channel.queueBind(queueName, exchangeName, RabbitMqConstant.RABBIT_DEFAULT_ROUTKEY+queueName);
    	channel.basicPublish(exchangeName, RabbitMqConstant.RABBIT_DEFAULT_ROUTKEY+queueName, null, SerializationUtils.serialize(params));
    	channel.close();
    		
    }
    
    public void sendDirectMsg( Map<String,Object> params) throws Exception{
    	this.sendDirectMsg(getExchangeName(), RabbitMqConstant.RABBIT_DEFAULT_QUEUE, params);
    }
    
    public void sendDirectMsg(String exchangeName,String queueName, Map<String,Object> params) throws Exception{
    	Channel channel = getProRabbitConnection().createChannel();
    	channel.exchangeDeclare(exchangeName, BuiltinExchangeType.DIRECT, true);
    	channel.queueDeclare(queueName, true, false, false, null);
    	channel.queueBind(queueName, exchangeName, RabbitMqConstant.RABBIT_DEFAULT_ROUTKEY+queueName);
    	channel.basicPublish(exchangeName,  RabbitMqConstant.RABBIT_DEFAULT_ROUTKEY+queueName, null, SerializationUtils.serialize(params));
    	channel.close();	
    }
    
   
    public void sendFanOutMsg(String exchangeName, Map<String,Object> params) throws Exception{
    	Channel channel = getProRabbitConnection().createChannel();
    	channel.exchangeDeclare(exchangeName, BuiltinExchangeType.FANOUT, true);
    	/*channel.queueDeclare(queueName, true, false, false, null);
    	channel.queueBind(queueName, exchangeName, "");*/
    	channel.basicPublish(exchangeName, "", null, SerializationUtils.serialize(params));
    	channel.close();
    		
    }
    
    public void sendTopicMsg(Map<String,Object> params) throws Exception{
    	this.sendTopicMsg(getExchangeName(), RabbitMqConstant.RABBIT_DEFAULT_QUEUE,RabbitMqConstant.RABBIT_DEFAULT_ROUTKEY, params);
    		
    }
    
    public void sendTopicMsg(String exchangeName,String queueName, String routingKey, Map<String,Object> params) throws Exception{
    	Channel channel = getProRabbitConnection().createChannel();
    	channel.exchangeDeclare(exchangeName, BuiltinExchangeType.TOPIC, true);
    	channel.queueDeclare(queueName, true, false, false, null);
    	channel.queueBind(queueName, exchangeName, routingKey);
    	channel.basicPublish(exchangeName, routingKey, null, SerializationUtils.serialize(params));
    	channel.close();
    		
    }
    
	/***
	 * 释放资源
	 */
    @PreDestroy	
	public void reaseResource() {
		try {		
			if (proRabbitConnection != null) {
				proRabbitConnection.close();
				proRabbitConnection = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}		
	
	}
}
