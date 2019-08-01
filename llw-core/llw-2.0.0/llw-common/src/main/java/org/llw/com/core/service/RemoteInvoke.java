package org.llw.com.core.service;

import org.llw.com.context.AppParam;
import org.llw.com.context.AppResult;

public class RemoteInvoke {
	
	private RemoteInvoke(){}
	private static class InnerClassSingleton {
		private final static RemoteInvoke remote = new RemoteInvoke();
	 }
	
	public static RemoteInvoke getInstance(){
		return InnerClassSingleton.remote;
	}
	
	
	/***
	 * 调用 事务处理
	 * 
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public AppResult call(final AppParam params) {
		return SoaManager.getInstance().invoke(params);
	}

	/***
	 * 不调用事务处理
	 * 
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public AppResult callNoTx(final AppParam params) {
		return SoaManager.getInstance().callNoTx(params);
	}
}
