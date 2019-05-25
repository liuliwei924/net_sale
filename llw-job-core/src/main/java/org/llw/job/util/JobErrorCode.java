package org.llw.job.util;

public class JobErrorCode {

	/**任务相关的typeCode不存在*/
	public static int JOB_TYPE_CODE_ISNOTEXISTS = 4001;
	
	/**任务已经是enable 不再再enable*/
	public static int JOB_IS_ENABLED = 4002;
	
	/**任务类型不能删除,存在相应的 任务*/
	public static int JOB_TYPE_DEL_HAVE_JOB = 4003;
	
	

	/**任务已经是enable 不再再enable*/
	public static int JOB_IS_DISABLED = 4004;
	
	/**任务不存在*/
	public static int JOB_IS_NOT_EXISTS = 4005;
}
