package org.llw.mq.rabbitmq;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "spring.rabbitmq")
public class RabbitMqProperties {

	private String hosts;
	private String ports;
	private String userName;
	private String userPwd;
	private String exchangeName;
	
	
	public String getHosts() {
		return hosts;
	}
	public void setHosts(String hosts) {
		this.hosts = hosts;
	}
	public String getPorts() {
		return ports;
	}
	public void setPorts(String ports) {
		this.ports = ports;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getUserPwd() {
		return userPwd;
	}
	public void setUserPwd(String userPwd) {
		this.userPwd = userPwd;
	}
	public String getExchangeName() {
		return exchangeName;
	}
	public void setExchangeName(String exchangeName) {
		this.exchangeName = exchangeName;
	}
}
