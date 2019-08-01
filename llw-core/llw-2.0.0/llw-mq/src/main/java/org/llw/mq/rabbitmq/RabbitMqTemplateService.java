package org.llw.mq.rabbitmq;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.AsyncRabbitTemplate;
import org.springframework.amqp.rabbit.AsyncRabbitTemplate.RabbitMessageFuture;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class RabbitMqTemplateService {

	@Autowired
	private RabbitTemplate rabbitTemplate;
	
	@Autowired
	private AsyncRabbitTemplate asyncRabbitTemplate;
	
	
	public void send(RabbitMqMessage rabbitMqMessage) {
		String exchange = rabbitMqMessage.getExchangeName();
		
		if(StringUtils.isEmpty(exchange)) {
			throw new IllegalArgumentException("rabbitmq send message:交换机[exchane]为空");
		}
		
		String routingKey = rabbitMqMessage.getRoutingKey();
		
		if(StringUtils.isEmpty(routingKey)) {
			throw new IllegalArgumentException("rabbitmq send message:路由为空[routingKey]为空");
		}
		
		Map<String,Object> messageParam = rabbitMqMessage.getParams();
		
		if(messageParam == null || messageParam.isEmpty()) {
			return ;
		}
		
		MessageProperties messageProperties = rabbitMqMessage.getMessageProperties();
		
		if(messageProperties == null) 
			messageProperties = new MessageProperties();
		
		CorrelationData correlationData = null;
		if(StringUtils.hasText(rabbitMqMessage.getMsgId()))
			correlationData = new CorrelationData(rabbitMqMessage.getMsgId());
		else
			correlationData = new CorrelationData(UUID.randomUUID().toString());
		
		Message message = rabbitTemplate.getMessageConverter().toMessage(messageParam, messageProperties);
		
		rabbitTemplate.sendAndReceive(exchange, routingKey, message, correlationData);
		rabbitTemplate.send(exchange,routingKey, message, correlationData);
	}	

	public boolean sendAndReceiveByAsyn(RabbitMqMessage rabbitMqMessage) {
		
		String exchange = rabbitMqMessage.getExchangeName();
		
		if(StringUtils.isEmpty(exchange)) {
			throw new IllegalArgumentException("rabbitmq send message:交换机[exchane]为空");
		}
		
		String routingKey = rabbitMqMessage.getRoutingKey();
		
		if(StringUtils.isEmpty(routingKey)) {
			throw new IllegalArgumentException("rabbitmq send message:路由为空[routingKey]为空");
		}
		
		Map<String,Object> messageParam = rabbitMqMessage.getParams();
		
		if(messageParam == null || messageParam.isEmpty()) {
			throw new IllegalArgumentException("rabbitmq send message 发送内容为空");
		}
		
		MessageProperties messageProperties = rabbitMqMessage.getMessageProperties();
		
		if(messageProperties == null) 
			messageProperties = new MessageProperties();
		
		Message message = asyncRabbitTemplate.getMessageConverter().toMessage(messageParam, messageProperties);
		
		RabbitMessageFuture future = asyncRabbitTemplate.sendAndReceive(exchange, routingKey, message);
		
		boolean success =false;
		try {
			success = (future.get() != null);
		} catch (InterruptedException | ExecutionException e) {
			log.error("rabbitmq 异步发送信息报错,",e);
		}
	
		return success;
	}
}
