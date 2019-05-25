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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ddq.common.util.JsonUtil;
import org.ddq.common.web.page.Page;

import com.alibaba.fastjson.serializer.SerializerFeature;

@SuppressWarnings("serial")
public class AppResult implements java.io.Serializable{

	/**系统跳转处理*/
	private String forward;
	/***
	 * 错误代号
	 */
	private String errorCode;
	
	/**
	 * 服务返回消息
	 */
	private String message;
	
	/***
	 * 分页信息
	 */
	private Page page = new Page();
	/**
	 * 服务状态
	 */
	private boolean success = true;
	
	/**
	 * 属性
	 */
	private Map<String,Object> attr = new HashMap<String,Object>();
	
	/**
	 * 返回数据
	 */
	private List<Map<String,Object>> rows = new ArrayList<Map<String,Object>>();
	
	public Map<String, Object> getAttr() {
		return attr;
	}

	public String getForward() {
		return forward;
	}
	public void setForward(String forward) {
		this.forward = forward;
	}
	public void setAttr(Map<String, Object> attr) {
		if(attr == null){
			return;
		}
		this.attr = attr;
	}
	
	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getErrorCode() {
		return errorCode;
	}
	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}
	
	public void putAttrs(Map<String, Object> attr) {
		this.attr.putAll(attr);
	}
	
	public void putAttr(String key,Object value) {
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
	
	
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public void addRow(Map<String, Object> data) {
		this.rows.add(data);
	}
	public void addRows(List<Map<String, Object>> dataList) {
		this.rows.addAll(dataList);
	}
	
	public List<Map<String, Object>> getRows() {
		return rows;
	}
	
	public Map<String, Object> getRow(int index) {
		return rows.get(index);
	}

	public void setRows(List<Map<String, Object>> rows) {
		if(rows == null){
			return;
		}
		this.rows = rows;
	}
	
	public void clearRows() {
		rows.clear();
	}

	
	public Page getPage() {
		if (page == null) {
			page = new Page();
		}
		return page;
	}
	public void setPage(Page page) {
		this.page = page;
	}
	
	public String toJson(SerializerFeature feature){
		return JsonUtil.getInstance().object2JSON(this, feature );
	}
	
	public String toJson(){
		return JsonUtil.getInstance().object2JSON(this, SerializerFeature.WriteDateUseDateFormat);
	}
	

}
