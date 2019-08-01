package org.llw.mq.rabbitmq;


import java.util.Map;

import org.springframework.amqp.core.MessageProperties;

import com.rabbitmq.client.BuiltinExchangeType;

public class RabbitMqMessage {

	private String exchangeName = RabbitMqConstant.RABBIT_DEFAULT_EX;
	private String routingKey;
	private Map<String,Object> params;
	private MessageProperties messageProperties;
	// 可以不设置，一般需要将消息和ID绑定时设置
	private String msgId;

	public RabbitMqMessage(){};
	
	public RabbitMqMessage(String exchangeName, String queueName,
			String routingKey, BuiltinExchangeType isSendType, Map<String, Object> params) {
		this.exchangeName = exchangeName;
		this.routingKey = routingKey;
		this.params = params;
	}
	
	
	public String getExchangeName() {
		return exchangeName;
	}
	public void setExchangeName(String exchangeName) {
		this.exchangeName = exchangeName;
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
	
	public MessageProperties getMessageProperties() {
		return messageProperties;
	}

	public void setMessageProperties(MessageProperties messageProperties) {
		this.messageProperties = messageProperties;
	}
	
	public String getMsgId() {
		return msgId;
	}

	public void setMsgId(String msgId) {
		this.msgId = msgId;
	}
}
