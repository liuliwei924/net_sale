package org.llw.mq.rabbitmq;

import java.time.Duration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "spring.rabbitmq")
@ConditionalOnProperty(value="spring.rabbitmq.enable",havingValue="true")
public class RabbitMqProperties {

	private String addresses;
	
	private Duration connectionTimeout =  Duration.ofMillis(30000);
	
	private Duration requestedHeartbeat = Duration.ofMillis(10000);
	
	private String userName;
	
	private String userPwd;
	/**缓存channel size 最高限制*/
	private int channelCacheSize =10;
	/** 当 channel size 缓存达到最大时，等待多少秒没获取到，直接抛异常*/
	private Duration channelCheckoutTimeout = Duration.ofMillis(10000);
	
	/**con 缓存量*/
	private int connectionCacheSize = 10;
	/**最大连接*/
	private int connectionLimit = Integer.MAX_VALUE;
	
	// 自动修复
	private boolean automaticRecoveryEnabled =true;
	// 遇到断链，每隔多久重连一次，默认5s
	private Duration networkRecoveryInterval = Duration.ofMillis(5000);
	
	private boolean retryEnable = false;

	public boolean isRetryEnable() {
		return retryEnable;
	}

	public void setRetryEnable(boolean retryEnable) {
		this.retryEnable = retryEnable;
	}

	public String getAddresses() {
		return addresses;
	}

	public void setAddresses(String addresses) {
		this.addresses = addresses;
	}

	
	public Duration getConnectionTimeout() {
		return connectionTimeout;
	}

	public void setConnectionTimeout(Duration connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}

	public Duration getRequestedHeartbeat() {
		return requestedHeartbeat;
	}

	public void setRequestedHeartbeat(Duration requestedHeartbeat) {
		this.requestedHeartbeat = requestedHeartbeat;
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

	public boolean isAutomaticRecoveryEnabled() {
		return automaticRecoveryEnabled;
	}

	public void setAutomaticRecoveryEnabled(boolean automaticRecoveryEnabled) {
		this.automaticRecoveryEnabled = automaticRecoveryEnabled;
	}

	public Duration getNetworkRecoveryInterval() {
		return networkRecoveryInterval;
	}

	public void setNetworkRecoveryInterval(Duration networkRecoveryInterval) {
		this.networkRecoveryInterval = networkRecoveryInterval;
	}

	public int getChannelCacheSize() {
		return channelCacheSize;
	}

	public void setChannelCacheSize(int channelCacheSize) {
		this.channelCacheSize = channelCacheSize;
	}

	public Duration getChannelCheckoutTimeout() {
		return channelCheckoutTimeout;
	}

	public void setChannelCheckoutTimeout(Duration channelCheckoutTimeout) {
		this.channelCheckoutTimeout = channelCheckoutTimeout;
	}

	public int getConnectionCacheSize() {
		return connectionCacheSize;
	}

	public void setConnectionCacheSize(int connectionCacheSize) {
		this.connectionCacheSize = connectionCacheSize;
	}

	public int getConnectionLimit() {
		return connectionLimit;
	}

	public void setConnectionLimit(int connectionLimit) {
		this.connectionLimit = connectionLimit;
	}
   
	
	
	
}
