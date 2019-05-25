package org.llw.job.core;

import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;

/***
 * job执行的类型基本类
 * @author Administrator
 *
 */
public interface BaseExecteJob {

	/***
	 * 执行任务的方法
	 * @param context
	 * @return
	 */
	public AppResult executeJob(AppParam param);
	
}
