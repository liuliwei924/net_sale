package org.ddq.common.web.session;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("serial")
public class DuoduoUser implements java.io.Serializable{
	private String username;
	private Long uerId;
	private Map<String,Object> sessionData = new HashMap<String,Object>();
	
	
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public Long getUserId() {
		return uerId;
	}
	
	public void setUserId(Long uerId) {
		this.uerId = uerId;
	}
	
	public Map<String, Object> getSessionData() {
		return sessionData;
	}
	public void setSessionData(Map<String, Object> sessionData) {
		this.sessionData = sessionData;
	}
	
	
}
