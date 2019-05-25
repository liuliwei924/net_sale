package org.llw.mq.rabbitmq;

/**
 * RabbitMq 常量类
 * @author liulw 2017-05-02
 *
 */
public class RabbitMqConstant {
	//rabbitmq 默认路由
	public static final String RABBIT_DEFAULT_EX = "myDefaultExchange";
	//rabbitmq 队列key 由每个项目配置队列的key
	public static final String RABBIT_QUEUE_KEY = "rabbitQueueName";
	//rabbitmq 默认队列
	public static final String RABBIT_DEFAULT_QUEUE = "myDefaultQueue";
	
	public static final String RABBIT_DEFAULT_ROUTKEY = "dfRoot_";
	
	public static final int DEFAULT_CHANNEL_COUNT = 10;
}
