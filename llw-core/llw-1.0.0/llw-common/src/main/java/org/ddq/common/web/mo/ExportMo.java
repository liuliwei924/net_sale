package org.ddq.common.web.mo;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ExportMo {

	private String tableCode;//表代号
	private String tableName;//国际化关键字
	private String columns;//导出的列,也是表头
	private List<String> columnTypes;
	private Map<String,Object> params;//参数信息
	private String exportName;//导出的显示文件名
	private OutputStream ot;//导出的显示文件名
	private String exportFile;//导出的真实文件名
	private List<Map<String,Object>> values;//导出的内容
	private List<Map<String,Object>> fileNames =new ArrayList<Map<String,Object>>();//当导出内容超过系统设置值，返回的内容
	
	/**最大导出数目*/
	private int pageStart = -1 ;
	
	/**最大导出数目*/
	private int recordLimit;
	/**总记录条数*/
	private int recordSize;
	
	/**每页的最大条数*/
	private Integer pageSize;
	
	private String orderBy;
	private String orderValue;
	
	
	public String getTableCode() {
		return tableCode;
	}
	public void setTableCode(String tableCode) {
		this.tableCode = tableCode;
	}
	public String getColumns() {
		return columns;
	}
	public void setColumns(String columns) {
		this.columns = columns;
	}
	
	public List<String> getColumnTypes() {
		return columnTypes;
	}
	public void setColumnTypes(List<String> columnTypes) {
		this.columnTypes = columnTypes;
	}
	
	public Map<String, Object> getParams() {
		return params;
	}
	public void setParams(Map<String, Object> params) {
		this.params = params;
	}
	public String getExportName() {
		return exportName;
	}
	public void setExportName(String exportName) {
		this.exportName = exportName;
	}
	
	public List<Map<String, Object>> getValues() {
		return values;
	}
	public void setValues(List<Map<String, Object>> values) {
		this.values = values;
	}
	
	public int getPageStart() {
		return pageStart;
	}
	public void setPageStart(int pageStart) {
		this.pageStart = pageStart;
	}
	public int getRecordLimit() {
		return recordLimit;
	}
	public void setRecordLimit(int recordLimit) {
		this.recordLimit = recordLimit;
	}
	public int getRecordSize() {
		return recordSize;
	}
	public void setRecordSize(int recordSize) {
		this.recordSize = recordSize;
	}
	public Integer getPageSize() {
		return pageSize;
	}
	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}
	public List<Map<String, Object>> getFileNames() {
		return fileNames;
	}
	public void setFileNames(List<Map<String, Object>> fileNames) {
		this.fileNames = fileNames;
	}
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	public String getExportFile() {
		return exportFile;
	}
	public void setExportFile(String exportFile) {
		this.exportFile = exportFile;
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
	public OutputStream getOt() {
		return ot;
	}
	public void setOt(OutputStream ot) {
		this.ot = ot;
	}
	
	
}
