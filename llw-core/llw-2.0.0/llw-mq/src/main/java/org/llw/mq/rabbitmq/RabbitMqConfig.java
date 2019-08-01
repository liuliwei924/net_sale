package org.llw.mq.rabbitmq;

import org.springframework.amqp.rabbit.AsyncRabbitTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class RabbitMqConfig{
	
	@Autowired
	private RabbitTemplate rabbitTemplate;
	
//
//	@Autowired
//	 private RabbitMqProperties rabbitMqProperties;
	
//	/**
//	 * 生产者工厂
//	 * @return
//	 */
//	@Bean("proRabbitConnectionFactory")
//	@Primary
//	public CachingConnectionFactory proRabbitConnectionFactory(){
//		CachingConnectionFactory factory = new CachingConnectionFactory();
//		
//		factory.setUsername(rabbitMqProperties.getUserName());
//		factory.setPassword(rabbitMqProperties.getUserPwd());
//		factory.setAddresses(rabbitMqProperties.getAddresses());
//		
//		factory.setConnectionTimeout(rabbitMqProperties.getConnectionTimeout().getNano());
//		factory.setRequestedHeartBeat(rabbitMqProperties.getRequestedHeartbeat().getNano());
//		factory.setCacheMode(CacheMode.CONNECTION);
//		factory.setConnectionCacheSize(rabbitMqProperties.getConnectionCacheSize());
//		factory.setConnectionLimit(rabbitMqProperties.getConnectionLimit());
//		factory.setChannelCacheSize(rabbitMqProperties.getChannelCacheSize());
//		factory.setChannelCheckoutTimeout(rabbitMqProperties.getChannelCheckoutTimeout().toMillis());
//		
//		/** 开始消息确认机制，当在channel增加 confirm监听有效*/
//		factory.setPublisherConfirms(true);
//		
//		/***
//		 * , channel.addReturnListener添加一个监听器，当broker执行basic.return方法时，
//		 * 会回调handleReturn方法，这样我们就可以处理变为死信的消息了；
//		 * 当mandatory设为false时，出现上述情形broker会直接将消息扔掉
//		 */
//		factory.setPublisherReturns(true);
//		
//		return factory;
//	}
//	
//	/**
//	 * 消费者工厂
//	 * @return
//	 */
//	@Bean("consRabbitConnectionFactory")
//	public CachingConnectionFactory conRabbitConnectionFactory(){
//		return proRabbitConnectionFactory();
//	}

	@Bean
	@Primary
	public AsyncRabbitTemplate AsyncRabbitTemplate() {
		return new AsyncRabbitTemplate(rabbitTemplate);
	}
}