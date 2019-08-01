package org.llw.com.exception;

public class SysException extends RuntimeException {

	private static final long serialVersionUID = 3116483353040779859L;

	private Object[] args;
	private int errorCode = 0;

	public SysException(String msg) {
		super(msg);
	}

	
	public SysException(int errorCode) {
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
			return "系统出错:" + this.errorCode + "args:" + args;
		}catch(Exception e){
			return "Not Fund MessageInfo:" + errorCode;
		}
	
	}

	public Throwable fillInStackTrace() {
		return this;
	}
}