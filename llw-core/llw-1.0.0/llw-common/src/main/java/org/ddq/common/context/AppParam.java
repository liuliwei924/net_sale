/*
 * Copyright (c) 2013, OpenCloudDB/MyCAT and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software;Designed and Developed mainly by many Chinese 
 * opensource volunteers. you can redistribute it and/or modify it under the 
 * terms of the GNU General Public License version 2 only, as published by the
 * Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 * Any questions about this component can be directed to it's project Web address 
 * https://code.google.com/p/opencloudb/.
 *
 */
package org.ddq.common.context;

import java.util.HashMap;
import java.util.Map;

import org.ddq.common.util.JsonUtil;

import com.alibaba.fastjson.serializer.SerializerFeature;

@SuppressWarnings("serial")
public class AppParam implements java.io.Serializable{
	
	public AppParam(){
		
	}
	public AppParam(String rmiServiceName,String serviceName,String methodName){
		this.rmiServiceName = rmiServiceName;	
		this.service = serviceName;
		this.method = methodName;
	}
	
	public AppParam(String serviceName,String methodName){
		this.service = serviceName;
		this.method = methodName;
	}
	
	
	public String toJson(SerializerFeature feature){
		return JsonUtil.getInstance().object2JSON(this, feature );
	}
	
	public String toJson(){
		return JsonUtil.getInstance().object2JSON(this, SerializerFeature.WriteDateUseDateFormat);
	}
	
	
	/**
	 * 远程服务名称
	 */
	private String rmiServiceName;
	
	
	/**用于支持 多数据处理*/
	private String dataBase;
	
	/**
	 * 服务名称
	 */
	private String service;
	/**
	 * 方法名称
	 */
	private String method;
	
	/**
	 * 排序
	 */
	private String orderBy;
	
	/**
	 * 排序值
	 */
	private String orderValue;
	
	/**
	 * 每页条数
	 */
	private int everyPage = 10;

	/**
	 * 当前第几页
	 */
    private int currentPage =1 ;
	
	/***
	 * sessionID
	 */
	private String sessionId;
	
	/**响应时长*/
	private int responseTimeout = 6000;
	/**
	 *参数
	 */
	private Map<String,Object> attr = new HashMap<String,Object>();
	
	
	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public Map<String, Object> getAttr() {
		return attr;
	}

	public void setAttr(Map<String, Object> attr) {
		if(attr == null){
			return;
		}
		this.attr = attr;
	}
	
	public String getDataBase() {
		if (dataBase == null) {
			return "";
		}
		return dataBase;
	}
	public void setDataBase(String dataBase) {
		this.dataBase = dataBase;
	}
	
	
	public void addAttrs(Map<String, Object> attr) {
		this.attr.putAll(attr);
	}
	
	public void addAttr(String key,Object value) {
		this.attr.put(key, value);
	}
	
	public Object removeAttr(String key) {
		return this.attr.remove(key);
	}
	
	public void clearAttr() {
		 this.attr.clear();
	}
	
	public Object getAttr(String key) {
		return this.attr.get(key);
	}
	
	public String getOrderBy() {
		return orderBy;
	}
	public void setOrderBy(String orderBy) {
		this.orderBy = orderBy;
	}
	
	public String getOrderValue() {
		return orderValue;
	}
	public void setOrderValue(String orderValue) {
		this.orderValue = orderValue;
	}
	
	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	public int getEveryPage() {
		return everyPage;
	}
	public void setEveryPage(int everyPage) {
		this.everyPage = everyPage;
	}
	public int getCurrentPage() {
		return currentPage;
	}
	public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
	}
	public int getResponseTimeout() {
		return responseTimeout;
	}
	public void setResponseTimeout(int responseTimeout) {
		this.responseTimeout = responseTimeout;
	}
	public String getRmiServiceName() {
		return rmiServiceName;
	}
	public void setRmiServiceName(String rmiServiceName) {
		this.rmiServiceName = rmiServiceName;
	}
	
}
