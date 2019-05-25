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
package org.ddq.common.exception;


public class AppException extends RuntimeException {

	private static final long serialVersionUID = 8688953989589840707L;

	private Object[] args;
	private int errorCode = 0;

	public AppException(int errorCode) {
		this.errorCode = errorCode;
	}
	
	public AppException(String message) {
		super(message);
	}
	
	
	public AppException(int errorCode,String msg) {
		super(msg);
		this.errorCode = errorCode;
	}
	
	public Object[] getArgs() {
		return this.args;
	}

	public void setArgs(Object[] args) {
		this.args = args;
	}

	public int getErrorCode() {
		return this.errorCode;
	}
	
	public String getMessage() {
		if (this.errorCode == 0 && super.getCause() != null) {
			return super.getCause().getMessage();
		}
		if (this.errorCode == 0 && super.getMessage() != null) {
			return super.getMessage() ;
		}
		try{
			return "系统出错:" + this.errorCode;
		}catch(Exception e){
			return "Not Fund MessageInfo:" + errorCode;
		}
	}

	public Throwable fillInStackTrace() {
		return this;
	}
}