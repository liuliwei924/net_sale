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
package org.ddq.common.core.service;

import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.SpringAppContext;
import org.ddq.common.exception.SysException;
import org.springframework.transaction.TransactionDefinition;


public class SoaManager {
	private SoaInvoker soaInvoker;
	
	private volatile static SoaManager soaManager;
	
	private SoaManager(){}
	
	public static SoaManager getInstance(){
		if(soaManager == null){
			synchronized (SoaManager.class) {
				if(soaManager == null){
					soaManager = new SoaManager();
				}
			}
		}
		return soaManager;
	}
	/***
	 * 业务处理
	 * @param param
	 * @return
	 */
	public  AppResult busInvoke(AppParam param) {
		if (param == null) {
			throw new SysException("duoduoSoa: busInvoke param is error");
		}
		if (param.getService() == null) {
			throw new SysException("duoduoSoa: busInvoke service is null !!");
		}
		if(param.getMethod() == null){
			throw new SysException("duoduoSoa: busInvoke method is null !!" + param.getService());
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
			throw new SysException("duoduoSoa: Service invoker is error");
		}
		if (param.getService() == null) {
			throw new SysException("duoduoSoa: Service is null !!");
		}
		if(param.getMethod() == null){
			throw new SysException("duoduoSoa: Service's method is null !!" + param.getService());
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