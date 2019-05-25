package org.llw.job.util;

import java.util.Date;

@SuppressWarnings("serial")
public class JobParamMo implements java.io.Serializable {
	/**任务ID*/
	private Long jobId;
	/**服务名称*/
	private String dealBean;
	/** 执行crons*/
	private Integer executeType;
	/** 执行crons*/
	private Date executeDate;
	/** 执行crons*/
	private Integer endType;
	/** 执行crons*/
	private Date endDate;
	/** 执行crons*/
	private String crons;
	
	/**创建人*/
	private String createBy;
	
	/**执行参数*/
	private String jobParams;

	public JobParamMo() {

	}

	public JobParamMo(Long jobId,
			String dealBean,
			Integer executeType,
			Date executeDate,
			Integer endType,
			Date endDate,
			String crons,
			String createBy,String jobParams) {
		super();
		this.jobId = jobId;
		this.executeType = executeType;
		this.executeDate = executeDate;
		this.endType = endType;
		this.endDate = endDate;
		this.dealBean = dealBean;
		this.crons = crons;
		this.createBy = createBy;
		this.jobParams = jobParams;
	}

	

	public Long getJobId() {
		return jobId;
	}

	public void setJobId(Long jobId) {
		this.jobId = jobId;
	}

	public String getDealBean() {
		return dealBean;
	}

	public void setDealBean(String dealBean) {
		this.dealBean = dealBean;
	}

	public String getCrons() {
		return crons;
	}

	public void setCrons(String crons) {
		this.crons = crons;
	}

	public String getCreateBy() {
		return createBy;
	}

	public void setCreateBy(String createBy) {
		this.createBy = createBy;
	}

	public String getJobParams() {
		return jobParams;
	}

	public void setJobParams(String jobParams) {
		this.jobParams = jobParams;
	}

	public Integer getExecuteType() {
		return executeType;
	}

	public void setExecuteType(Integer executeType) {
		this.executeType = executeType;
	}

	public Date getExecuteDate() {
		return executeDate;
	}

	public void setExecuteDate(Date executeDate) {
		this.executeDate = executeDate;
	}

	public Integer getEndType() {
		return endType;
	}

	public void setEndType(Integer endType) {
		this.endType = endType;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public String toString(){
		return " jobId:" + 
				this.getJobId() + " dealBean:" + this.getDealBean() + 
				" crons:" + this.getCrons() +" executeType:" +
				this.getJobParams() + " createBy:" + this.getCreateBy();
	}
}
