package org.llw.mq.rabbitmq;


import java.util.Map;

import com.rabbitmq.client.BuiltinExchangeType;

public class RabbitMqMessage {

	private String exchangeName;
	private String queueName;
	private String routingKey;
	private Map<String,Object> params;
	
	private BuiltinExchangeType isSendType;
	
	public RabbitMqMessage(){};
	
	public RabbitMqMessage(String exchangeName, String queueName,
			String routingKey, BuiltinExchangeType isSendType, Map<String, Object> params) {
		this.exchangeName = exchangeName;
		this.queueName = queueName;
		this.routingKey = routingKey;
		this.isSendType = isSendType;
		this.params = params;
	}
	
	public BuiltinExchangeType getIsSendType() {
		return isSendType;
	}

	public void setIsSendType(BuiltinExchangeType isSendType) {
		this.isSendType = isSendType;
	}

	public String getExchangeName() {
		return exchangeName;
	}
	public void setExchangeName(String exchangeName) {
		this.exchangeName = exchangeName;
	}
	public String getQueueName() {
		return queueName;
	}
	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}
	public String getRoutingKey() {
		return routingKey;
	}
	public void setRoutingKey(String routingKey) {
		this.routingKey = routingKey;
	}
	public Map<String, Object> getParams() {
		return params;
	}
	public void setParams(Map<String, Object> params) {
		this.params = params;
	}
	
	
}
