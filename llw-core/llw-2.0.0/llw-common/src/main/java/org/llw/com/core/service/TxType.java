package org.llw.com.core.service;

public enum TxType {
	/**
	 * 默认事务 0
	 * PROPAGATION_REQUIRED--支持当前事务，如果当前没有事务，就新建一个事务
	 */
	DEF_TX(0),
	/**
	 * 创建一个新事务 3
	 * PROPAGATION_REQUIRES_NEW--新建事务，如果当前存在事务，把当前事务挂起
	 */
	NEW_TX(3),
	/**
	 * 创建一个非事务 4
	 * PROPAGATION_NOT_SUPPORTED--以非事务方式执行操作，如果当前存在事务，就把当前事务挂起
	 */
	NO_TX(4);
	
	private int value = 0;
	
	private TxType(int value){
		this.value = value;
	}
	
    public int value() {
        return this.value;
    }

}
