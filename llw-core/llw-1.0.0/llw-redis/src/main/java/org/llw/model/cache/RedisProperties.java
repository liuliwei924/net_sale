package org.llw.model.cache;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;


@Component
@Configuration
@ConfigurationProperties(prefix = "spring.redis")
public class RedisProperties {
	
    private String clusterNodes;
    private int    expireSeconds;
    private String password;
    private int    commandTimeout;
    
	public int getExpireSeconds() {
		return expireSeconds;
	}
	public void setExpireSeconds(int expireSeconds) {
		this.expireSeconds = expireSeconds;
	}
	public String getClusterNodes() {
		return clusterNodes;
	}
	public void setClusterNodes(String clusterNodes) {
		this.clusterNodes = clusterNodes;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public int getCommandTimeout() {
		return commandTimeout;
	}
	public void setCommandTimeout(int commandTimeout) {
		this.commandTimeout = commandTimeout;
	}
    
    
    
}
