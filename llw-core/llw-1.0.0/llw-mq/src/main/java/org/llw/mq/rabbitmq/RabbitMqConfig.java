package org.llw.mq.rabbitmq;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.rabbitmq.client.Address;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

@Configuration
public class RabbitMqConfig{

	@Autowired
	private RabbitMqProperties rabbitMqProperties;
	/**
	 * 消费者连接工厂
	 * @return
	 */
	public ConnectionFactory createConsFactory(){
		ConnectionFactory connectionFactory = new ConnectionFactory();

		connectionFactory.setUsername(rabbitMqProperties.getUserName());
		connectionFactory.setPassword(rabbitMqProperties.getUserPwd());
		connectionFactory.setConnectionTimeout(10000);
		connectionFactory.setAutomaticRecoveryEnabled(true);
		connectionFactory.setNetworkRecoveryInterval(10000);
		connectionFactory.setRequestedHeartbeat(5000);
		return connectionFactory;
	}

	/**
	 * 生产者连接工厂
	 * @return
	 */
	public ConnectionFactory createProFactory(){
		ConnectionFactory connectionFactory = new ConnectionFactory();
		connectionFactory.setUsername(rabbitMqProperties.getUserName());
		connectionFactory.setPassword(rabbitMqProperties.getUserPwd());
		connectionFactory.setConnectionTimeout(10000);
		connectionFactory.setAutomaticRecoveryEnabled(false);//连接自动修复，生产者使用它，会经常报错
		return connectionFactory;
	}

	private List<Address> getAddresses(){
		String[] hosts = rabbitMqProperties.getHosts().split(",");
		String[] posts = rabbitMqProperties.getPorts().split(",");
		List<Address> addrList = new ArrayList<Address>();

		for(int i=0; i < hosts.length; i++){
			Address addr = new Address(hosts[i], Integer.parseInt(posts[i]));
			addrList.add(addr);
		}
		return addrList;
	}

	/**
	 * 获取生产者rabbit 连接
	 * @return
	 * @throws IOException
	 * @throws TimeoutException
	 */
	public Connection getProRabbitConnection() throws IOException, TimeoutException{
		return createProFactory().newConnection(getAddresses());

	}

	/**
	 * 获取消费者rabbit 连接
	 * @return
	 * @throws IOException
	 * @throws TimeoutException
	 */
	public Connection getConsRabbitConnection() throws IOException, TimeoutException{
		return createConsFactory().newConnection(getAddresses());
	}

	@Bean(initMethod="init",destroyMethod="reaseResource")
	@Primary
	public AsyncRabbitMqTemplate asyncRabbitMqTemplate(){
		return new AsyncRabbitMqTemplate(rabbitMqProperties,this);
	}
	@Bean(destroyMethod="reaseResource")
	@Primary
	public RabbitMqTemplate rabbitMqTemplate(){
		return new RabbitMqTemplate(rabbitMqProperties,this);
	}
}