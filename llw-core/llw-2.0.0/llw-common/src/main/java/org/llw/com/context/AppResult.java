package org.llw.com.context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.llw.com.constant.DuoduoConstant;
import org.llw.com.util.JsonUtil;
import org.llw.com.util.NumberUtil;
import org.llw.com.web.page.Page;

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
	
	public int getQueryCount() {
		return NumberUtil.getInt(this.attr.get(DuoduoConstant.TOTAL_SIZE), -1);
	}
	
	public int getInsertCount() {
		return NumberUtil.getInt(this.attr.get(DuoduoConstant.DAO_Insert_SIZE), 0);
	}
	
	public int getUpdateCount() {
		return NumberUtil.getInt(this.attr.get(DuoduoConstant.DAO_Update_SIZE), 0);
	}
	
	public int getDelCount() {
		return NumberUtil.getInt(this.attr.get(DuoduoConstant.DAO_Delete_SIZE), 0);
	}
	
	public AppResult retErrorResult(String errorMsg) {
		return retErrorResult(errorMsg,null);
	}
	
	public AppResult retErrorResult(String errorMsg,String errorCode) {
		this.setSuccess(false);
		this.setMessage(errorMsg);
		this.setErrorCode(errorCode);
		
		return this;
	}
	
	public String toJson(SerializerFeature feature){
		return JsonUtil.getInstance().object2JSON(this, feature );
	}
	
	public String toJson(){
		return JsonUtil.getInstance().object2JSON(this, SerializerFeature.WriteDateUseDateFormat);
	}
	

}
