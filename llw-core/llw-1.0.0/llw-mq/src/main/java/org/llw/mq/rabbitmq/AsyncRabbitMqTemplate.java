package org.llw.mq.rabbitmq;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.SerializationUtils;
import org.springframework.util.StringUtils;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

public class AsyncRabbitMqTemplate {
	Logger logger = LogManager.getLogger(this.getClass());
	
	private RabbitMqProperties rabbitMqProperties;
	
	private RabbitMqConfig rabbitMqConfig;
	/**生产者连接*/
	private  Connection proRabbitConnection;

    private final BlockingQueue<RabbitMqMessage> queue = new LinkedBlockingQueue<RabbitMqMessage>();
    
    private volatile boolean running = true;
    
    
    public AsyncRabbitMqTemplate(){}
	
	public AsyncRabbitMqTemplate(RabbitMqProperties rabbitMqProperties,RabbitMqConfig rabbitMqConfig){
		this.rabbitMqProperties = rabbitMqProperties;
		this.rabbitMqConfig = rabbitMqConfig;
	}
	
    @PostConstruct
    public void init() {
    	try {
    		new Thread(new RabbitMqWorker()).start();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    public Connection getProRabbitConnection() throws IOException, TimeoutException{
    	if(proRabbitConnection == null){
			synchronized (AsyncRabbitMqTemplate.class) {
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
	
	
	 public void send(String exchangeName, String queueName,String routingKey,
			 BuiltinExchangeType isSendType, Map<String,Object> msgParams) {
        queue.add(new RabbitMqMessage(exchangeName, queueName, routingKey, isSendType, msgParams));
     }

    private void sendMessage(RabbitMqMessage message)  throws Exception {
    	if(StringUtils.isEmpty(message.getExchangeName())){
    		message.setExchangeName(getExchangeName());
    	}
    	if(StringUtils.isEmpty(message.getQueueName())){
    		message.setQueueName(RabbitMqConstant.RABBIT_DEFAULT_QUEUE);
    	}
    	if(StringUtils.isEmpty(message.getRoutingKey())){
    		message.setRoutingKey(RabbitMqConstant.RABBIT_DEFAULT_ROUTKEY + message.getQueueName());
    	}
    	
    	String queueName = message.getQueueName();
    	String exchangeName = message.getExchangeName();
    	String routkey = message.getRoutingKey();
    	
    	if(message.getIsSendType() == BuiltinExchangeType.FANOUT){
    		Channel channel = getProRabbitConnection().createChannel();
        	channel.exchangeDeclare(exchangeName, BuiltinExchangeType.FANOUT, true);
        	channel.basicPublish(exchangeName, "", null, SerializationUtils.serialize(message.getParams()));
        	channel.close();
    	}else{
    		Channel channel = getProRabbitConnection().createChannel();
        	channel.exchangeDeclare(exchangeName, message.getIsSendType(), true);
        	channel.queueDeclare(queueName, true, false, false, null);
        	channel.queueBind(queueName, exchangeName, routkey);
        	channel.basicPublish(exchangeName,  routkey, null, SerializationUtils.serialize(message.getParams()));
        	channel.close();	
    	}
    
    	
    }
	
    /***
     * 发送点对点消息,默认 exchangeName
     * @param queueName
     * @param params
     * @throws Exception
     */
    public void sendDefaultMsg(String queueName, Map<String,Object> params) throws Exception{
    	this.send(getExchangeName(), queueName, null, BuiltinExchangeType.DIRECT, params);
    }
    
    /***
     * 发送点对点消息 自定义exchangeName
     * @param exchangeName
     * @param queueName
     * @param params
     * @throws Exception
     */
    public void sendDirectMsg(String exchangeName,String queueName, Map<String,Object> params) throws Exception{
    	this.send(exchangeName, queueName, null, BuiltinExchangeType.DIRECT, params);	
    }
    
    /***
     * 发送 多个接收消息，类似于topic
     * @param exchangeName
     * @param queueName
     * @param params
     * @throws Exception
     */
    public void sendFanOutMsg(String exchangeName, Map<String,Object> params) throws Exception{
    	this.send(exchangeName, null, null, BuiltinExchangeType.FANOUT, params);	
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
			
			if(queue != null){
			  queue.clear();	
			} 
			
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			 running = false;
		}		
	
	}
	
	
    private final class RabbitMqWorker implements Runnable {

        @Override
        public void run() {
            while (running) {
                try {
                    RabbitMqMessage message = queue.poll(1, TimeUnit.SECONDS);
                    if (message != null) {
                        sendMessage(message);
                    }
                } catch (InterruptedException ie) {
                	logger.error("Interrupted while waiting for RabbitMessage in queue",ie);
                    
                } catch (Exception e) {
                	logger.error("Error sending message",e);
                }
            }
        }
    }

}
