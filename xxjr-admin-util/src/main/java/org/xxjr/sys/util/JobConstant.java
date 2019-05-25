package org.xxjr.sys.util;

public class JobConstant {
	/** 任务执行 参数 */
	public final static String KEY_JOB_PARAM = "JOB_PARAM";

	/** 任务ID号 **/
	public final static String KEY_JOBID = "jobId";
	public final static String KEY_TYPE_CODE = "typeCode";
	public final static String KEY_JOB_NAME = "jobName";
	public final static String KEY_JOBDESC = "jobDesc";
	public final static String KEY_ENABLE = "enable";
	public final static String KEY_EXECUTE_TYPE = "executeType";
	public final static String KEY_RECURRENT_VALUE = "recurrentValue";
	public final static String KEY_EXECUTE_TIME = "executeTime";
	public final static String KEY_END_TYPE = "endType";
	public final static String KEY_END_DATE = "endDate";
	public final static String KEY_jobParams = "jobParams";
	
	public final static String KEY_LAST_EXE_TIME = "lastExeTime";
	public final static String KEY_LAST_EXE_STATUS = "lastExeStatus";
	public final static String KEY_NEXT_EXE_TIME = "nextExeTime";
	
	/** 任务执行状态 **/
	public final static String KEY_JOB_STATUS = "jobStatus";
	/** 创建人 **/
	public final static String KEY_CREATE_BY = "createBy";
	/** 创建人 **/
	public final static String KEY_UPDATE_BY = "updateBy";
	/** 处理ID号 **/
	public final static String KEY_processId = "processId";
	/** 处理过程的状态 **/
	public final static String KEY_executeStatus = "executeStatus";
	/** 处理通知的状态 **/
	public final static String KEY_notifyStatus = "notifyStatus";
	/** 处理执行信息 **/
	public final static String KEY_executeDesc = "executeDesc";
	/** 服务器名称 **/
	public final static String KEY_ServerName = "serverName";
	/** 开始时间 **/
	public final static String KEY_startTime = "startTime";
	/** 结束时间 **/
	public final static String KEY_endTime = "endTime";

	/** 任务启用状态 禁用 **/
	public final static int JOB_ENABLE_FALSE = 0;
	/** 任务启用状态 启用 **/
	public final static int JOB_ENABLE_TRUE = 1;
	
	
	/** 任务结束状态 0 无结束 **/
	public final static int JOB_END_TYPE_0 = 0;
	/** 任务结束状态 1 根据时间结束 **/
	public final static int JOB_END_TYPE_1 = 1;
	
	/** 任务执行方式 立即执行 **/
	public final static int JOB_EXECUTE_TYPE_0 = 0;
	/** 任务执行方式 预约执行 **/
	public final static int JOB_EXECUTE_TYPE_1 = 1;
	/** 任务执行方式 循环执行 **/
	public final static int JOB_EXECUTE_TYPE_2 = 2;

	/** 处理过程的状态 初始 **/
	public final static int Execute_Status_0 = 0;
	/** 处理过程的状态 运行中 **/
	public final static int Execute_Status_1 = 1;
	/** 处理过程的状态 运行成功 **/
	public final static int Execute_Status_2 = 2;
	/** 处理过程的状态 运行失败 **/
	public final static int Execute_Status_3 = 3;
	/** 处理过程的状态 运行取消 **/
	public final static int Execute_Status_4 = 4;
	/** 处理过程的状态 过期 **/
	public final static int Execute_Status_5 = 5;
	/** 处理过程的状态 已经完成 **/
	public final static int Execute_Status_6 = 6;

	/** 通知状态 未通知 **/
	public final static int Notify_Status_1 = 1;
	/** 通知状态 已通知 **/
	public final static int Notify_Status_2 = 2;
	/** 通知状态 已查看 **/
	public final static int Notify_Status_3 = 3;

	/** 任务锁ID号 **/
	public final static String KEY_lockId = "lockId";
	/** 任务锁 业务类型 **/
	public final static String KEY_businessType = "businessType";
	/** 任务锁 业务ID号 **/
	public final static String KEY_businessId = "businessId";
	
	/** 任务执行结果 **/
	public final static String KEY_EXECUTE_RESULT = "executeResult";

	
	
}
