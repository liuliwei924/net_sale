package org.llw.com.core.service;

import org.llw.com.context.AppParam;
import org.llw.com.context.AppResult;
import org.llw.com.core.SpringAppContext;
import org.llw.com.exception.SysException;
import org.springframework.transaction.TransactionDefinition;


public class SoaManager {
	private SoaInvoker soaInvoker;
	
	private SoaManager(){}
	private static class InnerClassSingleton {
		private final static SoaManager soaManager = new SoaManager();
	 }
	
	public static SoaManager getInstance(){
		return InnerClassSingleton.soaManager;
	}
	/***
	 * 业务处理
	 * @param param
	 * @return
	 */
	public  AppResult busInvoke(AppParam param) {
		if (param == null) {
			throw new SysException("SoaManager: busInvoke param is error");
		}
		if (param.getService() == null) {
			throw new SysException("SoaManager: busInvoke service is null !!");
		}
		if(param.getMethod() == null){
			throw new SysException("SoaManager: busInvoke method is null !!" + param.getService());
		}
		if(this.soaInvoker == null){
			this.soaInvoker = (SoaInvoker)SpringAppContext.getBean("soaInvoker");
		}
		return this.soaInvoker.busInvoke(param);
	}
	/***
	 * 默认事务类型,存在则加入事务，不存在则新建事务
	 * @param param
	 * @return
	 */
	public  AppResult invoke(AppParam param) {
		return callTx(param, TransactionDefinition.PROPAGATION_REQUIRED);
	}

	/***
	 * 不使用事务，若存在事务，则挂起当前的事务
	 * @param param
	 * @return
	 */
	public  AppResult callNoTx(AppParam param) {
		return callTx(param, TransactionDefinition.PROPAGATION_NOT_SUPPORTED);
	}
	/***
	 * 不管当前事务情况，都启劝一个新的事务
	 * @param param
	 * @return
	 */
	public  AppResult callNewTx(AppParam param) {
		return callTx(param, TransactionDefinition.PROPAGATION_REQUIRES_NEW);
	}

	private  AppResult callTx(AppParam param, int txType) {
		if (param == null) {
			throw new SysException("SoaManager: Service invoker is error");
		}
		if (param.getService() == null) {
			throw new SysException("SoaManager: Service is null !!");
		}
		if(param.getMethod() == null){
			throw new SysException("SoaManager: Service's method is null !!" + param.getService());
		}
		if(this.soaInvoker == null){
			this.soaInvoker = (SoaInvoker)SpringAppContext.getBean("soaInvoker");
		}
		param.addAttr("transactionType", Integer.valueOf(txType));
		return  this.soaInvoker.invoke(param);
		
	}

	public  SoaInvoker getSoaInvoker() {
		return soaInvoker;
	}

	public void setSoaInvoker(SoaInvoker soaInvoker) {
		this.soaInvoker = soaInvoker;
	}
}